#ifndef HAVE_LOGGING_HPP
#define HAVE_LOGGING_HPP 1

#include <stdarg.h>
#include <stdio.h>


#ifdef DEBUG_BUILD
   #define DEFAULT_LOG_LEVEL DEBUG
#else
   #define DEFAULT_LOG_LEVEL INFO
#endif

/*
 * Simple logging implementation that allows defining different backends
 */
namespace mico {
    namespace log {

        enum LoggingLevel {
            DEBUG, INFO, WARN, ERROR
        };

        /**
        * A backend used for logging messages. Defines simmple vararg forwarding methods to log on the most common
        * log levels. Subclasses only need to override the log method, which takes the level, a format string, and
        * format arguments.
        */
        class LoggingBackend {

        private:
            LoggingLevel level;


        public:


            LoggingBackend() : level(DEFAULT_LOG_LEVEL) {
            }

            virtual ~LoggingBackend() {};
/**
            * Set the maximum log level. Any messages below this level will be discarded.
            */
            void setLevel(LoggingLevel level) { this->level = level; };

            LoggingLevel getLevel() const { return level; };


            /**
            * Log a debug level message (formatted string) with optional format arguments.
            */
            inline void log_debug(const char* message, ...) {
                if(level <= DEBUG) {
                    va_list args;
                    va_start(args,message);
                    log(DEBUG, message, args);
                    va_end(args);
                }
            };

            /**
            * Log a info level message (formatted string) with optional format arguments.
            */
            inline void log_info(const char* message,  ...) {
                if(level <= INFO) {
                    va_list args;
                    va_start(args,message);
                    log(INFO, message, args);
                    va_end(args);
                }
            };

            /**
            * Log a warn level message (formatted string) with optional format arguments.
            */
            inline void log_warn(const char* message,  ...) {
                if(level <= WARN) {
                    va_list args;
                    va_start(args,message);
                    log(WARN, message, args);
                    va_end(args);
                }
            };

            /**
            * Log a error level message (formatted string) with optional format arguments.
            */
            inline void log_error(const char* message, ...) {
                if(level <= ERROR) {
                    va_list args;
                    va_start(args,message);
                    log(ERROR, message, args);
                    va_end(args);
                }
            };

            /**
            * Log a message (formatted string) with optional format arguments on the given level. This method
            * needs to be implemented by subclasses.
            */
            virtual void log(LoggingLevel level, const char* message, va_list args) = 0;

        };


        /**
        * Simple logging backend implementation logging to stdout.
        */
        class StdOutBackend : public LoggingBackend {

        public:

            virtual void log(LoggingLevel level, const char* message, va_list args);

        };


        extern LoggingBackend* backend;


        /**
        * Configure a different logging backend.
        */
        void set_log_backend(LoggingBackend* b);

        /**
        * Configure a different logging level.
        */
        void set_log_level(LoggingLevel l);
    }
}

#define LOG_DEBUG(...) if(mico::log::backend !=0) mico::log::backend->log_debug(__VA_ARGS__)
#define LOG_INFO(...)  if(mico::log::backend !=0) mico::log::backend->log_info(__VA_ARGS__)
#define LOG_WARN(...)  if(mico::log::backend !=0) mico::log::backend->log_warn(__VA_ARGS__)
#define LOG_ERROR(...) if(mico::log::backend !=0) mico::log::backend->log_error(__VA_ARGS__)

#endif
