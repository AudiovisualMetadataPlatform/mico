#include "EventManager.hpp"

#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
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

private:	
	AMQP::Channel&      channel;     //!< channel to RabbitMQ server for sending events	
	
public:	

	Consumer(AMQP::Channel& channel) : channel(channel) {};

	virtual void handleDelivery(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) = 0;

	void operator()(const AMQP::Message &message, uint64_t deliveryTag, bool redelivered) {
		handleDelivery(message,deliveryTag,redelivered);
	};
};
	
	
class AnalysisConsumer : public Consumer {
	
};

class DiscoveryConsumer : public Consumer {
	
};


class RabbitConnectionHandler : public AMQP::ConnectionHandler {

private:
	int sock;
		
public:	

	RabbitConnectionHandler(string& host, int port) {
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
		addr.sin_port = port;


		/* Connect to the server.   */
		if (0 > connect (sock, (struct sockaddr *) &addr, sizeof (addr))) {
			perror ("connect (client)");
			exit (EXIT_FAILURE);
		}
		
		free(hostinfo);
	};
    /**
     *  Method that is called by the AMQP library every time it has data
     *  available that should be sent to RabbitMQ.
     *  @param  connection  pointer to the main connection object
     *  @param  data        memory buffer with the data that should be sent to RabbitMQ
     *  @param  size        size of the buffer
     */
    virtual void onData(AMQP::Connection *connection, const char *data, size_t size)
    {
        // @todo
        //  Add your own implementation, for example by doing a call to the
        //  send() system call. But be aware that the send() call may not
        //  send all data at once, so you also need to take care of buffering
        //  the bytes that could not immediately be sent, and try to send
        //  them again when the socket becomes writable again
    }

    /**
     *  Method that is called by the AMQP library when the login attempt
     *  succeeded. After this method has been called, the connection is ready
     *  to use.
     *  @param  connection      The connection that can now be used
     */
    virtual void onConnected(AMQP::Connection *connection)
    {
        // @todo
        //  add your own implementation, for example by creating a channel
        //  instance, and start publishing or consuming
    }

    /**
     *  Method that is called by the AMQP library when a fatal error occurs
     *  on the connection, for example because data received from RabbitMQ
     *  could not be recognized.
     *  @param  connection      The connection on which the error occured
     *  @param  message         A human readable error message
     */
    virtual void onError(AMQP::Connection *connection, const char *message)
    {
        // @todo
        //  add your own implementation, for example by reporting the error
        //  to the user of your program, log the error, and destruct the
        //  connection object because it is no longer in a usable state
    }

    /**
     *  Method that is called when the connection was closed. This is the
     *  counter part of a call to Connection::close() and it confirms that the
     *  connection was correctly closed.
     *
     *  @param  connection      The connection that was closed and that is now unusable
     */
    virtual void onClosed(AMQP::Connection *connection) {}
	
};	
	
/**
 * Initialise the event manager, setting up any necessary channels and connections
 */
EventManager::EventManager(string host, int rabbitPort, int marmottaPort, string user, string password)
	: host(host), rabbitPort(rabbitPort), marmottaPort(marmottaPort), user(user), password(password)
	, persistence(host, marmottaPort, user, password) {

	// TODO: establish RabbitMQ connection and channel
	rabbit = new RabbitConnectionHandler(host, rabbitPort);
}

/**
 * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
 */
EventManager::~EventManager() {
	delete rabbit;
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