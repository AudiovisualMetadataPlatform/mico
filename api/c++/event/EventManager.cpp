#include "EventManager.hpp"
#include "Event.pb.h"

#include <stdio.h>
#include <stdlib.h>


#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid_generators.hpp>


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
private:
    PersistenceService& persistence;
	AnalysisService&  service;
	const std::string& queue;
		
public:	
	AnalysisConsumer(PersistenceService& persistence, AnalysisService& service, std::string queue, AMQP::Channel* channel) 
		: Consumer(channel), persistence(persistence), service(service), queue(queue) {
		channel->declareQueue(queue, AMQP::durable + AMQP::autodelete)
			.onError([](const char* message) {
				std::cerr << "could not create queue for analysis service: " << message << std::endl;
			});
		
		channel->consume(queue).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
			this->handleDelivery(message,deliveryTag,redelivered);				
		});
	}
	
		
	void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
			mico::event::model::AnalysisEvent event;
			event.ParseFromArray(message.body(), message.bodySize());
			
			std::cout << "received analysis event (content item " << event.contentitemuri() << ", object " << event.objecturi() << ", replyTo " << message.replyTo() << ")" << std::endl;
						
			ContentItem *ci = persistence.getContentItem(URI(event.contentitemuri()));
			URI object(event.objecturi());
			
			service.call([this,&message](ContentItem& ci, URI& object) {
				mico::event::model::AnalysisEvent event;
				event.set_contentitemuri(ci.getURI().stringValue());
				event.set_objecturi(object.stringValue());
				event.set_serviceid(this->service.getServiceID().stringValue());
				
				char* buffer = (char*)malloc(event.ByteSize() * sizeof(char));
				event.SerializeToArray(buffer, event.ByteSize());
				
				AMQP::Envelope data(buffer, event.ByteSize());
				data.setCorrelationID(message.correlationID());
				
				this->channel->publish("", message.replyTo(), data);
				
				delete buffer;				
			}, *ci, object);
			
			channel->ack(deliveryTag);
	}
};

class DiscoveryConsumer : public Consumer {
	
};

	
/**
 * Initialise the event manager, setting up any necessary channels and connections
 */
EventManager::EventManager(string host, int rabbitPort, int marmottaPort, string user, string password)
	: host(host), rabbitPort(rabbitPort), marmottaPort(marmottaPort), user(user), password(password), connected(false), unavailable(false)
	, persistence(host, marmottaPort, user, password), socket(io_service) {

	recv_len = 8192;
	recv_buf = (char*)malloc(recv_len * sizeof(char));
		
	std::cout << "connecting to RabbitMQ server running on host " << host <<", port " << rabbitPort << "\n";

	// establish RabbitMQ connection and channel
	tcp::resolver resolver(io_service);
	tcp::resolver::query query(host, std::to_string(rabbitPort));
	tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);

	connect(socket, endpoint_iterator);
	
	
	// establish connection and channel, the rest is done in their callbacks
	connection = new AMQP::Connection(this, AMQP::Login(user, password), "/");
	channel    = new AMQP::Channel(connection);
	
	// start receiving data in separate thread, afterwards return
	receiver = std::thread([this]() {
		std::cout << "starting to listen for responses from RabbitMQ server ...\n";
		io_service.run();
		std::cout << "stopped to listen for responses from RabbitMQ server ...\n";

		{
			std::lock_guard<std::mutex> lk(m);
			connected   = true;
			unavailable = true;
			std::cout << "RabbitMQ connection stopped\n";		
		}
		cv.notify_one();
	});
	
	// start reading data in a loop
	doRead();

	// register delivery consumer
	
	
	// wait until connected
	std::cout << "waiting until connection is established ...\n";
	{
		std::unique_lock<std::mutex> lk(m);
		cv.wait(lk, [this] { return connected; });
	}
	std::cout << "event manager initialization finished!\n";
}

/**
 * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
 */
EventManager::~EventManager() {
	io_service.post([this]() {
		socket.close();
	});
	
	free(recv_buf);
	
	
	delete channel;
	delete connection;
}

void EventManager::doRead() {
	// register read handler
	async_read(socket, buffer(recv_buf,recv_len), [this](boost::system::error_code ec, std::size_t bytes_received) {
		if(!ec) {
			std::cout << "received "<<bytes_received<<" bytes of data\n";
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
	io_service.post([this,data,size]() {
		std::cout << "sending "<<size<<" bytes of data...\n";		
		write(socket, buffer(data,size));
		std::cout << "DONE!\n";	
	});
}


void EventManager::onError(AMQP::Connection *connection, const char *message) {
	std::cerr << "error in connection handler: "<<message<<std::endl;
}


void EventManager::onConnected(AMQP::Connection *connection) {

	// check for the two exchanges we are making use of
	channel->declareExchange(EXCHANGE_SERVICE_REGISTRY, AMQP::fanout, AMQP::passive)	
		.onError([](const char* message) {
			std::cerr << "could not access service registry exchange: " << message << std::endl;
			exit(EXIT_FAILURE);
		});
		
	channel->declareExchange(EXCHANGE_SERVICE_DISCOVERY, AMQP::fanout, AMQP::passive)	
		.onError([](const char* message) {
			std::cerr << "could not access service discovery exchange: " << message << std::endl;
			exit(EXIT_FAILURE);
		});
		
	{
		std::lock_guard<std::mutex> lk(m);
		connected = true;
		std::cout << "RabbitMQ connection established!\n";		
	}
	cv.notify_one();
}

void EventManager::onClosed(AMQP::Connection *connection) {
	std::cout << "RabbitMQ connection closed!\n";
}


/**
 * Register the given service with the event manager.
 *
 * @param service
 */
void EventManager::registerService(AnalysisService* service) {
	if(unavailable) {
		throw std::string("event manager unavailable");
	}
	
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
	
	char* buffer = (char*)malloc(registrationEvent.ByteSize() * sizeof(char));
	registrationEvent.SerializeToArray(buffer, registrationEvent.ByteSize());
	
	AMQP::Envelope data(buffer, registrationEvent.ByteSize());
	
	this->channel->publish(EXCHANGE_SERVICE_REGISTRY, "", data);
	
	delete buffer;				

}


/**
 * Unregister the service with the given ID.
 * @param service
 * @throws IOException
 */
void EventManager::unregisterService(const AnalysisService* service) {
	
}

/**
 * Trigger analysis of the given content item.
 *
 * @param item content item to analyse
 * @throws IOException
 */
void EventManager::injectContentItem(const ContentItem& item) {
	
}
}
}