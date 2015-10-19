#ifndef HAVE_DAEMON_H
#define HAVE_DAEMON_H 1

#include <vector>
#include <initializer_list>


#include <AnalysisService.hpp>
#include <EventManager.hpp>

namespace mico {
    namespace log {
        class LoggingBackend;
    }
    namespace daemon {

        mico::log::LoggingBackend* createDaemonLogBackend();

        /*
        * Start a MICO daemon of the given name, connecting to the given server with the given user and password. The list of analysis services passed as last argument
        * is automatically registered/unregistered by the daemon
        */
        int start(const char* name, const char* server, const char* user, const char* password, std::initializer_list<mico::event::AnalysisService*> svcs);

        /*
        * Start a MICO daemon of the given name, connecting to the given server with the given user and password. The list of analysis services passed as last argument
        * is automatically registered/unregistered by the daemon
        */
        int start(const char* name, const char* server, const char* user, const char* password, std::vector<mico::event::AnalysisService*> svcs);

        /*
        * Start a MICO daemon of the given name, connecting to the given server with the given user and password. The list of analysis services passed as last argument
        * is automatically registered/unregistered by the daemon
        */
        int stop(const char* name);

    }
}

#endif
