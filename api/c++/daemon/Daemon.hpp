#ifndef HAVE_DAEMON_H
#define HAVE_DAEMON_H 1

#include <vector>
#include <initializer_list>


#include <AnalysisService.hpp>
#include <EventManager.hpp>

namespace mico {
    namespace daemon {


        class Daemon {

            typedef mico::event::EventManager EventManager;
            typedef std::vector<mico::event::AnalysisService*> ServiceList;

            friend int start(const char* name, const char* server, const char* user, const char* password, std::vector<mico::event::AnalysisService*> svcs);
        private:
            const char* name;

            EventManager eventManager;
            ServiceList  services;

            void start();
            void stop();

        public:

            Daemon(const char* name, const char* server, const char* user, const char* password, std::initializer_list<mico::event::AnalysisService*> svcs);
            Daemon(const char* name, const char* server, const char* user, const char* password, std::vector<mico::event::AnalysisService*> svcs);

            ~Daemon();


        };


        int start(const char* name, const char* server, const char* user, const char* password, std::initializer_list<mico::event::AnalysisService*> svcs);
        int start(const char* name, const char* server, const char* user, const char* password, std::vector<mico::event::AnalysisService*> svcs);
        int stop(const char* name);
    }
}

#endif
