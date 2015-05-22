#ifndef HAVE_HDFS_STREAM_H
#define HAVE_HDFS_STREAM_H 1

#include <istream>
#include <ostream>
#include <streambuf>
#include <hdfs/hdfs.h>

#define HDFS_DEFAULT_ADDRESS "localhost"
#define HDFS_DEFAULT_PORT 8020
#define DEFAULT_BUFFER_SIZE 128 * 1024


namespace mico
{
    namespace io
    {
        /**
        * File mode. for internal use only.
        */
        enum FileMode {
            FILE_MODE_READ, FILE_MODE_WRITE
        };

        /*
        * This is template class for HDFS input and output streams and not for direct use.
        * In-/Output is buffered in memory. Depending on the use case, the default buffer size might be suboptimal.
        *
        * HDFS access is managed with libhdfs3 developed by Pivotal.
        */
        class HDFSStreambuf : public std::streambuf {

        public:
            virtual ~HDFSStreambuf();

        protected:
            /*
            * Constructor sets up the buffer and opens the given file on the HDFS server.
            *
            * @param path Path including file name.
            * @param mode If file is opened for reading (FILE_MODE_READ) or writing (FILE_MODE_WRITE).
            * @param address Address of the HDFS name node.
            * @param port Port of the HDFS RPC service.
            */
            HDFSStreambuf(const char* path, FileMode mode, const char* address, uint16_t port, int bufsize = DEFAULT_BUFFER_SIZE);

            char* buffer;    //stream buffer
            int buffer_size; //stream buffer size

            hdfsFS fs = NULL;     //HDFS connection handle
            hdfsFile file = NULL; //HDFS file handle

        private:
            /*
            * Copying not allowed, therefore copy constructor is not implemented.
            */
            HDFSStreambuf(const HDFSStreambuf& other);

            /*
            * Copying not allowed, therefore copy assignment constructor is not implemented.
            */
            HDFSStreambuf& operator=(HDFSStreambuf other);

       };

        /*
        * HDFSIStream provides the specific functionality for the HDFS input stream.
        */
        class HDFSIStream : public HDFSStreambuf {

        public:
            /*
            * Calls HDFSStreambuf constructor and sets up all input stream relevant things.
            *
            * @param path Path including file name.
            * @param address Address of the HDFS name node.
            * @param port Port of the HDFS RPC service.
            */
            HDFSIStream(const char* path, const char* address, uint16_t port);

        private:
            /*
            * Copying not allowed, therefore copy constructor is not implemented.
            */
            HDFSIStream(const HDFSIStream& other);

            /*
            * Copying not allowed, therefore copy assignment constructor is not implemented.
            */
            HDFSIStream& operator=(HDFSIStream other);

            /*
            * Retrieves data from the HDFS file if the buffer is empty.
            *
            * @return Hands back the next byte of the stream, or end of file (traits_type::eof()), or -1 on read error.
            */
            std::streambuf::int_type underflow();

            /*
            * Implements seeking capabilities.
            *
            * @return The new absolute stream position or -1 on error.
            */
            std::streampos seekoff(std::streamoff off, std::ios_base::seekdir way, std::ios_base::openmode which = std::ios_base::in);

            /*
            * Implements seeking capabilities.
            *
            * @return The new absolute stream position or -1 on error.
            */
            std::streampos seekpos(std::streampos pos, std::ios_base::openmode which = std::ios_base::in);

            /*
            * The file size is needed to support relative seeking for way is std::ios_base::end.
            */
            std::streamsize file_size = -1;
        };

        /*
        * HDFSOStream provides the specific functionality for the HDFS output stream.
        */
        class HDFSOStream : public HDFSStreambuf {

        public:
            /*
            * Calls HDFSStreambuf constructor and sets up all output stream relevant things.
            *
            * @param path Path including file name.
            * @param address Address of the HDFS name node.
            * @param port Port of the HDFS RPC service.
            */
            HDFSOStream(const char* path, const char* address, uint16_t port);

        private:
            /*
            * Copying not allowed, therefore copy constructor is not implemented.
            */
            HDFSOStream(const HDFSOStream& other);

            /*
            * Copying not allowed, therefore copy assignment constructor is not implemented.
            */
            HDFSOStream& operator=(HDFSOStream other);

            /*
            * Calls the HDFS write function to flush the buffer and pushes c on the buffer.
            *
            * @return c on success, traits_type::eof() on error.
            */
            std::streambuf::int_type overflow(std::streambuf::int_type c);

            /*
            * Flushes the buffer.
            *
            * @return 0 on success, -1 on error
            */
            int sync();

            /*
            * Calles the HDFS write function to flush the buffer and reset buffer pointers. Helper for overflow(...)
            * and sync().
            *
            * @return 0 on success, -1 on failure;
            */
            int writeBuffer();
        };



        /*
        * Main type for opening an input stream to a HDFS file for reading.
        * Use hdfs_istream(path, name node address, RPC port) to open a new stream. Stream supports seeking.
        */
        class hdfs_istream : public std::istream {
        public:
            hdfs_istream(const char* path) : hdfs_istream(path, HDFS_DEFAULT_ADDRESS, HDFS_DEFAULT_PORT) {};
            hdfs_istream(std::string path) : hdfs_istream(path.c_str()) {};
            hdfs_istream(const char* path, const char* address, uint16_t port) : std::istream(new HDFSIStream(path, address, port)) {};
            hdfs_istream(std::string path, std::string address, uint16_t port) : hdfs_istream(path.c_str(), address.c_str(), port) {};
            ~hdfs_istream() { delete rdbuf(); };
        };

        /*
        * Main type for opening an output stream to a HDFS file for writing.
        * Use hdfs_ostream(path, name node address, RPC port) to open a new stream. Stream does not support seeking.
        */
        class hdfs_ostream : public std::ostream {
        public:
            hdfs_ostream(const char* path) : hdfs_ostream(path, HDFS_DEFAULT_ADDRESS, HDFS_DEFAULT_PORT) {};
            hdfs_ostream(std::string path) : hdfs_ostream(path.c_str()) {};
            hdfs_ostream(const char* path, const char* address, uint16_t port) : std::ostream(new HDFSOStream(path, address, port)) {};
            hdfs_ostream(std::string path, std::string address, uint16_t port) : hdfs_ostream(path.c_str(), address.c_str(), port) {};
            ~hdfs_ostream() { rdbuf()->pubsync(); delete rdbuf(); };
        };

        int removeHdfsFile(const char* path, const char* address, uint16_t port);
        int removeHdfsFile(const char* path);
    }
}

#endif