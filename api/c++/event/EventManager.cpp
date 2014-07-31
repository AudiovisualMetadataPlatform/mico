#include "EventManager.hpp"
#include "Event.pb.h"

#include <cstdlib>
#include <cstdio>
#include <cstring>

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid_generators.hpp>

#define BOOST_LOG_DYN_LINK 1

#include <boost/log/trivial.hpp>


#define LOG_DEBUG BOOST_LOG_TRIVIAL(debug)
#define LOG_INFO  BOOST_LOG_TRIVIAL(info)
#define LOG_WARN  BOOST_LOG_TRIVIAL(warn)
#define LOG_ERROR BOOST_LOG_TRIVIAL(error)

namespace mico
{
namespace event
{

using namespace boost::asio;
	
static boost::uuids::random_generator rnd_gen;


/**
 * A helper class to simulate the Java RabbitMQ API for consumers
 */	
class Consumer {

protected:	
	AMQP::Channel*      channel;     //!< channel to RabbitMQ server for sending events	
	
public:	

	Consumer(AMQP::Channel* channel) : channel(channel) {};

	~Consumer() {
		channel->close();
		delete channel;
	};

	virtual void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) = 0;

};
	
class AnalysisConsumer : public Consumer {
	friend class EventManager;
	
private:
    PersistenceService& persistence;
	AnalysisService&  service;
	const std::string queue;
		
public:	
	AnalysisConsumer(PersistenceService& persistence, AnalysisService& service, std::string queue, AMQP::Channel* channel) 
		: Consumer(channel), persistence(persistence), service(service), queue(queue) {
		channel->onReady([this, channel, queue]() {
			channel->declareQueue(queue, AMQP::durable + AMQP::autodelete)
				.onSuccess([this,channel, queue]() {
					LOG_INFO << "starting to consume data for analysis service " << this->service.getServiceID().stringValue() << " on queue " << this->queue << std::endl;
					channel->consume(queue).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
						this->handleDelivery(message,deliveryTag,redelivered);				
					});							
				});
		});
	}
	
	~AnalysisConsumer() {
		std::cout << "stopping to consume data for analysis service " << this->service.getServiceID().stringValue() << " on queue " << queue << std::endl;
	}
	
		
	void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
			mico::event::model::AnalysisEvent event;
			event.ParseFromArray(message.body(), message.bodySize());
			
			LOG_DEBUG << "received analysis event (content item " << event.contentitemuri() << ", object " << event.objecturi() << ", replyTo " << message.replyTo() << ")" << std::endl;
						
			ContentItem *ci = persistence.getContentItem(URI(event.contentitemuri()));
			URI object(event.objecturi());
			
			service.call([this,&message](const ContentItem& ci, const URI& object) {
				mico::event::model::AnalysisEvent event;
				event.set_contentitemuri(ci.getURI().stringValue());
				event.set_objecturi(object.stringValue());
				event.set_serviceid(this->service.getServiceID().stringValue());
				
				char buffer[event.ByteSize()];
				event.SerializeToArray(buffer, event.ByteSize());
				
				AMQP::Envelope data(buffer, event.ByteSize());
				data.setCorrelationID(message.correlationID());
				
				this->channel->publish("", message.replyTo(), data);
			}, *ci, object);
			
			channel->ack(deliveryTag);
	}
};


	
/**
 * Initialise the event manager, setting up any necessary channels and connections
 */
EventManager::EventManager(string host, int rabbitPort, int marmottaPort, string user, string password)
	: host(host), rabbitPort(rabbitPort), marmottaPort(marmottaPort), user(user), password(password), connected(false), unavailable(false)
	, persistence(host, marmottaPort, user, password), socket(io_service) {

	recv_len = 8192;
	recv_buf = (char*)malloc(recv_len * sizeof(char));
		
	LOG_INFO << "connecting to RabbitMQ server running on host " << host <<", port " << rabbitPort << "\n";

	// establish RabbitMQ connection and channel
	tcp::resolver resolver(io_service);
	tcp::resolver::query query(host, std::to_string(rabbitPort));
	tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);

	async_connect(socket, endpoint_iterator, [this](boost::system::error_code ec, tcp::resolver::iterator) { 
		if(!ec) {
			doConnect(); 			
		} else {
			LOG_ERROR << "network error '" << ec.category().name() << "': " << ec.category().message(ec.value()) << std::endl;
			unavailable = true;
		}
	});

			
	// start the TCP socket operation
	receiver = std::thread([this]() {
		LOG_DEBUG << "starting I/O operations ...\n";
		io_service.run();
		LOG_DEBUG << "stopped I/O operations ...\n";

		{
			std::lock_guard<std::mutex> lk(m);
			connected   = true;
			unavailable = true;
		}
		cv.notify_one();
	});

	
	
	// wait until connected (conditional lock released either via established AMQP connection or io_service end)
	LOG_DEBUG << "waiting until connection is established ...\n";
	{
		std::unique_lock<std::mutex> lk(m);
		cv.wait(lk, [this] { return connected; });
	}
	
	if(!unavailable) {
		LOG_INFO << "event manager initialization finished!\n";
	} else {
		LOG_ERROR << "AMQP connection unavailable\n";
		receiver.join();
		throw EventManagerException("AMQP connection unavailable");
	}	
}

/**
 * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
 */
EventManager::~EventManager() {
	// close AMQP connections
	LOG_INFO << "closing AMQP connection ..." << std::endl;
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
	
}

void EventManager::doConnect() {
	
	// establish connection and channel, the rest is done in their callbacks
	LOG_DEBUG << "establishing AMQP connection ... \n";
	connection = new AMQP::Connection(this, AMQP::Login(user, password), "/");

	// start reading data in a loop
	doRead();

}


void EventManager::doRead() {
	// register read handler
	socket.async_read_some(buffer(recv_buf,recv_len), [this](boost::system::error_code ec, std::size_t bytes_received) {
		if(!ec) {
			connection->parse(recv_buf, bytes_received);
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
	LOG_ERROR << "error in connection handler: "<<message<<std::endl;
}


void EventManager::onConnected(AMQP::Connection *connection) {
	

	LOG_DEBUG << "establishing AMQP channel ... \n";	
	channel    = new AMQP::Channel(connection);
	channel->onReady([this]() {
		// check for the two exchanges we are making use of
		channel->declareExchange(EXCHANGE_SERVICE_REGISTRY, AMQP::fanout, AMQP::passive)	
			.onError([](const char* message) {
				LOG_ERROR << "could not access service registry exchange: " << message << std::endl;
			});
			
		channel->declareExchange(EXCHANGE_SERVICE_DISCOVERY, AMQP::fanout, AMQP::passive)	
			.onError([](const char* message) {
				LOG_ERROR << "could not access service discovery exchange: " << message << std::endl;
			});

		// register delivery consumer
		channel->declareQueue(AMQP::durable + AMQP::autodelete)
			.onSuccess([this](const std::string &name, uint32_t messageCount, uint32_t consumerCount) {
				LOG_INFO << "starting to listen for discovery requests on queue " << name << std::endl;
				channel->bindQueue(EXCHANGE_SERVICE_DISCOVERY, name, "");
				channel->consume(name).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
					LOG_INFO << "received discovery request, sending service list to broker ... \n";

					for(auto entry : services) {
						AnalysisService* service = entry.first;
						AnalysisConsumer* consumer = entry.second;
						
						LOG_INFO << "registering analysis service " << service->getServiceID().stringValue() << "..." << std::endl;
						
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
			LOG_DEBUG << "RabbitMQ connection established!\n";		
		}
		cv.notify_one();
	});

}

void EventManager::onClosed(AMQP::Connection *connection) {
	LOG_DEBUG << "RabbitMQ connection closed!\n";
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
	
	LOG_INFO << "registering analysis service " << service->getServiceID().stringValue() << "..." << std::endl;
	
	boost::uuids::uuid UUID = rnd_gen();
	std::string queue = service->getQueueName() != "" ? service->getQueueName() : boost::uuids::to_string(UUID);
	
	services[service] = new AnalysisConsumer(persistence, *service, queue, new AMQP::Channel(connection));
	
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

	LOG_INFO << "unregistering analysis service " << service->getServiceID().stringValue() << "..." << std::endl;

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
	LOG_INFO << "injecting content item " << item.getURI().stringValue() << "..." << std::endl;
	
	mico::event::model::ContentEvent contentEvent;
	contentEvent.set_contentitemuri(item.getURI().stringValue());
	
	char buffer[contentEvent.ByteSize()];
	contentEvent.SerializeToArray(buffer, contentEvent.ByteSize());
	
	AMQP::Envelope data(buffer, contentEvent.ByteSize());	
	this->channel->publish("", QUEUE_CONTENT_INPUT, data);
}
}
}