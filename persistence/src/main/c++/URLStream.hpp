#ifndef HAVE_URL_STREAM_H
#define HAVE_URL_STREAM_H 1

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
enum URLType {
    URL_TYPE_FILE = 1,
    URL_TYPE_HTTP = 2,
    URL_TYPE_FTP  = 3
};

enum URLMode {
	URL_MODE_READ, URL_MODE_WRITE
};

// cURL callbacks; declared here so we can friend them
static size_t read_callback(void *ptr, size_t size, size_t nmemb, void *device);
static size_t write_callback(void *ptr, size_t size, size_t nmemm, void *device);

/**
 * Base class for Boost Device implementations allowing to access local (file://) and remote (ftp://, http://)
 * URLs as stdio streams, similar to fstream. Remote files are partly buffered in main memory.
 *
 * The class is implemented using the Boost IOStreams library and uses the cURL library in the
 * background to a access remote files.
 */
class URLStreambufBase : public std::streambuf
{
	friend size_t read_callback(void *ptr, size_t size, size_t nmemb, void *device);	
	friend size_t write_callback(void *ptr, size_t size, size_t nmemm, void *device);
	
protected:

	URLType type;
	URLMode mode;

	union {
		CURL *curl;
		FILE *file;
	} handle;

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
	 * Open URL device using the given URL and flags. Uses cURL internally to access a remote
	 * server, and fstream to access local files.
	 *
	 * @param url        the full URL to the file on the local or remote server (either starting with file://, http:// or ftp://)
	 * @param mode       open mode, like for fopen; supported modes: r, r+, w, w+
	 */
	URLStreambufBase(const char* url, URLMode mode, int bufsize);


	/**
	 * Clean up resources occupied by device, e.g. remote or local file handles and connections.
	 */ 
	virtual ~URLStreambufBase();

	
private:

	/**
	 * Copy constructor not implemented, copying not allowed.
	 */
	URLStreambufBase(const URLStreambufBase& other);


	/**
	 * Copy assignment operator not implemented, copying not allowed.
	 */
	URLStreambufBase& operator=(URLStreambufBase other);

};


/**
 * A Boost Device implementation allowing read access to local and remote URLs.
 */
class URLIStreambuf : public URLStreambufBase {

public:

	URLIStreambuf(const char* url) : URLStreambufBase(url, URL_MODE_READ, CURL_MAX_WRITE_SIZE) {};
	
	
private:

	/**
	 * Underflow, so we need to fill the buffer again with more data.
	 */ 
	int underflow();

	/**
	 * Copy constructor not implemented, copying not allowed
	 */
	URLIStreambuf(const URLIStreambuf& other);


	/**
	 * Copy assignment operator not implemented, copying not allowed
	 */
	URLIStreambuf& operator=(URLIStreambuf other);
	
};


/**
 * A Boost Device implementation allowing write access to local and remote URLs.
 */
class URLOStreambuf : public URLStreambufBase {

public:
	
	URLOStreambuf(const char* url) : URLStreambufBase(url, URL_MODE_WRITE, CURL_MAX_WRITE_SIZE) {};

private:

	/**
	 * Buffer overflow, so we need to write out the buffer to the URL connection.
	 */ 
	int overflow(int c);

	/**
	 * Explicit call to write out the buffer to the URL connection even when it is not full
	 */ 
	int sync();

	/**
	 * Copy constructor not implemented, copying not allowed
	 */
	URLOStreambuf(const URLOStreambuf& other);


	/**
	 * Copy assignment operator not implemented, copying not allowed
	 */
	URLOStreambuf& operator=(URLOStreambuf other);
					
};

/**
 * Main type for opening an output stream to an URL for writing. Use url_ostream(URL) to open a 
 * new stream, and normal stream operators for sending data (i.e. <<).
 */ 
class url_ostream : public std::ostream {
public:
	url_ostream(const char* url) : std::ostream(new URLOStreambuf(url)) {};

	url_ostream(std::string url) : std::ostream(new URLOStreambuf(url.c_str())) {};
	
	~url_ostream() { rdbuf()->pubsync(); delete rdbuf(); };
};


/**
 * Main type for opening an input stream to an URL for reading. Use url_istream(URL) to open a 
 * new stream, and normal stream operators for receiving data (i.e. >>).
 */ 
class url_istream : public std::istream {
public:
	url_istream(const char* url) : std::istream(new URLIStreambuf(url)) {};

	url_istream(std::string url) : std::istream(new URLIStreambuf(url.c_str())) {};
	
	~url_istream() { delete rdbuf(); };
};


// TODO: deleting of content parts binary data from disk/FTP

}
}

#endif
