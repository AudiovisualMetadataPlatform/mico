#ifndef HAVE_HTTP_CLIENT_H
#define HAVE_HTTP_CLIENT_H 1

#include <string>
#include <map>
#include <iostream>


struct curl_slist;

namespace mico {
  namespace http {

    // cURL callbacks; declared here so we can friend them
    static size_t read_callback(void *ptr, size_t size, size_t nmemb, void *request);
    static size_t write_callback(void *ptr, size_t size, size_t nmemm, void *response);
    static size_t header_callback(char *buffer, size_t size, size_t nitems, void *response);

    /**
     * The different types of requests supported by the library.
     */
    enum Method {
      GET, POST, PUT, DELETE
    };


    class Body {	

      friend class Request;
      friend class HTTPClient;
      friend size_t write_callback(void *ptr, size_t size, size_t nmemm, void *response);
      friend size_t read_callback(void *ptr, size_t size, size_t nmemb, void *request);

    private:
      char*       ptr;
      size_t      length;
      std::string type;
      bool        managed;
      size_t      pos;    // internal use by read_callback

      Body() : ptr(NULL), length(0), managed(false) {};

    public:

      Body(char* ptr, size_t length, const std::string& content_type) : ptr(ptr), length(length), type(content_type), managed(false), pos(0) {};
      Body(const std::string& data, const std::string& content_type);
      ~Body();


      const char* getContent() const { return ptr; };

      /**
       * Get the length of the content of this request.
       */
      size_t getContentLength() const { return length; };

      /**
       * Get the type of the content of this request.
       */
      const std::string& getContentType() const { return type; };

    };


    class Message {
      friend class HTTPClient;
      friend std::ostream& operator<<(std::ostream& os, const Message& req);
      friend std::istream& operator>>(std::istream& is, const Message& req);

      typedef std::map<std::string,std::string> header_map;

    protected:

      // headers
      header_map  headers;
	
      // optional request body
      Body*       body;

    public:

      Message() : body(NULL) { headers["Expect"]=""; };
      virtual ~Message();

      /**
       * Return the header with the given name
       */
      inline const std::string& getHeader(const std::string hdr) const { return headers.at(hdr); };


      /**
       * Return the body of the message
       */
      inline const Body* getBody() const { return body; };



      /**
       * Return the headers in CURL format
       */
      curl_slist* getCurlHeaders();

      /**
       * Set the headers in CURL format
       */
      void setCurlHeaders(curl_slist* hdr);

    };



    class Request : public Message{

      friend std::ostream& operator<<(std::ostream& os, const Request& req);
      friend std::istream& operator>>(std::istream& is, const Request& req);

      friend size_t read_callback(void *ptr, size_t size, size_t nmemb, void *request);
	
    private:
      // request type to send
      Method type;

      // request URL
      std::string url;


    public:

      Request(Method type, std::string url) : Message(), type(type), url(url) {};

      /**
       * Return the request method
       */
      inline Method getMethod() const { return type; };

      /**
       * Return the request URL
       */
      inline const std::string& getURL() const { return url; }

      /**
       * Add an HTTP header to the request. Note that Content-Type and Content-Length are already
       * set by addBody().
       */
      inline Request& setHeader(const std::string hdr, const std::string value) { headers[hdr] = value; };


      /**
       * Remove an HTTP header from the request. Use with care.
       */
      inline Request& delHeader(const std::string hdr) { headers.erase(hdr); };

      /**
       * Set the request body for the request, starting at the given memory location and counting
       * length bytes. The method automatically sets the Content-Type and Content-Length headers 
       * accordingly. Only supported by POST and PUT methods. The memory needs to be managed by
       * the caller and is NOT free'd when the request is cleaned up.
       */
      Request& setBody(char *ptr, size_t length, const std::string content_type);


      /**
       * Set the request body for the request, using the content of the given data string. The
       * contents of the string are copied by the request, which takes care of cleaning up the
       * data when it is removed from memory.
       */
      Request& setBody(const std::string data, const std::string content_type);
	

      /**
       * Set the request body for the request, using the given body object. The memory needs to be managed by
       * the caller and is NOT free'd when the request is cleaned up.
       */
      Request& setBody(Body& body) { setBody(body.ptr, body.length, body.type); };



    };



    class Response : public Message {
      friend size_t header_callback(char *buffer, size_t size, size_t nitems, void *response);
      friend size_t write_callback(void *ptr, size_t size, size_t nmemm, void *response);

      friend class HTTPClient;

    private:
      long status;

    public:
      Response() : Message() {};


      long getStatus() const { return status; };

    };


    /**
     * A simple HTTP client. Takes care of properly initialising cURL and allows executing requests.
     */
    class HTTPClient {

    public:
      HTTPClient();
      ~HTTPClient();
	

      Response* execute(Request& req); 

    };


    std::ostream& operator<<(std::ostream& os, const Message& req);
    std::ostream& operator<<(std::ostream& os, const Request& req);
    std::ostream& operator<<(std::ostream& os, const Response& resp);
    std::istream& operator>>(std::istream& is, const Message& req);
    std::istream& operator>>(std::istream& is, const Request& req);

      
  }
}


#endif
