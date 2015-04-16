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

#include <cstdlib>
#include <fstream>
#include <iostream>
#include <regex>
#include "FileStream.hpp"
#include "HDFSStream.hpp"
#include "WebStream.hpp"

#include "URLStream.hpp"


namespace mico {
    namespace io {


        url_ostream::url_ostream(const char* url) : std::ostream(getStreamHandler(url)) {};

        url_ostream::~url_ostream() {
            if (rdbuf() != NULL) {
                rdbuf()->pubsync();
                delete rdbuf();
            }
        }

        std::streambuf* url_ostream::getStreamHandler(const char* url) {
            url_components url_parsed = getURLComponents(url);
            URLType url_type = getType(url_parsed);

            switch (url_type) {
                case URL_TYPE_FILE:
                    return new FileOStream(url_parsed.path.c_str());
                case URL_TYPE_HTTP:
                case URL_TYPE_FTP:
                    return new WebOStreambuf(url);
                case URL_TYPE_HDFS:
                    return new HDFSOStream(url_parsed.path.c_str(), url_parsed.host.c_str(), url_parsed.port);
            }
            throw std::string("Invalid URL: ") + url;
        }

        url_istream::url_istream(const char *url) : std::istream(getStreamHandler(url)) {}

        url_istream::~url_istream() {
            if (rdbuf() != NULL) {
                delete rdbuf();
            }
        }

        std::streambuf* url_istream::getStreamHandler(const char *url) {
            url_components url_parsed = getURLComponents(url);
            URLType url_type = getType(url_parsed);

            switch (url_type) {
                case URL_TYPE_FILE:
                    return new FileIStream(url_parsed.path.c_str());
                case URL_TYPE_HTTP:
                case URL_TYPE_FTP:
                    return new WebIStreambuf(url);
                case URL_TYPE_HDFS:
                    return new HDFSIStream(url_parsed.path.c_str(), url_parsed.host.c_str(), url_parsed.port);
            }
            throw std::string("Invalid URL: ") + url;
        }


        namespace {
            URLType getType(url_components url) {
                std::string scheme = url.scheme;
                std::transform(scheme.begin(), scheme.end(), scheme.begin(), ::tolower);
                if (scheme == "http" || scheme == "https") {
                    return URL_TYPE_HTTP;
                } else if (scheme == "ftp") {
                    return URL_TYPE_FTP;
                } else if (scheme == "file" || (scheme == "" && url.path != "")) {
                    return URL_TYPE_FILE;
                } else if (scheme == "hdfs") {
                    return URL_TYPE_HDFS;
                }
                return URL_TYPE_UNKNOWN;
            }

            url_components getURLComponents(const char* url) {
                std::string url_s = std::string(url);
                /*
                * Examples the regexp can handle:
                *  /path/to/file
                *  file:///path/to/file
                *  ftp://127.0.0.1/
                *  ftp://localhost/path/to/file
                *  ftp://domain.com/path/to/file
                *  hdfs://localhost:9000/path/to/file
                *  hdfs://user:pwd@localhost:9000/path/to/file
                *  hdfs://user:pwd@localhost/path/to/file
                *  hdfs://user@localhost/path/to/file
                */
                /*
                * Regex groups:
                * 1:   scheme: ([[:alpha:]][[:alnum:]\+\.-]*?)://
                * 2,3: username and password: (?:([[:alnum:]]+?)(?::([[:alnum:]]+?))?@)?
                * 4:   domain/ip: ([\.[:alnum:]]+?)
                * 5:   port: (?::([[:digit:]]+?))?
                * 6:   path (including query and fragments: (/.*)
                */
                std::regex uri_pattern("^(?:([[:alpha:]][[:alnum:]\\+\\.-]*?)://(?:(?:([[:alnum:]]+?)(?::([[:alnum:]]+?))?@)?([\\.[:alnum:]]+?)(?::([[:digit:]]+?))?)?)?(/.*)");
                std::smatch result;
                std::regex_search(url_s, result, uri_pattern);

                url_components retval;
                retval.scheme = result[1].str();
                retval.username = result[2].str();
                retval.password = result[3].str();
                retval.host = result[4].str();
                retval.port = std::strtol(result[5].str().c_str(), NULL, 0);
                retval.path = result[6].str();

                /*for (int i = 0; i < result.length(); i++) {
                    std::cout << "[" << i << "]:" << result[i].str() << "|" << std::endl;
                }
                std::cout << "Prefix:" << result.prefix().str() << "|" << std::endl;
                std::cout << "Suffix:" << result.suffix().str() << "|" << std::endl;
                */

                return retval;
            }
        }

        int remove(const char* url) {
            url_components url_parsed = getURLComponents(url);
            URLType url_type = getType(url_parsed);

            switch (url_type) {
                case URL_TYPE_FILE:
                    return removeLocalFile(url_parsed.path.c_str());
                case URL_TYPE_HTTP:
                    throw std::string("remove not supported for HTTP(S): ") + url;
                case URL_TYPE_FTP:
                    return removeFtpFile(url_parsed.path.c_str());
                case URL_TYPE_HDFS:
                    return removeHdfsFile(url_parsed.path.c_str());
            }
            throw std::string("Invalid URL: ") + url;
        }

        int remove(std::string url) {
            return remove(url.c_str());
        }
    }
}