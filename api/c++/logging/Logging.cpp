#include "Logging.hpp"

#include <cstdio>
#include <ctime>

namespace mico {
    namespace log {


        LoggingBackend* backend = new StdOutBackend();

        void StdOutBackend::log(LoggingLevel level, const char *message, va_list args) {
            char       buf[80];

            time_t     now = time(0);
            struct tm  tstruct = *localtime(&now);

            strftime(buf, sizeof(buf), "%Y-%m-%d.%X", &tstruct);

            printf("[%s] ",buf);

            switch (level) {
                case DEBUG:
                    printf("DEBUG ");
                    break;
                case INFO:
                    printf("INFO ");
                    break;
                case WARN:
                    printf("WARN ");
                    break;
                case ERROR:
                    printf("ERROR ");
                    break;
            }

            vprintf(message, args);
            printf("\n");
        }


        /**
        * Configure a different logging backend.
        */
        void set_log_backend(LoggingBackend *b) {
            LoggingLevel level = b->getLevel();

            if(backend != NULL) {
                level = backend->getLevel();
                delete backend;
            }
            backend = b;
            backend->setLevel(level);
        }

        /**
        * Configure a different logging level.
        */
        void set_log_level(LoggingLevel l) {
            if(backend !=0)
               backend->setLevel(l);
        }

    }
}