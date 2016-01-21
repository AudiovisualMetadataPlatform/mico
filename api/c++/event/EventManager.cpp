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
#include "Event.pb.h"

#include <boost/uuid/uuid_generators.hpp>
#include <google/protobuf/stubs/common.h>

#include "Logging.hpp"

using std::string;
using boost::asio::ip::tcp;
using namespace boost::asio;
using namespace mico::persistence;
using namespace mico::rdf::model;

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
                    if (!amqpWorkaround.isFirstCall())
                        return;
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

        void AnalysisResponse::sendFinish(const ContentItem& ci, const URI& object) {
          LOG_INFO("AnalysisResponse:sendFinish to queue %s", m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          event.set_contentitemuri(ci.getURI().stringValue());
          event.set_objecturi(object.stringValue());
          event.set_serviceid(m_service.getServiceID().stringValue());
          event.set_type(::mico::event::model::MessageType::FINISH);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        void AnalysisResponse::sendErrorMessage(const ContentItem& ci, const URI& object, const std::string& msg)
        {
          LOG_INFO("AnalysisResponse:sendErrorMessage: \"%s\" to queue %s",msg.c_str(), m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          event.set_contentitemuri(ci.getURI().stringValue());
          event.set_objecturi(object.stringValue());
          event.set_serviceid(m_service.getServiceID().stringValue());
          event.set_type(::mico::event::model::MessageType::ERROR);
          event.set_message(msg);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        void AnalysisResponse::sendProgress(const ContentItem& ci, const URI& object, const float& progress)
        {
          LOG_INFO("AnalysisResponse:sendProgress: %f to queue %s", progress, m_message.replyTo().c_str());
          mico::event::model::AnalyzeProgress event;
          event.set_contentitemuri(ci.getURI().stringValue());
          event.set_objecturi(object.stringValue());
          event.set_serviceid(m_service.getServiceID().stringValue());
          event.set_type(::mico::event::model::MessageType::PROGRESS);
          event.set_progress(progress);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        void AnalysisResponse::sendNew(const ContentItem& ci, const URI& object) {
          LOG_INFO("AnalysisResponse:sendNew to queue %s", m_message.replyTo().c_str());
          mico::event::model::AnalysisEvent event;
          event.set_contentitemuri(ci.getURI().stringValue());
          event.set_objecturi(object.stringValue());
          event.set_serviceid(m_service.getServiceID().stringValue());
          event.set_type(::mico::event::model::MessageType::NEW_PART);

          char buffer[event.ByteSize()];
          event.SerializeToArray(buffer, event.ByteSize());

          AMQP::Envelope data(buffer, event.ByteSize());
          data.setCorrelationID(m_message.correlationID());

          m_channel->publish("", m_message.replyTo(), data);
        }

        class AnalysisConsumer : public Consumer {
            friend class EventManager;

        private:
            PersistenceService* persistence;
            AnalysisService&  service;
            const std::string queue;
            AMQPCPPOnCloseBugfix amqpWorkaround;

        public:
            AnalysisConsumer(PersistenceService* persistence, AnalysisService& service, std::string queue, AMQP::Channel* channel)
                    : Consumer(channel), persistence(persistence), service(service), queue(queue) {
                channel->onReady([this, channel, queue]() {
                    if (!amqpWorkaround.isFirstCall())
                        return;
                    channel->declareQueue(queue, AMQP::durable + AMQP::autodelete)
                            .onSuccess([this,channel, queue]() {
                                LOG_INFO("starting to consume data for analysis service %s on queue %s", this->service.getServiceID().stringValue().c_str(), this->queue.c_str());
                                channel->consume(queue).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
                                    this->handleDelivery(message,deliveryTag,redelivered);
                                });
                            });
                });
            }

            virtual ~AnalysisConsumer() {
                LOG_INFO("stopping to consume data for analysis service %s on queue %s",this->service.getServiceID().stringValue().c_str(), this->queue.c_str());
            }


            void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
                mico::event::model::AnalysisEvent event;
                event.ParseFromArray(message.body(), message.bodySize());

                LOG_DEBUG("received analysis event (content item %s, object %s, replyTo %s)", event.contentitemuri().c_str(), event.objecturi().c_str(), message.replyTo().c_str());

                ContentItem *ci = (*persistence).getContentItem(URI(event.contentitemuri()));
                URI object(event.objecturi());
                AnalysisResponse response(this->service, message, channel);

                service.call( response, *ci, object);

                channel->ack(deliveryTag);

                LOG_DEBUG("acknowledged finished processing of analysis event (content item %s, object %s, replyTo %s)", event.contentitemuri().c_str(), event.objecturi().c_str(), message.replyTo().c_str());
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
                if (!amqpWorkaround.isFirstCall())
                    return;

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

            LOG_INFO ("registering analysis service %s...", service->getServiceID().stringValue().c_str());

            boost::uuids::uuid UUID = rnd_gen();
            std::string queue = service->getQueueName() != "" ? service->getQueueName() : boost::uuids::to_string(UUID);

            services[service] = new AnalysisConsumer(getPersistenceService(), *service, queue, new AMQP::Channel(connection));

            mico::event::model::RegistrationEvent registrationEvent;
            registrationEvent.set_type(mico::event::model::REGISTER);
            registrationEvent.set_serviceid(service->getServiceID().stringValue());
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
        void EventManager::injectContentItem(const ContentItem& item) {
            LOG_INFO ("injecting content item %s...", item.getURI().stringValue().c_str());

            mico::event::model::ContentEvent contentEvent;
            contentEvent.set_contentitemuri(item.getURI().stringValue());

            char buffer[contentEvent.ByteSize()];
            contentEvent.SerializeToArray(buffer, contentEvent.ByteSize());

            AMQP::Envelope data(buffer, contentEvent.ByteSize());
            this->channel->publish("", QUEUE_CONTENT_INPUT, data);
        }
    }
}
