#include <curl/curl.h>
#include <cstdlib>
#include <cstring>
#include <cctype>

#include "http_client.hpp"

using std::string;

namespace mico {
    namespace http {


        Body::Body(const std::string& data, const std::string& content_type) {
            ptr     = (char*)malloc((data.size()+1) * sizeof(char));
            memcpy(ptr, (const void*)data.c_str(), (data.size()+1) * sizeof(char));

            length  = data.size() * sizeof(char);
            type    = content_type;
            managed = true;
        }

        Body::~Body() {
            if(managed && ptr) {
                free(ptr);
            }
        }


        Message::~Message() {
            if(body) delete body;
        }

        /**
        * Return the headers in CURL format
        */
        curl_slist* Message::getCurlHeaders() {
            curl_slist* cheaders = NULL;
            for(auto it : headers) {
                string entry = it.first + ": " + it.second;
                cheaders = curl_slist_append(cheaders,entry.c_str());
            }
            return cheaders;
        }

        /**
        * Set the headers in CURL format
        */
        void Message::setCurlHeaders(curl_slist* hdr) {
            while(hdr) {
                char* cpos = strchr(hdr->data, ':');
                string key(hdr->data,cpos);

                while(*cpos == ':' || isspace(*cpos)) { cpos++; }
                int clen = strlen(cpos);
                string value(cpos,clen);
                headers[key] = value;
                hdr = hdr->next;
            }
        }





        /**
        * Set the request body for the request, starting at the given memory location and counting
        * length bytes. The method automatically sets the Content-Type and Content-Length headers
        * accordingly. Only supported by POST and PUT methods. The memory needs to be managed by
        * the caller and is NOT free'd when the request is cleaned up.
        */
        Request& Request::setBody(char *ptr, size_t length, const string content_type) {
            if(body) delete body;
            body = new Body(ptr, length, content_type);

            setHeader("Content-Type", content_type);
            setHeader("Content-Length", std::to_string(length));

            return *this;
        }


        /**
        * Set the request body for the request, using the content of the given data string. The
        * contents of the string are copied by the request, which takes care of cleaning up the
        * data when it is removed from memory.
        */
        Request& Request::setBody(const string data, const string content_type) {
            if(body) delete body;
            body = new Body(data, content_type);

            setHeader("Content-Type", content_type);
            setHeader("Content-Length", std::to_string(data.size()));

            return *this;
        }



        // read callback used by cURL to read request body, last argument is pointer to request object
        static size_t read_callback(void *ptr, size_t size, size_t nmemb, void *request) {
            Request* req = static_cast<Request*>(request);

            if(req->body->pos < req->body->length) {
                size_t len = req->body->length - req->body->pos;
                if(len > size*nmemb) {
                    len = size*nmemb;
                }
                memcpy(ptr, req->body->ptr + req->body->pos, len);
                req->body->pos += len;
                return len;
            } else {
                return 0;
            }
        }

        static size_t write_callback(void *ptr, size_t size, size_t nmemb, void *response) {

            Response* resp = static_cast<Response*>(response);
            if(resp->body == NULL) {
                resp->body = new Body();
            }
            resp->body->ptr = (char*)realloc(resp->body->ptr, resp->body->length + size * nmemb);
            memcpy(resp->body->ptr + resp->body->length, ptr, size * nmemb);
            resp->body->length += size * nmemb;

            return size * nmemb;
        }


        static size_t header_callback(char *buffer, size_t size, size_t nitems, void *response) {

            Response* resp = static_cast<Response*>(response);

            char* cpos = buffer;
            while(*cpos != ':' && cpos - buffer < size*nitems) cpos++;

            if(cpos - buffer < size*nitems) {
                // header found
                string key(buffer,cpos);

                char* last = buffer + (size*nitems);

                while(*cpos == ':' || isspace(*cpos)) { cpos++; }
                while((isspace(*last) || *last == 0) && last >= cpos) { last--; };
                int clen = last - cpos;
                string value(cpos,clen+1);

                resp->headers[key] = value;

            }
            return nitems*size;
        }


        /**
        * Initialise cURL HTTP client; carries out main cURL initialisation and registers callbacks
        */
        HTTPClient::HTTPClient() {
            curl_global_init(CURL_GLOBAL_ALL);

        }

        /**
        * Clean up cURL HTTP client; carries out main cURL shutdown.
        */
        HTTPClient::~HTTPClient() {
            curl_global_cleanup();
        }


        static void set_method(CURL* curl, Method m) {
            curl_easy_setopt(curl, CURLOPT_HTTPGET, 1); // clear first

            switch(m) {
                case PUT:
                    curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "PUT");
                    curl_easy_setopt(curl, CURLOPT_UPLOAD, 1L);
                    break;
                case POST:
                    curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "POST");
                    curl_easy_setopt(curl, CURLOPT_UPLOAD, 1L);
                    break;
                case GET:
                    curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "GET");
                    curl_easy_setopt(curl, CURLOPT_UPLOAD, 0L);
                    break;
                case DELETE:
                    curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "DELETE");
                    curl_easy_setopt(curl, CURLOPT_UPLOAD, 0L);
                    break;
            }
        }


        Response* HTTPClient::execute(Request& req) {
            CURL* curl = curl_easy_init();
            if(curl) {
                curl_easy_setopt(curl, CURLOPT_READFUNCTION, read_callback);
                curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
                curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, header_callback);
                curl_easy_setopt(curl, CURLOPT_USERAGENT, "mico-client/1.0");

                set_method(curl,req.getMethod());
                curl_easy_setopt(curl, CURLOPT_URL, req.getURL().c_str());

                Response* resp = new Response();
                curl_easy_setopt(curl, CURLOPT_READDATA, (void *)&req);
                curl_easy_setopt(curl, CURLOPT_WRITEDATA, resp);
                curl_easy_setopt(curl, CURLOPT_HEADERDATA, resp);
                curl_easy_setopt(curl, CURLOPT_HTTPHEADER, req.getCurlHeaders());
                if(req.getBody()) {
                    req.body->pos = 0; // init read position
                    curl_easy_setopt(curl, CURLOPT_INFILESIZE_LARGE, (curl_off_t)req.getBody()->getContentLength());
                } else {
                    curl_easy_setopt(curl, CURLOPT_INFILESIZE_LARGE, (curl_off_t)0);
                }
                curl_easy_perform(curl);
                curl_easy_getinfo (curl, CURLINFO_RESPONSE_CODE, &resp->status);
                curl_easy_cleanup(curl);


                return resp;
            } else {
                return NULL;
            }
        }




        std::ostream& operator<<(std::ostream& os, const Message& req) {
            for(auto hdr : req.headers) {
                os << hdr.first << ": " << hdr.second << std::endl;
            }
            os << std::endl;
            if(req.getBody() && req.getBody()->getContentLength() > 0) {
                os.write(req.getBody()->getContent(), req.getBody()->getContentLength());
            }

            return os;
        }


        std::ostream& operator<<(std::ostream& os, const Request& req) {
            switch(req.getMethod()) {
                case POST:
                    os << "POST ";
                    break;
                case PUT:
                    os << "PUT ";
                    break;
                case GET:
                    os << "GET ";
                    break;
                case DELETE:
                    os << "DELETE ";
                    break;
            }
            os << req.getURL() << std::endl;
            os << (Message&)req;
            return os;
        }


        std::ostream& operator<<(std::ostream& os, const Response& resp) {
            os << "Status: " << resp.getStatus() << "\n";
            os << (Message&)resp;
            return os;
        }


    }
}

