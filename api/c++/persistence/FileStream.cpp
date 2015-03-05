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
#include <unistd.h>
#include <sys/stat.h>
#include "Logging.hpp"

#include "FileStream.hpp"

namespace mico {
    namespace io{
        FileIStream::FileIStream(const char *path) {
            open(path, std::ios::in);
            //close gets called by the destructor of the parent class.
        }

        FileOStream::FileOStream(const char *path) {
            mkdirs(path);
            open(path, std::ios::out);
            //close gets called by the destructor of the parent class.
        }

        namespace {
            /*
            * Create all directories in the path excluding the last (which is a file).
            */
                static void mkdirs(const char*_path) {
                char
                        * path = strdup(_path); // temporary buffer which we can modify
                char *last = 0, *cur = path;

                if(*path == '/' && chdir("/") != 0) {
                    LOG_ERROR("could not change into root directory, exiting!");
                    exit(1);
                }
                while(*cur) {
                    if(*cur == '/') {
                        *cur = '\0';

                        // create directory
                        if(last) {
                            if(chdir(last) != 0) {
                                if(mkdir(last,S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH) != 0) {
                                    LOG_ERROR("could not create directory %s, exiting!", _path);
                                    exit(1);
                                }
                                if(chdir(last) != 0) {
                                    LOG_ERROR("could not change into created directory %s, exiting!", _path);
                                    exit(1);
                                }
                            }
                        }
                        last = cur+1;
                    }
                    cur++;
                }

                free(path);
            }
        }
    }
}