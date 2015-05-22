#ifndef HAVE_WEB_STREAM_H
#define HAVE_WEB_STREAM_H 1

#include <cstdio>
#include <iosfwd>
#include <streambuf>
#include <istream>
#include <ostream>

#include <fcntl.h>
#include <curl/curl.h>



namespace mico
{
    namespace io
    {

        /**
        * Type of stream managed by the URLDevice. Internal Use Only.
        */
        enum URLMode {
            URL_MODE_READ, URL_MODE_WRITE
        };


        /**
        * Base class for Boost Device implementations allowing to access remote (%ftp://, %http://, %https://)
        * URLs as stdio streams, similar to fstream. Remote files are partly buffered in main memory.
        *
        * The class is implemented using the Boost IOStreams library and uses the cURL library in the
        * background to a access remote files.
        */
        class WebStreambufBase : public std::streambuf
        {

            // cURL callbacks; declared here so we can friend them
            static size_t read_callback(void *ptr, size_t size, size_t nmemb, void *device);
            static size_t write_callback(void *ptr, size_t size, size_t nmemm, void *device);

        protected:

            typedef union {
                CURL *curl;
                FILE *file;
            } handle_t;

            URLMode mode;

            handle_t handle;

            CURLM *multi_handle;

            int running_handles;

            char* buffer;           //!< internal buffer for storing data from last read
            int buffer_size;        //!< allocated buffer size
            char* buffer_position;  //!< cURL position in buffer (when sending/receiving data)

            bool finishing;         //!< indicate if device is already finishing transfer (i.e. no more reads/writes)
            bool waiting;           //!< indicate if the device is still waiting for more data


            void loop();            //!< loop to fetch/send more data



        public:

            /**
            * Open URL device using the given URL and flags. Uses cURL internally to access the server.
            *
            * @param url        the full URL to the file (either starting with %http://, %https:// or %ftp://)
            * @param mode       open mode, like for fopen; supported modes: read, write
            */
            WebStreambufBase(const char* url, URLMode mode, int bufsize);


            /**
            * Clean up resources occupied by device, e.g. file handles and connections.
            */
            virtual ~WebStreambufBase();


        private:

            /**
            * Copy constructor not implemented, copying not allowed.
            */
            WebStreambufBase(const WebStreambufBase & other);


            /**
            * Copy assignment operator not implemented, copying not allowed.
            */
            WebStreambufBase & operator=(WebStreambufBase other);

        };


        /**
        * A Boost Device implementation allowing read access.
        */
        class WebIStreambuf : public WebStreambufBase {

        public:

            WebIStreambuf(const char* url) : WebStreambufBase(url, URL_MODE_READ, CURL_MAX_WRITE_SIZE) {};


        private:

            /**
            * Underflow, so we need to fill the buffer again with more data.
            */
            std::streambuf::int_type underflow();

            /**
            * Copy constructor not implemented, copying not allowed
            */
            WebIStreambuf(const WebIStreambuf & other);


            /**
            * Copy assignment operator not implemented, copying not allowed
            */
            WebIStreambuf & operator=(WebIStreambuf other);

        };


        /**
        * A Boost Device implementation allowing write access.
        */
        class WebOStreambuf : public WebStreambufBase {

        public:

            WebOStreambuf(const char* url) : WebStreambufBase(url, URL_MODE_WRITE, CURL_MAX_WRITE_SIZE) {};

        private:

            /**
            * Buffer overflow, so we need to write out the buffer to the URL connection.
            */
            std::streambuf::int_type overflow(std::streambuf::int_type c);

            /**
            * Explicit call to write out the buffer to the URL connection even when it is not full
            */
            int sync();

            /**
            * Copy constructor not implemented, copying not allowed
            */
            WebOStreambuf(const WebOStreambuf & other);


            /**
            * Copy assignment operator not implemented, copying not allowed
            */
            WebOStreambuf & operator=(WebOStreambuf other);

        };

        /**
        * Main type for opening an output stream to an URL for writing. Use url_ostream(URL) to open a
        * new stream, and normal stream operators for sending data (i.e.  \<\<).
        */
        class web_ostream : public std::ostream {
        public:
            web_ostream(const char* url) : std::ostream(new WebOStreambuf(url)) {};

            web_ostream(std::string url) : std::ostream(new WebOStreambuf(url.c_str())) {};

            ~web_ostream() { rdbuf()->pubsync(); delete rdbuf(); };
        };


        /**
        * Main type for opening an input stream to an URL for reading. Use url_istream(URL) to open a
        * new stream, and normal stream operators for receiving data (i.e. \>\>).
        */
        class web_istream : public std::istream {
        public:
            web_istream(const char* url) : std::istream(new WebIStreambuf(url)) {};

            web_istream(std::string url) : std::istream(new WebIStreambuf(url.c_str())) {};

            ~web_istream() { delete rdbuf(); };
        };

        int removeFtpFile(const char* url);

        namespace {
            static size_t write_callback(void *ptr, size_t size, size_t nmemb, void *_device);
        }
    }
}

#endif
