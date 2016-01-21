#ifndef HAVE_URL_STREAM_H
#define HAVE_URL_STREAM_H 1

#include <istream>
#include <ostream>
#include <streambuf>

namespace mico {
    namespace io {

        /*
        * Internal data types and functions.
        */
        namespace {
            enum URLType {
                URL_TYPE_UNKNOWN = 0,
                URL_TYPE_FILE =    1,
                URL_TYPE_HTTP =    2,
                URL_TYPE_FTP  =    3,
                URL_TYPE_HDFS =    4
            };

            /*
            * Parsed URL components
            *
            * @param port The port number. 0 means not specified.
            */
            struct url_components {
                std::string scheme;
                std::string host;
                uint16_t port;
                std::string path;
                std::string username;
                std::string password;
            };

            /*
            * Takes an URL or absolute path (with UNIX style delimiters) to parse it. It does no validation of the
            * URL/path, therefore the result of malformed (or maybe even unusual) input can lead to incorrect results.
            *
            * @param url The URL string to parse
            * @return The identified URL components. If everything is empty (and port == 0) the URL could not be parsed.
            */
            static url_components getURLComponents(const char* url);

            /*
            * Returns the URL type.
            *
            * @param url The parsed URL.
            * @return URL type.
            */
            static URLType getType(url_components url);

            /*
            * Removes double occurrences of slashes from string
            *
            * @param path The path to fix
            * @return Fixed path
            */
            static std::string fixPath(const std::string& path);
        }

        /**
        * Main type for opening an output stream to an URL for writing. Use url_ostream(URL) to open a
        * new stream, and normal stream operators for sending data (i.e.  \<\<).
        */
        class url_ostream : public std::ostream {
        public:
            url_ostream(const char* url);
            url_ostream(std::string url) : url_ostream(url.c_str()) {};
            ~url_ostream();

        private:
            static std::streambuf* getStreamHandler(const char* url);
        };


        /**
        * Main type for opening an input stream to an URL for reading. Use url_istream(URL) to open a
        * new stream, and normal stream operators for receiving data (i.e. \>\>).
        */
        class url_istream : public std::istream {
        public:
            url_istream(const char* url);
            url_istream(std::string url) : url_istream(url.c_str()) {};
            ~url_istream();

        private:
            static std::streambuf* getStreamHandler(const char* url);
        };

        /**
        * Remove file from storage.
        * TODO: Also remove empty directories
        */
        int remove(const char* url);
        int remove(std::string url);
    }
}

#endif

