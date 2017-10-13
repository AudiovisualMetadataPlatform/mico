/**
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

#include <cstring>
#include <libgen.h>
#include "HDFSStream.hpp"

namespace mico {
    namespace io {
        hdfsFS createHdfsFileSystem(const char *host, uint16_t port){
            
            //Set Hostname and port of the HDFS name node
            struct hdfsBuilder *builder = hdfsNewBuilder();
            
            if (port == 0) {
                port = HDFS_DEFAULT_PORT;
            }
            std::string nameNodeHostName = "hdfs://" + std::string(host) + ":" + std::to_string(port);
            hdfsBuilderConfSetStr(builder, "fs.defaultFS", nameNodeHostName.c_str());
            hdfsBuilderSetNameNode(builder, nameNodeHostName.c_str());

            //Resolve slave data nodes based on the hostname they are bound to,
            //not on ther IPs (which could be part of an unreachable private network)
            hdfsBuilderConfSetStr(builder, "dfs.client.use.datanode.hostname", "true");

            //always read data from the websocket provided by the data node
            hdfsBuilderConfSetStr(builder, "dfs.client.read.shortcircuit", "false");

            hdfsBuilderSetForceNewInstance(builder);
	    hdfsFS fs = hdfsBuilderConnect(builder);
            hdfsFreeBuilder(builder);
            return fs;
        }
    }
}

namespace mico {
    namespace io {
        HDFSStreambuf::HDFSStreambuf(const char* path, FileMode mode, const char* host, uint16_t port, int bufsize)
            : buffer_size(bufsize)
        {
            buffer = (char*)malloc(buffer_size * sizeof(char));
            if (buffer == NULL) {
                return;
            }
            
            fs=createHdfsFileSystem(host,port);

            //Open file
            switch(mode) {
                case FILE_MODE_READ:
                    file = hdfsOpenFile(fs, path, O_RDONLY, 0, 0, 0);
                    break;
                case FILE_MODE_WRITE:
                    char* path_dup = strdup(path);
                    char* dir = dirname(path_dup);
                    if(hdfsExists(fs, dir) != 0) {
                        if(hdfsCreateDirectory(fs, dir) != 0) {
                            throw std::string("Error creating directory: ") + dir;
                        }
                    }
                    free(path_dup);
                    file = hdfsOpenFile(fs, path, O_WRONLY, 0, 0, 0);
                    break;
            }
        }

        HDFSStreambuf::~HDFSStreambuf() {
            //HDFS: Close file and disconnect.
            if (fs != NULL) {
                if (file != NULL) {
                    hdfsCloseFile(fs, file);
                }
                hdfsDisconnect(fs);
            }

            //Free buffer.
            if (buffer != NULL) {
                free(buffer);
            }

        }

        HDFSIStream::HDFSIStream(const char* path, const char* address, uint16_t port)
                : HDFSStreambuf(path, FILE_MODE_READ, address, port)
        {
            if (buffer != NULL) {
                //Set buffer pointers to mark buffer as empty. That will cause a call of underflow() on the first stream
                //read request.
                setg(buffer, buffer + buffer_size, buffer + buffer_size);

                //Retrieve the file size, used for seek operations.
                hdfsFileInfo* info = hdfsGetPathInfo(fs, path);
                if (info != NULL) {
                    file_size = info->mSize;
                    hdfsFreeFileInfo(info, 1);
                }
            }
        };

        std::streambuf::int_type HDFSIStream::underflow() {
            //If buffer is not empty return next byte.
            if (gptr() < egptr()) {
                return traits_type::to_int_type(*gptr());
            }

            //Fill buffer with data from HDFS file.
            int length = hdfsRead(fs, file, buffer, buffer_size);
            if (length == 0) {
                //We are at the end of the file.
                return traits_type::eof();
            } else if (length == -1) {
                //Read error
                /* hdfsRead:
                *  On error, -1.  Errno will be set to the error code.
                *  Just like the POSIX read function, hdfsRead will return -1
                *  and set errno to EINTR if data is temporarily unavailable,
                *  but we are not yet at the end of the file.
                */
                return length;
            }

            //Set buffer pointer to indicate full (or the number of bytes that have been red actually) buffer.
            setg(buffer, buffer, buffer + length);
            return traits_type::to_int_type(*gptr());
        }

        std::streampos HDFSIStream::seekoff(std::streamoff off, std::ios_base::seekdir way, std::ios_base::openmode which) {
            if (which != std::ios_base::in || file_size < 0)
                return -1;

            std::streamoff position = -1;

            switch (way) {
                //Offset is relative to the beginning of the file, so it is the absolute position.
                case std::ios_base::beg:
                    position = off;
                    break;
                //Offset is relative to the current file position.
                case std::ios_base::cur:
                    position = hdfsTell(fs, file) + off;
                    break;
                //Offset is relative to the end of the file and therefore should be negative.
                case std::ios_base::end:
                    position = file_size + position;
                    break;
            }

            if (position >= 0 && position <= file_size) {
                if (hdfsSeek(fs, file, position) == 0) {
                    //Invalidate buffer
                    setg(buffer, buffer + buffer_size, buffer + buffer_size);
                    return position;
                }
            }

            return -1;
        }

        std::streampos HDFSIStream::seekpos(std::streampos pos, std::ios_base::openmode which) {
            return seekoff(pos, std::ios_base::beg, which);
        }

        HDFSOStream::HDFSOStream(const char* path, const char* address, uint16_t port)
                : HDFSStreambuf(path, FILE_MODE_WRITE, address, port)
        {
            if (buffer != NULL) {
                //Set buffer pointers to mark buffer as empty.
                setp(buffer, buffer + buffer_size);
            }
        };

        std::streambuf::int_type HDFSOStream::overflow(std::streambuf::int_type c) {
            //Write buffer content to file.
            if(writeBuffer() == 0) {
                if (!traits_type::eq_int_type(c, traits_type::eof())) {
                    //Push c as first byte on the buffer.
                    sputc(c);
                }
                return traits_type::not_eof(c);
            }
            return traits_type::eof();
        }

        int HDFSOStream::sync() {
            //Write buffer content to file.
            if (writeBuffer() != 0) return -1;
            //Also flush HDFS buffers.
            return hdfsSync(fs, file);
        }


        int HDFSOStream::writeBuffer() {
            int write = pptr() - pbase();
            if (write) {
                int written = hdfsWrite(fs, file, buffer, write);
                if (write != written) {
                    return -1;
                }
            }
            //Set buffer pointers to mark buffer as empty.
            setp(buffer, buffer + buffer_size);
            return 0;
        }

        int removeHdfsFile(const char* path, const char* host, uint16_t port) {
            //Connect to HDFS
            hdfsFS fs = createHdfsFileSystem(host,port);

            int status = hdfsDelete(fs, path, 0);

            //HDFS: disconnect.
            if (fs != NULL) {
                hdfsDisconnect(fs);
            }

            return status;
        }

        int removeHdfsFile(const char* path) {
            return removeHdfsFile(path, HDFS_DEFAULT_ADDRESS, HDFS_DEFAULT_PORT);
        }
    }
}





