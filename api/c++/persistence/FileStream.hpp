#ifndef HAVE_FILE_STREAM_H
#define HAVE_FILE_STREAM_H 1

#include <fstream>

namespace mico {
    namespace io {

        /*
        * Provides stream for reading local files.
        */
        class FileIStream : public std::filebuf {
        public:
            FileIStream(const char* path);
        };

        /*
        * Provides stream for writing local files.
        */
        class FileOStream : public std::filebuf {
        public:
            FileOStream(const char* path);
        };

        /*
        * Internal helper functions.
        */
        namespace {
            static void mkdirs(const char *_path);
        }

    }
}

#endif