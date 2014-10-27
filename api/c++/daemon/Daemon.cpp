#include <signal.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>

#include <cstdlib>

#include <libdaemon/dfork.h>
#include <libdaemon/dsignal.h>
#include <libdaemon/dlog.h>
#include <libdaemon/dpid.h>

#include "Daemon.hpp"

namespace mico {
    namespace daemon {


        using mico::event::AnalysisService;

        Daemon::Daemon(const char* name, const char *server, const char *user, const char *password, std::initializer_list<mico::event::AnalysisService*> svcs)
                : name(name), eventManager(server,user,password) {

            for (AnalysisService *s : svcs) {
                services.push_back(s);
            }
        }

        Daemon::Daemon(const char* name, const char *server, const char *user, const char *password, std::vector<mico::event::AnalysisService*> svcs)
                : name(name), eventManager(server,user,password), services(svcs) {
        }

        Daemon::~Daemon() {
            for (AnalysisService *s : services) {
                delete s;
            }
        }

        int Daemon::start() {
            daemon_log(LOG_INFO, "starting daemon and registering services ...");


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
            char* _name= strdup(name);
            daemon_pid_file_ident = daemon_log_ident = daemon_ident_from_argv0(_name);


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
                daemon_log(ret != 0 ? LOG_ERR : LOG_INFO, "Daemon returned %i as return value.", ret);
                return ret;
            } else { /* The daemon */
                int fd, quit = 0;
                fd_set fds;
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


                for (AnalysisService *s : services) {
                    eventManager.registerService(s);
                }

                /* Send OK to parent process */
                daemon_retval_send(0);
                daemon_log(LOG_INFO, "Sucessfully started");


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
                    /* Check if a signal has been recieved */
                    if (FD_ISSET(fd, &fds2)) {
                        int sig;
                        /* Get signal */
                        if ((sig = daemon_signal_next()) <= 0) {
                            daemon_log(LOG_ERR, "daemon_signal_next() failed: %s", strerror(errno));
                            break;
                        }
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

                for (AnalysisService *s : services) {
                    eventManager.unregisterService(s);
                }
                daemon_log(LOG_INFO, "MICO analysis services unregistered");

            finish:

                daemon_retval_send(255);
                daemon_signal_done();
                daemon_pid_file_remove();

                daemon_log(LOG_INFO, "MICO daemon %s stopped", name);

                free(_name);
                return 0;
            }
        }

        int Daemon::stop() {
            int ret;

            /* Kill daemon with SIGTERM */
            /* Check if the new function daemon_pid_file_kill_wait() is available, if it is, use it. */
            if ((ret = daemon_pid_file_kill_wait(SIGTERM, 5)) < 0)
                daemon_log(LOG_WARNING, "Failed to kill daemon: %s", strerror(errno));

            return ret;
        }
    }
}