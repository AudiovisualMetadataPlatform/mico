#ifndef HAVE_EVENT_MANAGER_H
#define HAVE_EVENT_MANAGER_H 1

#include <string>
#include <map>

#include "amqpcpp.h"
#include "rdf_model.hpp"
#include "ContentItem.hpp"
#include "PersistenceService.hpp"
#include "AnalysisService.hpp"

namespace mico
{
namespace event
{

using std::string;
using std::map;
using namespace mico::rdf::model;
using namespace mico::persistence;

/**
 * Name of service registry exchange where brokers bind their registration queues. The event manager will send
 * a registration event to this exchange every time a new service is registered.
 */
static const string EXCHANGE_SERVICE_REGISTRY = "service_registry";
/**
 * Name of service discovery exchange where brokers send discovery requests. The event manager binds its own
 * discovery queue to this exchange and reacts on any incoming discovery events by sending its service list to
 * the replyTo queue provided by the requester.
 */
static const string EXCHANGE_SERVICE_DISCOVERY = "service_discovery";

/**
 * Name of the queue used for injecting content items when they are newly added to the system.
 */
static const string QUEUE_CONTENT_INPUT  = "content_input";

/**
 * Name of the queue used for reporting about content items where processing is finished.
 */
static const string QUEUE_CONTENT_OUTPUT = "content_output";


class AnalysisConsumer;
class DiscoveryConsumer;
class RabbitConnectionHandler;

class EventManager
{
private:
	string host;         //!< host name to connect to
	int    rabbitPort;   //!< port number of RabbitMQ server
	int    marmottaPort; //!< port number of Marmotta server

	string user;         //!< user name to log in with (RabbitMQ, FTP, Marmotta)
	string password;     //!< user password to log in with (RabbitMQ, FTP, Marmotta)
	
	PersistenceService persistence;  //!< instance of persistence service to resolve content items
	RabbitConnectionHandler* rabbit; //!< AMQP connection handler connecting to a RabbitMQ server using Linux C sockets
	
	AMQP::Connection*   connection;  //!< connection to RabbitMQ server
	AMQP::Channel*      channel;     //!< channel to RabbitMQ server for sending events
	
	map<AnalysisService*, AnalysisConsumer*> services; //!< services currently registered and their consumers
	
public:

	EventManager(string host) : EventManager(host, "mico", "mico") {};
	
	EventManager(string host, string user, string password) : EventManager(host, 5762, 8080, user, password) {};
	
	/**
	 * Initialise the event manager, setting up any necessary channels and connections
	 */
	EventManager(string host, int rabbitPort, int marmottaPort, string user, string password);
	
	/**
	 * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
	 */
	~EventManager();

	/**
	 * Register the given service with the event manager.
	 *
	 * @param service
	 */
	void registerService(AnalysisService* service);


	/**
	 * Unregister the service with the given ID.
	 * @param service
	 * @throws IOException
	 */
	void unregisterService(const AnalysisService* service);

	/**
	 * Trigger analysis of the given content item.
	 *
	 * @param item content item to analyse
	 * @throws IOException
	 */
	void injectContentItem(const ContentItem& item);


};

}
}
#endif
