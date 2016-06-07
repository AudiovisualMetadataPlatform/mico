#include <signal.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>

#include <cstdlib>

#include "Daemon.hpp"
#include "Logging.hpp"

#undef LOG_DEBUG
#undef LOG_INFO
#undef LOG_WARN
#undef LOG_ERROR

#include <libdaemon/dfork.h>
#include <libdaemon/dsignal.h>
#include <libdaemon/dlog.h>
#include <libdaemon/dpid.h>
#include <pwd.h>


namespace mico {
    namespace daemon {


        using mico::event::AnalysisService;

        class DaemonLogBackend : public log::LoggingBackend {


        public:
            virtual void log(log::LoggingLevel level, const char *message, va_list args) {
                switch(level) {
                    case log::LoggingLevel::DEBUG:
                        daemon_logv(LOG_DEBUG, message, args);
                        break;
                    case log::LoggingLevel::INFO:
                        daemon_logv(LOG_INFO, message, args);
                        break;
                    case log::LoggingLevel::WARN:
                        daemon_logv(LOG_WARNING, message, args);
                        break;
                    case log::LoggingLevel::ERROR:
                        daemon_logv(LOG_ERR, message, args);
                        break;
                }
            }
        };

        class Daemon {

            typedef mico::event::EventManager EventManager;
            typedef std::vector<mico::event::AnalysisService*> ServiceList;

            friend int start(const char* name, const char* server, const char* user, const char* password, std::vector<mico::event::AnalysisService*> svcs);
        private:
            EventManager eventManager;
            ServiceList  services;

            void start() {
                for (AnalysisService *s : services) {
                    eventManager.registerService(s);
                }
            }

            void stop() {
                for (AnalysisService *s : services) {
                    eventManager.unregisterService(s);
                }

            }

        public:

            Daemon(const std::string name, const std::string& server, const std::string& user, const std::string& password, const std::vector<mico::event::AnalysisService*>& svcs)
                : eventManager(server,user,password) {

                for (AnalysisService *s : svcs) {
                    services.push_back(s);
                }
            };

            ~Daemon() {
                for (AnalysisService *s : services) {
                    delete s;
                }
            }


        };


        log::LoggingBackend* createDaemonLogBackend() { return (log::LoggingBackend*) new DaemonLogBackend(); }

        int start(const char* name, const char* server, const char* user, const char* password, std::initializer_list<mico::event::AnalysisService*> svcs) {
            std::vector<AnalysisService*> services;
            for (AnalysisService *s : svcs) {
                services.push_back(s);
            }

            return start(name, server, user, password, services);
        }


        int start(const char* _name, const char* _server, const char* _user, const char* _password, std::vector<mico::event::AnalysisService*> svcs) {

            pid_t pid;
            daemon_set_verbosity(LOG_DEBUG);
            log::set_log_backend(new DaemonLogBackend());
            log::set_log_level(log::LoggingLevel::DEBUG);
            daemon_log(LOG_INFO, "starting daemon and registering services ...");


            std::string name(_name);
            std::string server(_server);
            std::string user(_user);
            std::string password(_password);

            /* Reset signal handlers */
            if (daemon_reset_sigs(-1) < 0) {
                daemon_log(LOG_ERR, "Failed to reset all signal handlers: %s", strerror(errno));
                return 1;
            }
            /* Unblock signals */
            if (daemon_unblock_sigs(-1) < 0) {
                daemon_log(LOG_ERR, "Failed to unblock all signals: %s", strerror(errno));
                return 1;
            }

            /* Set identification string for the daemon for both syslog and PID file */
            char* logname = strdup(_name);
            daemon_pid_file_ident = daemon_log_ident = daemon_ident_from_argv0(logname);

            daemon_log(LOG_DEBUG, "daemon_log_ident set to %s", daemon_log_ident);


            /* Check that the daemon is not rung twice a the same time */
            if ((pid = daemon_pid_file_is_running()) >= 0) {
                daemon_log(LOG_ERR, "Daemon already running on PID file %u", pid);
                return 1;
            }

            /* Prepare for return value passing from the initialization procedure of the daemon process */
            if (daemon_retval_init() < 0) {
                daemon_log(LOG_ERR, "Failed to create pipe.");
                return 1;
            }


            /* Do the fork */
            if ((pid = daemon_fork()) < 0) {
                /* Exit on error */
                daemon_retval_done();
                return 1;
            } else if (pid) { /* The parent */
                int ret;
                /* Wait for 20 seconds for the return value passed from the daemon process */
                if ((ret = daemon_retval_wait(20)) < 0) {
                    daemon_log(LOG_ERR, "Could not receive return value from daemon process: %s", strerror(errno));
                    return 255;
                }
                daemon_log(ret != 0 ? LOG_ERR : LOG_DEBUG, "Daemon returned %i as return value.", ret);
                return 0;
            } else { /* The daemon */
                int fd, quit = 0;
                fd_set fds;

                daemon_log(LOG_INFO, "MICO daemon %s starting up ...", name.c_str());


                Daemon* d;
                struct passwd* pwentry;

                /* Close FDs */
                if (daemon_close_all(-1) < 0) {
                    daemon_log(LOG_ERR, "Failed to close all file descriptors: %s", strerror(errno));
                    /* Send the error condition to the parent process */
                    daemon_retval_send(1);
                    goto finish;
                }
                /* Create the PID file */
                if (daemon_pid_file_create() < 0) {
                    daemon_log(LOG_ERR, "Could not create PID file (%s).", strerror(errno));
                    daemon_retval_send(2);
                    goto finish;
                }
                /* Initialize signal handling */
                if (daemon_signal_init(SIGINT, SIGTERM, SIGQUIT, SIGHUP, 0) < 0) {
                    daemon_log(LOG_ERR, "Could not register signal handlers (%s).", strerror(errno));
                    daemon_retval_send(3);
                    goto finish;
                }

                // change privileges
                pwentry = getpwnam(_user);
                if(pwentry == NULL) {
                    daemon_log(LOG_ERR, "Could not resolve ID of mico user (user=%s, error=%s)", _user, strerror(errno));
                    daemon_retval_send(4);
                    goto finish;
                }
                setuid(pwentry->pw_uid);

                // change working directory to /tmp
                chdir(P_tmpdir);

                /* Send OK to parent process */
                daemon_retval_send(0);
                daemon_log(LOG_INFO, "MICO daemon %s sucessfully started", name.c_str());


                d = new Daemon(name,server,user,password, svcs);
                d->start();

                /* Prepare for select() on the signal fd */
                FD_ZERO(&fds);
                fd = daemon_signal_fd();
                FD_SET(fd, &fds);
                while (!quit) {
                    fd_set fds2 = fds;
                    /* Wait for an incoming signal */
                    if (select(FD_SETSIZE, &fds2, 0, 0, 0) < 0) {
                        /* If we've been interrupted by an incoming signal, continue */
                        if (errno == EINTR)
                            continue;
                        daemon_log(LOG_ERR, "select(): %s", strerror(errno));
                        break;
                    }
                    /* Check if a signal has been received */
                    if (FD_ISSET(fd, &fds2)) {
                        daemon_log(LOG_INFO, "checking interupt signal...");
                        int sig;
                        /* Get signal */
                        if ((sig = daemon_signal_next()) <= 0) {
                            daemon_log(LOG_ERR, "daemon_signal_next() failed: %s", strerror(errno));
                            break;
                        }
                        daemon_log(LOG_INFO, "received signal is %d", sig);
                        /* Dispatch signal */
                        switch (sig) {
                            case SIGINT:
                            case SIGQUIT:
                            case SIGTERM:
                            case SIGHUP:
                                daemon_log(LOG_WARNING, "Got SIGINT, SIGQUIT or SIGTERM.");
                                quit = 1;
                                break;
                        }
                    }

                }
                daemon_log(LOG_INFO, "gracefull shutting down extractor.");
                d->stop();
                delete d;
                daemon_log(LOG_INFO, "MICO analysis services unregistered");

                finish:

                daemon_retval_send(255);
                daemon_signal_done();
                daemon_pid_file_remove();

                daemon_log(LOG_INFO, "MICO daemon %s stopped", name.c_str());

                return 0;
            }
        }

        int stop(const char* name) {
            int ret;

            /* Reset signal handlers */
            if (daemon_reset_sigs(-1) < 0) {
                daemon_log(LOG_ERR, "Failed to reset all signal handlers: %s", strerror(errno));
                return 1;
            }
            /* Unblock signals */
            if (daemon_unblock_sigs(-1) < 0) {
                daemon_log(LOG_ERR, "Failed to unblock all signals: %s", strerror(errno));
                return 1;
            }
            /* Set indetification string for the daemon for both syslog and PID file */
            char* _name= strdup(name);
            daemon_pid_file_ident = daemon_log_ident = daemon_ident_from_argv0(_name);

            /* Kill daemon with SIGTERM */
            /* Check if the new function daemon_pid_file_kill_wait() is available, if it is, use it. */
            if ((ret = daemon_pid_file_kill_wait(SIGTERM, 5)) < 0)
                daemon_log(LOG_WARNING, "Failed to kill daemon: %s", strerror(errno));


            free(_name);

            return ret < 0 ? 1 : 0;
        }


    }
}
