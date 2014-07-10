#ifndef HAVE_URL_STREAM_H
#define HAVE_URL_STREAM_H 1

#include <cstdio>
#include <fcntl.h>
#include <iosfwd>
#include <curl/curl.h>
#include <boost/iostreams/stream.hpp>
#include <boost/iostreams/stream_buffer.hpp>
#include <boost/iostreams/categories.hpp>
#include <boost/iostreams/positioning.hpp>


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
class URLDeviceBase
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

	char* buffer;           //!< internal buffer for storing data from last read

	size_t buffer_position; //!< position in buffer as set by cURL 
	size_t buffer_length;   //!< current data length of buffer
	size_t buffer_size;     //!< allocated buffer size

	bool finishing;         //!< indicate if device is already finishing transfer (i.e. no more reads/writes)
	bool waiting;           //!< indicate if the device is still waiting for more data

public:
	typedef char                          char_type;

	/**
	 * Open URL device using the given URL and flags. Uses cURL internally to access a remote
	 * server, and fstream to access local files.
	 *
	 * @param url        the full URL to the file on the local or remote server (either starting with file://, http:// or ftp://)
	 * @param mode       open mode, like for fopen; supported modes: r, r+, w, w+
	 */
	URLDeviceBase(const char* url, URLMode mode);

	/**
	 * Copy constructor, as we wrap non-trivial memory allocation
	 */
	URLDeviceBase(const URLDeviceBase& other);

	/**
	 * Clean up resources occupied by device, e.g. remote or local file handles and connections.
	 */ 
	virtual ~URLDeviceBase();

	/**
	 * Close the opened URL file handle.
	 */
	void close();
};


/**
 * A Boost Device implementation allowing read access to local and remote URLs.
 */
class URLDeviceSource : public URLDeviceBase {

public:
	typedef boost::iostreams::source_tag  category;  //!< support read operations

	URLDeviceSource(const char* url) : URLDeviceBase(url, URL_MODE_READ) {};

	/**
	 * Copy constructor, as we wrap non-trivial memory allocation
	 */
	URLDeviceSource(const URLDeviceSource& other) : URLDeviceBase(other) {};
	
	/**
	 * Read at most n characters from the file, starting at the current position. Returns number
	 * of characters read, or -1 in case of EOF.
	 */
	std::streamsize read(char* s, std::streamsize n);
	
};


/**
 * A Boost Device implementation allowing write access to local and remote URLs.
 */
class URLDeviceSink : public URLDeviceBase {

public:
	typedef boost::iostreams::sink_tag  category;  //!< support write operations
	
	
	URLDeviceSink(const char* url) : URLDeviceBase(url, URL_MODE_WRITE) {};

	/**
	 * Copy constructor, as we wrap non-trivial memory allocation
	 */
	URLDeviceSink(const URLDeviceSink& other) : URLDeviceBase(other) {};
		
	/**
	 * Write n characters to the file, starting at the current position. Returns number of
	 * characters written.
	 */
	std::streamsize write(const char* s, std::streamsize n);
	
};

/**
 * Main type for opening an output stream to an URL for writing. Use url_ostream(URL) to open a 
 * new stream, and normal stream operators for sending data (i.e. <<).
 */ 
typedef boost::iostreams::stream<URLDeviceSink> url_ostream;

/**
 * Main type for opening an output streambuffer to an URL for writing.
 */ 
typedef boost::iostreams::stream_buffer<URLDeviceSink> url_ostreambuf;

/**
 * Main type for opening an input stream to an URL for reading. Use url_istream(URL) to open a 
 * new stream, and normal stream operators for receiving data (i.e. >>).
 */ 
typedef boost::iostreams::stream<URLDeviceSource> url_istream;

/**
 * Main type for opening an input streambuffer to an URL for reading.
 */ 
typedef boost::iostreams::stream_buffer<URLDeviceSource> url_istreambuf;


}
}

#endif
