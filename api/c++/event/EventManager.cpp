#include "EventManager.hpp"
#include "Event.pb.h"

#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

namespace mico
{
namespace event
{

/**
 * A helper class to simulate the Java RabbitMQ API for consumers
 */	
class Consumer {

protected:	
	AMQP::Channel&      channel;     //!< channel to RabbitMQ server for sending events	
	
public:	

	Consumer(AMQP::Channel& channel) : channel(channel) {};

	virtual void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) = 0;

};
	
class AnalysisConsumer : public Consumer {
private:
    PersistenceService& persistence;
	AnalysisService&  service;
	const std::string& queue;
		
public:	
	AnalysisConsumer(PersistenceService& persistence, AnalysisService& service, const std::string& queue, AMQP::Channel channel) 
		: Consumer(channel), persistence(persistence), service(service), queue(queue) {
		channel.declareQueue(queue, AMQP::durable + AMQP::autodelete)
			.onError([](const char* message) {
				std::cerr << "could not create queue for analysis service: " << message << std::endl;
			});
		
		channel.consume(queue).onReceived([this](const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
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
				
				this->channel.publish("", message.replyTo(), data);
				
				delete buffer;				
			}, *ci, object);
			
			channel.ack(deliveryTag);
	}
};

class DiscoveryConsumer : public Consumer {
	
};


/**
 * Event loop, started in a separate thread. Reads from the socket until EOF and sends all received data to
 * the RabbitMQ connection for processing.
 */ 
static void* event_loop(void *event_manager) {
	EventManager* mgr = static_cast<EventManager*>(event_manager);

	size_t bytes_received;
	std::cout << "starting to listen for messages from RabbitMQ " << std::endl;
	
	while( (bytes_received = read(mgr->sock, (void*)mgr->recv_buf, mgr->recv_len)) > 0) {
		mgr->connection->parse(mgr->recv_buf, bytes_received);
	}
	
	std::cout << "stopping to listen for messages from RabbitMQ " << std::endl;
}
	
/**
 * Initialise the event manager, setting up any necessary channels and connections
 */
EventManager::EventManager(string host, int rabbitPort, int marmottaPort, string user, string password)
	: host(host), rabbitPort(rabbitPort), marmottaPort(marmottaPort), user(user), password(password)
	, persistence(host, marmottaPort, user, password) {

	recv_len = 8192;
	recv_buf = (char*)malloc(recv_len * sizeof(char));
		
	// establish RabbitMQ connection and channel
	struct sockaddr_in addr;

	/* Create the socket.   */
	sock = socket (PF_INET, SOCK_STREAM, 0);
	if (sock < 0) {
		perror ("socket (client)");
		exit (EXIT_FAILURE);
	}

	/* Create the address */
	struct hostent *hostinfo = gethostbyname(host.c_str());
	if(hostinfo == NULL) {
		fprintf (stderr, "unknown host %s", host.c_str());
		exit (EXIT_FAILURE);			
	}

	bzero((char*) &addr, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr = *(struct in_addr *) hostinfo->h_addr;
	addr.sin_port = rabbitPort;


	/* Connect to the server.   */
	if (0 > connect (sock, (struct sockaddr *) &addr, sizeof (addr))) {
		perror ("connect (client)");
		exit (EXIT_FAILURE);
	}
	
	free(hostinfo);

	
	connection = new AMQP::Connection(this, AMQP::Login(user, password), "/");
	channel    = new AMQP::Channel(connection);
	
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

	// register delivery consumer
	
	// start receiving data in separate thread, afterwards return
	pthread_create(&receiver, NULL, event_loop, static_cast<void*>(this));
}

/**
 * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
 */
EventManager::~EventManager() {
	pthread_kill(receiver, SIGINT); //!< cancel the blocking read call with a sigint
	pthread_cancel(receiver);

	free(recv_buf);
	
	shutdown(sock, 2);
	delete channel;
	delete connection;
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
	int pos = 0;
	int sent;
	while ( (sent = send(sock,data + pos, size - pos,0) > 0)) {
		pos += sent;
	}
}


/**
 * Register the given service with the event manager.
 *
 * @param service
 */
void EventManager::registerService(AnalysisService* service) {
	
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