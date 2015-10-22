#ifndef HAVE_EVENT_MANAGER_H
#define HAVE_EVENT_MANAGER_H 1

#include <string>
#include <map>
#include <thread>
#include <mutex>
#include <condition_variable>

// network I/O
#include <boost/asio.hpp>

#include <amqpcpp.h>
#include "rdf_model.hpp"
#include "ContentItem.hpp"
#include "PersistenceService.hpp"
#include "AnalysisService.hpp"

namespace mico {
    namespace event {

        /**
        * Name of service registry exchange where brokers bind their registration queues. The event manager will send
        * a registration event to this exchange every time a new service is registered.
        */
        static const std::string EXCHANGE_SERVICE_REGISTRY = "service_registry";
        /**
        * Name of service discovery exchange where brokers send discovery requests. The event manager binds its own
        * discovery queue to this exchange and reacts on any incoming discovery events by sending its service list to
        * the replyTo queue provided by the requester.
        */
        static const std::string EXCHANGE_SERVICE_DISCOVERY = "service_discovery";

        /**
        * Name of the queue used for injecting content items when they are newly added to the system.
        */
        static const std::string QUEUE_CONTENT_INPUT  = "content_input";

        /**
        * Name of the queue used for reporting about content items where processing is finished.
        */
        static const std::string QUEUE_CONTENT_OUTPUT = "content_output";

        /**
         * Name of the queue used for configuration discovery.
         */
        static const std::string QUEUE_CONFIG_REQUEST = "config_request";


        class AnalysisConsumer;
        class DiscoveryConsumer;
        class RabbitConnectionHandler;
        class ConfigurationClient;

        // Bug: https://github.com/CopernicaMarketingSoftware/AMQP-CPP/issues/25
        // Got fixed with version 2.2.0
        class AMQPCPPOnCloseBugfix {
        private:
            bool firstCall;
            std::mutex firstCallMutex;

        public:
            AMQPCPPOnCloseBugfix();
            bool isFirstCall();
        };


        /**
        * This exception is thrown by the event manager in case a method call failed.
        */
        class EventManagerException {
        private:
            std::string message;
        public:
            EventManagerException(std::string message) : message(message) {};

            const std::string& getMessage() { return message; };
        };

        class EventManager : public AMQP::ConnectionHandler
        {
        private:
            boost::asio::
            io_service io_service; //!< Boost I/O service for network connection

            boost::asio::ip::tcp::
            socket socket;         //!< Boost socket for connecting with RabbitMQ

            std::string host;    //!< host name to connect to
            int    rabbitPort;   //!< port number of RabbitMQ server
            int    marmottaPort; //!< port number of Marmotta server

            std::string user;         //!< user name to log in with (RabbitMQ, FTP, Marmotta)
            std::string password;     //!< user password to log in with (RabbitMQ, FTP, Marmotta)

            bool connected;              //!< true when connection is finished
            bool unavailable;            //!< indicate connection failure

            mico::persistence::
            PersistenceService *persistence = NULL;  //!< instance of persistence service to resolve content items

            size_t recv_len;
            char*  recv_buf;


            RabbitConnectionHandler* rabbit; //!< AMQP connection handler connecting to a RabbitMQ server using Linux C sockets

            AMQP::Connection*   connection;  //!< connection to RabbitMQ server
            AMQP::Channel*      channel;     //!< channel to RabbitMQ server for sending events

            std::map<AnalysisService*, AnalysisConsumer*> services; //!< services currently registered and their consumers

            std::thread receiver;        //!< event loop for listening and receiving data, starts the io_service
            std::mutex m;                //!< mutex for notifying when asynchronous callback is finished
            std::condition_variable cv;  //!< condition variable for notifying when asynchronous callback is finished

            void doConnect();            //!< internal method called when TCP connection is established
            void doRead();               //!< internal method for reading data from network

            ConfigurationClient *configurationClient = NULL; //<! configuration service

            AMQPCPPOnCloseBugfix amqpWorkaround;

            std::vector<char> intermediateReceiveBuffer; //!< buffer for temoprary storage of bytes of unfinished RabbitMQ frames

        public:

            EventManager(const std::string& host) : EventManager(host, "mico", "mico") {};

            EventManager(const std::string& host, const std::string& user, const std::string& password) : EventManager(host, 5672, 8080, user, password) {};

            /**
            * Initialise the event manager, setting up any necessary channels and connections
            */
            EventManager(const std::string& host, int rabbitPort, int marmottaPort, const std::string& user, const std::string& password);

            /**
            * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
            */
            virtual ~EventManager();

            /**
            * Return a reference to the persistence service used by this event manager, e.g. for creating or managing
            * content items.
            */
            mico::persistence::
            PersistenceService* getPersistenceService();

            /**
            *  Method that is called by the AMQP library every time it has data
            *  available that should be sent to RabbitMQ.
            *  @param  connection  pointer to the main connection object
            *  @param  data        memory buffer with the data that should be sent to RabbitMQ
            *  @param  size        size of the buffer
            */
            void onData(AMQP::Connection *connection, const char *data, size_t size);

            /**
            * When the connection ends up in an error state this method is called.
            * This happens when data comes in that does not match the AMQP protocol
            *
            * After this method is called, the connection no longer is in a valid
            * state and can no longer be used.
            *
            * This method has an empty default implementation, although you are very
            * much advised to implement it. When an error occurs, the connection
            * is no longer usable, so you probably want to know.
            *
            * @param connection The connection that entered the error state
            * @param message Error message
            */
            void onError(AMQP::Connection *connection, const char *message);

            /**
            * Method that is called when the login attempt succeeded. After this method
            * is called, the connection is ready to use. This is the first method
            * that is normally called after you've constructed the connection object.
            *
            * According to the AMQP protocol, you must wait for the connection to become
            * ready (and this onConnected method to be called) before you can start
            * using the Connection object. However, this AMQP library will cache all
            * methods that you call before the connection is ready, so in reality there
            * is no real reason to wait for this method to be called before you send
            * the first instructions.
            *
            * @param connection The connection that can now be used
            */
            void onConnected(AMQP::Connection *connection);

            /**
            * Method that is called when the connection was closed.
            *
            * This is the counter part of a call to Connection::close() and it confirms
            * that the connection was correctly closed.
            *
            * @param connection The connection that was closed and that is now unusable
            */
            void onClosed(AMQP::Connection *connection);

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
            void unregisterService(AnalysisService* service);

            /**
            * Trigger analysis of the given content item.
            *
            * @param item content item to analyse
            * @throws IOException
            */
            void injectContentItem(const mico::persistence::ContentItem& item);

        };

    }
}
#endif
