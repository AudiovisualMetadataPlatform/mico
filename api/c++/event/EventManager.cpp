/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "EventManager.hpp"

#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <thread>
#include <google/protobuf/stubs/common.h>
#include "Logging.hpp"
#include "Uri.hpp"
#include <regex>


using std::string;
using boost::asio::ip::tcp;
using namespace boost::asio;

namespace mico {
    namespace event {


        static boost::uuids::random_generator rnd_gen;


        /**
        * A helper class to simulate the Java RabbitMQ API for consumers
        */
        class Consumer {

        protected:
            AMQP::Channel*      channel;     //!< channel to RabbitMQ server for sending events

        public:

            Consumer(AMQP::Channel* channel) : channel(channel) {};

            virtual ~Consumer() {
                channel->close();
                delete channel;
            };

            virtual void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) = 0;

        };


        AMQPCPPOnCloseBugfix::AMQPCPPOnCloseBugfix() {
            firstCall = true;
        }

        bool AMQPCPPOnCloseBugfix::isFirstCall() {
            firstCallMutex.lock();
            bool retVal = firstCall;
            firstCall = false;
            firstCallMutex.unlock();
            return retVal;
        }

        class ConfigurationClient {
        private:
            bool error = false;
            bool receivedConfig = false;
            AMQP::Channel *channel;
            std::string marmottaBaseURI;
            std::string storageBaseURI;

            std::timed_mutex configAvailableMutex; //!< mutex to wait a given timeout for a config reply.

            AMQPCPPOnCloseBugfix amqpWorkaround;

            void parseResponse(const AMQP::Message &message) {
                mico::event::model::ConfigurationEvent configurationEvent;
                configurationEvent.ParseFromArray(message.body(), message.bodySize());
                marmottaBaseURI = configurationEvent.marmottabaseuri();
                storageBaseURI = configurationEvent.storagebaseuri();
                receivedConfig = true;
                //release mutex as config is available now.
                configAvailableMutex.unlock();
                LOG_INFO("Marmotta base URI: %s", marmottaBaseURI.c_str());
                LOG_INFO("Storage base URI: %s", storageBaseURI.c_str());
            }

        public:
            ConfigurationClient(AMQP::Channel* channel)
                : channel(channel)
            {
                //lock mutex as we do not have the config now.
                configAvailableMutex.lock();

                channel->onReady([this]() {
                    if (!amqpWorkaround.isFirstCall()) {
                      LOG_DEBUG("prevent double execution of ConfigurationClient callback.");
                      return;
                    }
                    LOG_DEBUG("Declaring configuration queue for event manager");
                    //declare config reply queue
                    this->channel->declareQueue(AMQP::autodelete + AMQP::exclusive)
                            .onSuccess([this](const std::string &name, uint32_t messageCount, uint32_t consumerCount) {
                                LOG_INFO ("starting to listen for config replys %s", name.c_str());
                                this->channel->consume(name).onReceived([this, name](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
                                    LOG_INFO ("received config reply ... ");
                                    //TODO: Check correlation ID
                                    this->channel->ack(deliveryTag);
                                    this->channel->removeQueue(name, 0);
                                    parseResponse(message);
                                }).onError([this](const char* message) {
                                    LOG_ERROR ("error consuming message from config reply queue: %s", message);
                                    this->error = true;
                                });

                                //prepare config request
                                mico::event::model::ConfigurationDiscoverEvent discoverEvent;
                                char buffer[discoverEvent.ByteSize()];
                                discoverEvent.SerializeToArray(buffer, discoverEvent.ByteSize());
                                AMQP::Envelope requestEnvelope(buffer, discoverEvent.ByteSize());
                                requestEnvelope.setReplyTo(name);

                                LOG_INFO("sending config request...");
                                this->channel->publish("", QUEUE_CONFIG_REQUEST, requestEnvelope);
                            })
                            .onError([this](const char* message) {
                                LOG_ERROR ("could not declare config reply queue: %s", message);
                                this->error = true;
                            });

                });
                channel->onError([this](const char* message) {
                    LOG_ERROR ("could not open config discover channel: %s", message);
                    this->error = true;
                });
            }

            bool configAvailable() {
                if(error)
                    return false;
                if(receivedConfig)
                    return true;
                if (configAvailableMutex.try_lock_for(std::chrono::milliseconds(5000))) {
                    configAvailableMutex.unlock();
                    return configAvailable();
                } else {
                    LOG_ERROR("did not get a config reply within timeout");
                    error = true;
                }
            }

            std::string getMarmottaBaseURI() {
                if (configAvailable())
                    return marmottaBaseURI;
                return "";
            }

            std::string getStorageBaseURI() {
                if (configAvailable())
                    return storageBaseURI;
                return "";
            }

            ~ConfigurationClient() {
                LOG_INFO("ConfigurationClient destrutor.");
                if (channel != NULL) {
                    if (channel->connected()) {
                        channel->close();
                    }
                    delete(channel);
                    channel = NULL;
                }
            }
        };

        void AnalysisResponse::sendFinish(std::shared_ptr< mico::persistence::model::Item > i) {
          LOG_INFO("AnalysisResponse:sendFinish to queue %s", m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          mico::event::model::AnalysisEvent::Finish *fevent = new mico::event::model::AnalysisEvent::Finish();
          std::shared_ptr<mico::persistence::model::Resource> r = std::dynamic_pointer_cast<mico::persistence::model::Resource>(i);
          fevent->set_itemuri(r->getURI().stringValue());
          fevent->set_serviceid(m_service.getServiceID().stringValue());
          event.set_type(mico::event::model::MessageType::FINISH);
          event.set_allocated_finish(fevent);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        void AnalysisResponse::sendErrorMessage(std::shared_ptr< mico::persistence::model::Item > i, const mico::event::model::ErrorCodes& errcode, const std::string& msg, const std::string& desc)
        {
          LOG_ERROR("AnalysisResponse:sendErrorMessage: \"%s:%s\" to queue %s",msg.c_str(), desc.c_str(), m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          mico::event::model::AnalysisEvent::Error *eevent = new mico::event::model::AnalysisEvent::Error();
          std::shared_ptr<mico::persistence::model::Resource> r = std::dynamic_pointer_cast<mico::persistence::model::Resource>(i);
          eevent->set_itemuri(r->getURI().stringValue());
          eevent->set_serviceid(m_service.getServiceID().stringValue());
          eevent->set_errorcode(errcode);
          eevent->set_message(msg);
          eevent->set_description(desc);
          event.set_type(::mico::event::model::MessageType::ERROR);
          event.set_allocated_error(eevent);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        void AnalysisResponse::sendProgress(std::shared_ptr< mico::persistence::model::Item > i,
                                            const mico::persistence::model::URI& part, const float& progress)
        {
          LOG_INFO("AnalysisResponse:sendProgress: %f to queue %s", progress, m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          mico::event::model::AnalysisEvent::Progress *pevent = new mico::event::model::AnalysisEvent::Progress();
          std::shared_ptr<mico::persistence::model::Resource> r = std::dynamic_pointer_cast<mico::persistence::model::Resource>(i);
          pevent->set_itemuri(r->getURI().stringValue());
          pevent->set_parturi(part.stringValue());
          pevent->set_serviceid(m_service.getServiceID().stringValue());
          pevent->set_progress(progress);
          event.set_type(::mico::event::model::MessageType::PROGRESS);
          event.set_allocated_progress(pevent);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        void AnalysisResponse::sendNew(std::shared_ptr< mico::persistence::model::Item > i, const mico::persistence::model::URI& part) {
          LOG_INFO("AnalysisResponse:sendNew to queue %s", m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          mico::event::model::AnalysisEvent::NewPart* nevent = new mico::event::model::AnalysisEvent::NewPart();
          std::shared_ptr<mico::persistence::model::Resource> r =
              std::dynamic_pointer_cast<mico::persistence::model::Resource>(i);

          if (!r) {
            LOG_DEBUG("Item resource is NULL !!!!");
          } else {
            LOG_DEBUG("Item resource is %s",r->getURI().stringValue().c_str());
          }

          nevent->set_itemuri(r->getURI().stringValue().c_str(), r->getURI().stringValue().size());
          LOG_DEBUG("new event URI set");
          nevent->set_parturi(part.stringValue());
          LOG_DEBUG("new event part URI set");
          nevent->set_serviceid(m_service.getServiceID().stringValue().c_str(), m_service.getServiceID().stringValue().size());
          LOG_DEBUG("new event service id set");
          event.set_type(::mico::event::model::MessageType::NEW_PART);
          LOG_DEBUG("new event type set");
          event.set_allocated_new_(nevent);
          LOG_DEBUG("event set allocated new called with nevent");

          char buffer[event.ByteSize()];

          LOG_DEBUG("buffer set to size of event: %d", event.ByteSize());

          event.SerializeToArray(buffer, event.ByteSize());

          LOG_DEBUG("event serialized to buffer");

          AMQP::Envelope data(buffer, event.ByteSize());

          LOG_DEBUG("AMQP envelope created");

          data.setCorrelationID(m_message.correlationID());

          LOG_DEBUG("AMQP correlation ID set");

          m_channel->publish("", m_message.replyTo(), data);

          LOG_DEBUG("Event published");
        }

        class AnalysisConsumer : public Consumer {
            friend class EventManager;

        private:
            mico::persistence::PersistenceService* persistence;
            AnalysisService&  service;
            const std::string queue;
            AMQPCPPOnCloseBugfix amqpWorkaround;

            std::vector< std::shared_ptr<mico::persistence::model::Resource> > parseResourceList(
                std::vector<mico::persistence::model::URI> resourceURIList,
                std::shared_ptr<mico::persistence::model::Item> item)
            {
              std::vector< std::shared_ptr<mico::persistence::model::Resource> > resVec;

              std::shared_ptr< mico::persistence::model::Resource > itemResource =
                  std::dynamic_pointer_cast<mico::persistence::model::Resource>(item);


              for (auto resourceUri : resourceURIList) {
                  if (itemResource->getURI() == resourceUri) {
                      resVec.push_back(std::dynamic_pointer_cast<mico::persistence::model::Resource>(item));
                  } else {
                      std::shared_ptr<mico::persistence::model::Part> part = item->getPart(resourceUri);
                      if (part) {
                        resVec.push_back(std::dynamic_pointer_cast<mico::persistence::model::Resource>(part));
                      } else {
                        LOG_WARN("EventManger received URI which neither represents a Part or a Item");
                      }
                  }
              }
              return resVec;

            }

        public:
            AnalysisConsumer(mico::persistence::PersistenceService* persistence, AnalysisService& service, std::string queue, AMQP::Channel* channel)
                    : Consumer(channel), persistence(persistence), service(service), queue(queue)
            {
                channel->onReady([this, channel, queue]() {
                    if (!amqpWorkaround.isFirstCall()) {

                        return;
                    }
                    LOG_DEBUG("Declaring consumption queue [%s] for analysis service %s",queue.c_str(),
                              this->service.getServiceID().stringValue().c_str());
                    channel->declareQueue(queue, AMQP::durable + AMQP::autodelete)
                      .onSuccess([this,channel, queue]() {
                          LOG_INFO("starting to consume data for analysis service %s on queue %s", this->service.getServiceID().stringValue().c_str(), this->queue.c_str());
                          channel->consume(queue).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
                              this->handleDelivery(message,deliveryTag,redelivered);
                          });
                      })
                      .onError([this](const char* message) {
                          LOG_ERROR ("Cannot consume data on queue %s: %s", this->queue.c_str(), message);
                      })
                      .onFinalize([this]() {
                          LOG_DEBUG ("Finalize called on queue %s", this->queue.c_str());
                    });
                });
            }

            virtual ~AnalysisConsumer() {
                LOG_INFO("stopping to consume data for analysis service %s on queue %s",this->service.getServiceID().stringValue().c_str(), this->queue.c_str());
            }


            void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
                mico::event::model::AnalysisRequest event;
                if (event.ParseFromArray(message.body(), message.bodySize())){
                    std::stringstream ss;
                    ss << std::this_thread::get_id();

                    LOG_DEBUG("Received analysis event (content item %s, object %s, replyTo %s) in thread %s",
                              event.itemuri().c_str(), event.parturi(0).c_str(), message.replyTo().c_str(), ss.str().c_str());

                    std::vector<mico::persistence::model::URI> resourceURIList;

                    for (unsigned int i = 0; i < event.parturi().size(); ++i) {
                      resourceURIList.push_back(mico::persistence::model::URI(event.parturi(i)));
                    }

                    std::shared_ptr<mico::persistence::model::Item> item =
                        (*persistence).getItem(mico::persistence::model::URI(event.itemuri()));

                    std::vector<std::shared_ptr<mico::persistence::model::Resource> > resources =
                        parseResourceList(resourceURIList, item);

                    std::map<std::string,std::string> params;

                    for (unsigned int i = 0; i < event.params().size(); ++i) {
                      params[ event.params(i).key() ] = event.params(i).value();
                    }
                    AnalysisResponse response(this->service, message, channel);

                    service.call( response, item, resources, params);

                    LOG_DEBUG("acknowledged finished processing of analysis event (content item %s, object %s, replyTo %s)", event.itemuri().c_str(), event.parturi(0).c_str(), message.replyTo().c_str());
                }else{
                    //@TODO send error to broker
                    LOG_ERROR("Could not parse received analysis event.");
                    LOG_INFO("received event data: %s ", event.DebugString().c_str());
                }

                channel->ack(deliveryTag);

            }
        };



        /**
        * Initialise the event manager, setting up any necessary channels and connections
        */
        EventManager::EventManager(const string& host, int rabbitPort, int marmottaPort, const string& user, const string& password)
                : host(host), rabbitPort(rabbitPort), marmottaPort(marmottaPort)
                , user(user), password(password), connected(false), unavailable(false)
                , socket(io_service)  {


            recv_len = 8192;
            recv_buf = (char*)malloc(recv_len * sizeof(char));

            //reserving 10000 bytes here since this is the default max frame size set in AMQP
            intermediateReceiveBuffer.reserve(10000);

            LOG_INFO ("connecting to RabbitMQ server running on host %s, port %d", host.c_str(), rabbitPort);

            // establish RabbitMQ connection and channel
            tcp::resolver resolver(this->io_service);
            tcp::resolver::query query(host, std::to_string(rabbitPort));
            tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);

            async_connect(socket, endpoint_iterator, [this](boost::system::error_code ec, tcp::resolver::iterator) {
                if(!ec) {
                    doConnect();
                } else {
                    LOG_ERROR("network error '%s': %s", ec.category().name(), ec.category().message(ec.value()).c_str());
                    unavailable = true;
                }
            });


            // start the TCP socket operation
            receiver = std::thread([this]() {
                LOG_DEBUG("starting I/O operations ...");
                io_service.run();
                LOG_DEBUG("stopped I/O operations ...");

                {
                    std::lock_guard<std::mutex> lk(m);
                    connected   = true;
                    unavailable = true;
                }
                cv.notify_one();
            });



            // wait until connected (conditional lock released either via established AMQP connection or io_service end)
            LOG_DEBUG("waiting until connection is established ...");
            {
                std::unique_lock<std::mutex> lk(m);
                cv.wait(lk, [this] { return connected; });
            }

            if(!unavailable) {
                LOG_INFO("event manager initialization finished!");
            } else {
                LOG_ERROR("AMQP connection unavailable");
                receiver.join();
                throw EventManagerException("AMQP connection unavailable");
            }
        }

        /**
        * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
        */
        EventManager::~EventManager() {
            // close AMQP connections
            LOG_INFO("closing AMQP connection ...");
            channel->close();
            connection->close();

            io_service.post([this]() {
                socket.close();
            });

            free(recv_buf);

            delete channel;
            delete connection;

            // wait for I/O loop thread to finish properly
            receiver.join();

            if (persistence != NULL)
                delete(persistence);

            if (configurationClient != NULL)
              delete configurationClient;

            google::protobuf::ShutdownProtobufLibrary();
        }

        void EventManager::doConnect() {

            // establish connection and channel, the rest is done in their callbacks
            LOG_DEBUG("establishing AMQP connection ... ");
            connection = new AMQP::Connection(this, AMQP::Login(user, password), "/");

            // start reading data in a loop
            doRead();

        }


        void EventManager::doRead() {
            // register read handler
            socket.async_read_some(buffer(recv_buf,recv_len), [this](boost::system::error_code ec, std::size_t bytes_received) {
                if(!ec) {
                    intermediateReceiveBuffer.insert(intermediateReceiveBuffer.end(),recv_buf,recv_buf+bytes_received);

                    // TODO: THIS IS A REALLY BAD AND NASTY THING I DID HERE. It's delaying the message processing for each
                    // incoming package by 10 milliseconds in order to create a virtual thread safetly when registering callbacks
                    // once this even loop in a different thread is running already. See also:
                    //
                    // https://github.com/CopernicaMarketingSoftware/AMQP-CPP/issues/90
                    //
                    std::this_thread::sleep_for(std::chrono::milliseconds(10));

                    size_t bytes_processed =
                        connection->parse(&intermediateReceiveBuffer[0], intermediateReceiveBuffer.size());

                    intermediateReceiveBuffer.erase(intermediateReceiveBuffer.begin(),
                                                    intermediateReceiveBuffer.begin()+bytes_processed);
                    doRead();
                } else {
                    socket.close();
                }
            });
        }

        /**
        *  Method that is called by the AMQP library every time it has data
        *  available that should be sent to RabbitMQ.
        *  @param  connection  pointer to the main connection object
        *  @param  data        memory buffer with the data that should be sent to RabbitMQ
        *  @param  size        size of the buffer
        */
        void EventManager::onData(AMQP::Connection *connection, const char *data, size_t size)
        {
            char *copy = (char*)malloc(size);
            memcpy(copy,data,size);
            async_write(socket, buffer(copy,size), [this, copy](boost::system::error_code ec, std::size_t bytes_sent) {
                free(copy);
                if(ec) {
                    socket.close();
                }
            });
        }


        void EventManager::onError(AMQP::Connection *connection, const char *message) {
            LOG_ERROR ("error in connection handler: %s", message);
        }


        void EventManager::onConnected(AMQP::Connection *connection) {
            LOG_DEBUG("establishing AMQP channel ... ");

            if (configurationClient != NULL)
              delete configurationClient;

            // setup configuration service (channel gets shut down by the service itself)
            configurationClient = new ConfigurationClient(new AMQP::Channel(connection));

            channel    = new AMQP::Channel(connection);
            channel->onReady([this]() {
                if (!amqpWorkaround.isFirstCall()) {
                    LOG_DEBUG("prevent double execution of onConnected.");
                    return;
                }

                // check for the two exchanges we are making use of
                channel->declareExchange(EXCHANGE_SERVICE_REGISTRY, AMQP::fanout, AMQP::passive)
                        .onError([](const char* message) {
                            LOG_ERROR ("could not access service registry exchange: %s", message);
                        });

                channel->declareExchange(EXCHANGE_SERVICE_DISCOVERY, AMQP::fanout, AMQP::passive)
                        .onError([](const char* message) {
                            LOG_ERROR ("could not access service discovery exchange: %s", message);
                        });

                // register discovery consumer
                channel->declareQueue(AMQP::durable + AMQP::autodelete)
                        .onSuccess([this](const std::string &name, uint32_t messageCount, uint32_t consumerCount) {
                            LOG_INFO ("starting to listen for discovery requests on queue %s", name.c_str());
                            channel->bindQueue(EXCHANGE_SERVICE_DISCOVERY, name, "");
                            channel->consume(name).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
                                LOG_INFO ("received discovery request, sending service list to broker ... ");

                                for(auto entry : services) {
                                    AnalysisService* service = entry.first;
                                    AnalysisConsumer* consumer = entry.second;

                                    LOG_INFO ("registering analysis service %s...", service->getServiceID().stringValue().c_str());

                                    mico::event::model::RegistrationEvent registrationEvent;
                                    registrationEvent.set_type(mico::event::model::REGISTER);
                                    registrationEvent.set_serviceid(service->getServiceID().stringValue());
                                    registrationEvent.set_extractorid(service->getExtractorID());
                                    registrationEvent.set_extractormodeid(service->getExtractorModeID());
                                    registrationEvent.set_extractorversion(service->getExtractorVersion());
                                    registrationEvent.set_queuename(consumer->queue);
                                    registrationEvent.set_language(mico::event::model::CPP);
                                    registrationEvent.set_requires(service->getRequires());
                                    registrationEvent.set_provides(service->getProvides());

                                    char buffer[registrationEvent.ByteSize()];
                                    registrationEvent.SerializeToArray(buffer, registrationEvent.ByteSize());

                                    AMQP::Envelope data(buffer, registrationEvent.ByteSize());
                                    data.setCorrelationID(message.correlationID());

                                    this->channel->publish("", message.replyTo(), data);

                                }

                                this->channel->ack(deliveryTag);
                            });
                        });


                {
                    std::lock_guard<std::mutex> lk(m);
                    connected = true;
                    LOG_DEBUG("RabbitMQ connection established!");
                }
                cv.notify_one();
            });

        }

        /**
         * Get the persistence service. On the first call it creates the instance using the marmotta and storage base
         * URI provided by the configuration service.
         */
        mico::persistence::PersistenceService* EventManager::getPersistenceService() {
            if (persistence == NULL) {
                //configAvailable will wait for a specific timeout, if the configuration fetch process has not finished
                // yet.
                if (!configurationClient->configAvailable()) {
                    LOG_ERROR("failed to get configuration");
                    throw EventManagerException("failed to get configuration");
                    unavailable = true;
                }
                persistence = new mico::persistence::PersistenceService(configurationClient->getMarmottaBaseURI(), configurationClient->getStorageBaseURI());
            }
            return persistence;
        }

        void EventManager::onClosed(AMQP::Connection *connection) {
            LOG_DEBUG("RabbitMQ connection closed!");
        }


        /**
        * Register the given service with the event manager.
        *
        * @param service
        */
        void EventManager::registerService(AnalysisService* service) {
            if(unavailable) {
                throw EventManagerException("event manager unavailable");
            }
            std::stringstream ss;
            ss << std::this_thread::get_id();

            LOG_INFO ("registering analysis service %s in thread %s...", service->getServiceID().stringValue().c_str(), ss.str().c_str());

            boost::uuids::uuid UUID = rnd_gen();

            std::string queue = service->getExtractorID() + "-" +
                                stripPatchVersion(service->getExtractorVersion()) + "-" +
                                service->getExtractorModeID();


            services[service] = new AnalysisConsumer(getPersistenceService(), *service, queue, new AMQP::Channel(connection));

            mico::event::model::RegistrationEvent registrationEvent;
            registrationEvent.set_type(mico::event::model::REGISTER);
            registrationEvent.set_serviceid(service->getServiceID().stringValue());
            registrationEvent.set_extractorid(service->getExtractorID());
            registrationEvent.set_extractormodeid(service->getExtractorModeID());
            registrationEvent.set_extractorversion(service->getExtractorVersion());
            registrationEvent.set_queuename(queue);
            registrationEvent.set_language(mico::event::model::CPP);
            registrationEvent.set_requires(service->getRequires());
            registrationEvent.set_provides(service->getProvides());

            char buffer[registrationEvent.ByteSize()];
            registrationEvent.SerializeToArray(buffer, registrationEvent.ByteSize());

            AMQP::Envelope data(buffer, registrationEvent.ByteSize());

            this->channel->publish(EXCHANGE_SERVICE_REGISTRY, "", data);

        }

        /**
        * Unregister the service with the given ID.
        * @param service
        * @throws IOException
        */
        void EventManager::unregisterService(AnalysisService* service) {

            LOG_INFO ("unregistering analysis service %s", service->getServiceID().stringValue().c_str());

            AnalysisConsumer* consumer = services[service];

            mico::event::model::RegistrationEvent registrationEvent;
            registrationEvent.set_type(mico::event::model::UNREGISTER);
            registrationEvent.set_serviceid(service->getServiceID().stringValue());
            registrationEvent.set_extractorid(service->getExtractorID());
            registrationEvent.set_extractormodeid(service->getExtractorModeID());
            registrationEvent.set_extractorversion(service->getExtractorVersion());
            registrationEvent.set_queuename(consumer->queue);
            registrationEvent.set_language(mico::event::model::CPP);
            registrationEvent.set_requires(service->getRequires());
            registrationEvent.set_provides(service->getProvides());

            char buffer[registrationEvent.ByteSize()];
            registrationEvent.SerializeToArray(buffer, registrationEvent.ByteSize());

            AMQP::Envelope data(buffer, registrationEvent.ByteSize());

            this->channel->publish(EXCHANGE_SERVICE_REGISTRY, "", data);

            delete consumer;
            services.erase(service);
        }

        /**
        * Trigger analysis of the given content item.
        *
        * @param item content item to analyse
        * @throws IOException
        */
        void EventManager::injectItem(std::shared_ptr< mico::persistence::model::Item > item) {
            std::shared_ptr<mico::persistence::model::Resource> r = std::dynamic_pointer_cast<mico::persistence::model::Resource>(item);
            LOG_INFO ("injecting content item %s...", r->getURI().stringValue().c_str());

            mico::event::model::ItemEvent contentEvent;
            contentEvent.set_itemuri(r->getURI().stringValue());

            char buffer[contentEvent.ByteSize()];
            contentEvent.SerializeToArray(buffer, contentEvent.ByteSize());

            AMQP::Envelope data(buffer, contentEvent.ByteSize());
            this->channel->publish("", QUEUE_CONTENT_INPUT, data);
        }

        /**
         *  Given a string of format major_version.minor_version.patch_version, e.g. "3.0.0-SNAPSHOT"
         *  returns a string of the format major_version.minor_version, e.g. "3.0"
         */
        std::string EventManager::stripPatchVersion(std::string version){

          std::string result;
          try {
            std::regex re("([0-9]+\\.[0-9]+)\\.[0-9]+.*");
            std::smatch match;
            if (std::regex_search(version, match, re) && match.size() > 1) {
              result = match.str(1);
            } else {
              std::string msg =  "The input version \""+version+"\" is not formatted as MAJ.MIN.PATCH";
              LOG_ERROR(msg.c_str());
              throw std::runtime_error(msg);
            }
          } catch (std::regex_error& e) {
            LOG_ERROR(e.what());
            throw e;
          }
          return result;

        }
    }
}
