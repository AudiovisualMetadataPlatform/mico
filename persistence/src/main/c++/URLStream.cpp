#include <iosfwd>
#include <cstring>
#include <cstdio>
#include <utility>
#include <sys/time.h>
#include <boost/log/trivial.hpp>

#include "URLStream.hpp"

#define MIN(a,b) a < b ? a : b

namespace mico
{
namespace io
{


struct nullstream {};

// Swallow all types
template <typename T>
nullstream & operator<<(nullstream & s, T const &) {return s;}

// Swallow manipulator templates
nullstream & operator<<(nullstream & s, std::ostream &(std::ostream&)) {return s;}

static nullstream logstream;	
	
//#define LOG std::cout
#define LOG if(0) logstream	
	
/**
 * Read callback used by cURL to read request body from memory. Whenever cURL requests more data, 
 * we first check if there is still more data in our device buffer, in which case we hand it over 
 * to cURL directly. If there is no more data and we are not yet finishing the connection, we
 * return an indicator that cURL should wait and come back later when it is unpaused. If there
 * is no more data and we are finishing the connection, we return 0 to indicate EOF.
 * 
 * @param ptr     pointer to cURL buffer where we copy the data to
 * @param size    size of datatype in buffer
 * @param nmemb   maxmimum number of members of given size we can move into the buffer
 * @param _device pointer to the device we are handling data for
 */
static size_t read_callback(void *ptr, size_t size, size_t nmemb, void *_device)
{	
	URLDeviceBase* d = static_cast<URLDeviceBase*>(_device);
	LOG << "CALLBACK: sending up to " << nmemb << " bytes (pos: "<<d->buffer_position<<", len: "<<d->buffer_length<<", size: "<<d->buffer_size<<")\n";

	d->waiting = false;
	if(d->finishing) {
		LOG << "CALLBACK: sending finished!\n";
		return 0;
	} else if(d->buffer_position < d->buffer_length) {
		// read either as many bytes as are still in the buffer or at most as many as indicated by the function parameters
		int amount = MIN(size * nmemb, d->buffer_length-d->buffer_position);
		memcpy(ptr, d->buffer + d->buffer_position, amount);
		d->buffer_position += amount;

		LOG << "CALLBACK: sending "<<amount<<" bytes of data...\n";

		return amount;
	} else {
		if(d->mode == URL_MODE_WRITE) {
			LOG << "no more data, waiting for more...\n";
			
			// there is no more data in the buffer, wait for more data to be written by stream user
			d->waiting = true;
			d->buffer_position = 0;
			d->buffer_length   = 0;
			return CURL_READFUNC_PAUSE;
		} else {
			// we are in read mode, so we won't give cURL any more data anyways
			return 0;
		}
	}
}


/**
 * Write callback used by cURL in case it receives more data. In case the buffer can be reinitialised
 * (buffer position is at buffer length), we copy that data and tell cURL how much we copied. Otherwise, 
 * if we are not yet finishing, we tell cURL to wait until sending more data. If we are finishing, we 
 * tell cURL to abort further transfer by returning 0.
 * 
 * @param ptr     pointer to cURL buffer where we copy the data from
 * @param size    size of datatype in buffer
 * @param nmemb   maxmimum number of members of given size we can move from the buffer
 * @param _device pointer to the device we are handling data for
 */
static size_t write_callback(void *ptr, size_t size, size_t nmemb, void *_device)
{	
	URLDeviceBase* d = static_cast<URLDeviceBase*>(_device);
	LOG << "CALLBACK: receiving up to " << nmemb << " bytes (pos: "<<d->buffer_position<<", len: "<<d->buffer_length<<", size: "<<d->buffer_size<<")\n";

	d->waiting = false;
	if(d->buffer_position == d->buffer_length) {
		LOG << "CALLBACK: adding data to buffer ...\n";
		
		// check if we need to reserve more room
		if(size * nmemb > d->buffer_size) {
			d->buffer = (char*)realloc(d->buffer, size*nmemb);
			d->buffer_size = size*nmemb;			
		}
		memcpy(d->buffer, ptr, size*nmemb);
		d->buffer_position = 0;            // new position for read() is at beginning
		d->buffer_length   = size*nmemb;   // new length of data is the number of bytes we got
		return size * nmemb;
	} else if(!d->finishing) {
		if(d->mode == URL_MODE_READ) {
			// there is still data in the buffer, wait until the buffer is consumed
			d->waiting = true;
			return CURL_WRITEFUNC_PAUSE;
		} else {
			// we are not interested in receiving any data, because we are in write mode, so we 
			// just ignore the data
			return nmemb*size;			
		}	
	} else {
		return 0;
	}
}


/**
 * Open URL device using the given URL and flags. Uses cURL internally to access a remote
 * server, and fstream to access local files.
 *
 * @param url        the full URL to the file on the local or remote server (either starting with file://, http:// or ftp://)
 * @param mode       open mode, like for fopen; supported modes: r, w; remote files cannot be opened for reading and 
 *                   writing at the same time
 */
URLDeviceBase::URLDeviceBase(const char* url, URLMode mode) 
	: mode(mode), buffer(NULL), buffer_position(0), buffer_length(0), buffer_size(0), finishing(false), waiting(false), running_handles(0)
{
	if(strncmp("ftp://",url,6) == 0)  {
		// FTP URL mode
		type = URL_TYPE_FTP;
	} else if(strncmp("http://",url,7) == 0 || strncmp("https://",url,8) == 0) {
		type = URL_TYPE_HTTP;
	} else if(strstr(url, "://") != NULL) {
		// unknown protocol
		throw std::string("unsupported URL protocol: ") + url;
	} else {
		// file mode
		type = URL_TYPE_FILE;
	}

	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		curl_global_init(CURL_GLOBAL_ALL);

		handle.curl = curl_easy_init();

		// register cURL callbacks
		curl_easy_setopt(handle.curl, CURLOPT_READFUNCTION, read_callback);
		curl_easy_setopt(handle.curl, CURLOPT_WRITEFUNCTION, write_callback);
		curl_easy_setopt(handle.curl, CURLOPT_USERAGENT, "mico-client/1.0");

		// register cURL callback user data
		curl_easy_setopt(handle.curl, CURLOPT_READDATA,  this);
		curl_easy_setopt(handle.curl, CURLOPT_WRITEDATA, this);

		// set request URL
		curl_easy_setopt(handle.curl, CURLOPT_URL, url);
		
		switch(mode) {
		case URL_MODE_READ:
			// open file for reading
			curl_easy_setopt(handle.curl, CURLOPT_UPLOAD, 0);
			break;
		case URL_MODE_WRITE:
			// open file for writing
			curl_easy_setopt(handle.curl, CURLOPT_UPLOAD, 1);
			break;
		}

		multi_handle = curl_multi_init();
		curl_multi_add_handle(multi_handle, handle.curl);
		
		LOG << "constructed NEW cURL stream\n";
	} else if(type == URL_TYPE_FILE) {
		switch(mode) {
		case URL_MODE_READ:
			// open file for reading
			handle.file = fopen(url,"r");
			break;
		case URL_MODE_WRITE:
			// open file for writing
			handle.file = fopen(url,"w");
			break;
		}
	}
}


/**
 * Copy constructor, as we wrap non-trivial memory allocation
 */
URLDeviceBase::URLDeviceBase(const URLDeviceBase& other) 
  : mode(other.mode), type(other.type)
  , buffer(NULL), buffer_position(other.buffer_position), buffer_length(other.buffer_length), buffer_size(other.buffer_size)
  , finishing(other.finishing), waiting(other.waiting), running_handles(0)
{
	if(other.buffer != NULL) {
		// allocate new local buffer and copy all relevant data
		buffer = (char*)malloc(buffer_length * sizeof(char));
		memcpy(buffer, other.buffer, buffer_length * sizeof(char));
	}
	
	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		handle.curl = curl_easy_duphandle(other.handle.curl);

		// register cURL callback user data
		curl_easy_setopt(handle.curl, CURLOPT_READDATA,  this);
		curl_easy_setopt(handle.curl, CURLOPT_WRITEDATA, this);

		multi_handle = curl_multi_init();
		curl_multi_add_handle(multi_handle, handle.curl);
		
	} else {
		handle.file = other.handle.file; // TODO: necessary to duplicate?
	}
	
	LOG << "copy constructor, pos: " <<buffer_position<< ", len: " <<buffer_length<< ", size: " <<buffer_size<< "\n";
}


/**
 * Move constructor, as we wrap non-trivial memory allocation
 */
URLDeviceBase::URLDeviceBase(URLDeviceBase&& other) 
  : mode(other.mode), type(other.type)
  , buffer(NULL), buffer_position(other.buffer_position), buffer_length(other.buffer_length), buffer_size(other.buffer_size)
  , finishing(other.finishing), waiting(other.waiting), running_handles(0)
{
	LOG << "move constructor called\n";
	if(other.buffer != NULL) {
		// swap buffers
		std::swap(buffer,other.buffer);
	}
}


/**
 * Clean up resources occupied by device, e.g. remote or local file handles and connections.
 */
URLDeviceBase::~URLDeviceBase() 
{
	LOG << "closing stream\n";
	
	// inidicate we are finishing and let cURL continue running
	finishing = true;
	

	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		if(waiting) {
			loop();	
		}


		curl_multi_remove_handle(multi_handle, handle.curl);
		curl_multi_cleanup(multi_handle);
		curl_easy_cleanup(handle.curl);
	} else if(type == URL_TYPE_FILE) {
		fclose(handle.file);
	}

	// all data read or written, we don't need the buffer any more
	if(buffer != NULL) {
		free(buffer);
	}	
}


void URLDeviceBase::loop() 
{
	// first, in case we are waiting, continue processing
	if(waiting) {
		curl_easy_pause(handle.curl, CURLPAUSE_CONT);					
	}
	do {
		// then, loop until multi_perform finishes
		int rc; /* select() return code */ 

		struct timeval timeout;
		
		fd_set fdread;
		fd_set fdwrite;
		fd_set fdexcep;
		int maxfd = -1;		

		long curl_timeo = -1;
		
		FD_ZERO(&fdread);
		FD_ZERO(&fdwrite);
		FD_ZERO(&fdexcep);	
	
				
	    /* set a suitable timeout to play around with */ 
		timeout.tv_sec = 1;
		timeout.tv_usec = 0;
 
		curl_multi_timeout(multi_handle, &curl_timeo);
		if(curl_timeo >= 0) {
			timeout.tv_sec = curl_timeo / 1000;
			if(timeout.tv_sec > 1)
				timeout.tv_sec = 1;
			else
				timeout.tv_usec = (curl_timeo % 1000) * 1000;
		}
	
		/* get file descriptors from the transfers */ 
		curl_multi_fdset(multi_handle, &fdread, &fdwrite, &fdexcep, &maxfd);
		
		rc = select(maxfd+1, &fdread, &fdwrite, &fdexcep, &timeout);
 
		switch(rc) {
		case -1:
			/* select error */ 
			break;
		case 0:
		default:
			/* timeout or readable/writable sockets */ 
			curl_multi_perform(multi_handle, &running_handles);
			break;
		}	
	} while(running_handles && !waiting);
	
	
}

/**
 * Read at most n characters from the file, starting at the current position. Returns number
 * of characters read, or -1 in case of EOF.
 */
std::streamsize URLDeviceSource::read(char* s, std::streamsize n)
{
	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		if(buffer_position == buffer_length) {
			// no more data, signal cURL to continue receiving
			LOG << "READ: no more data, retrieving ...\n";
			loop();	
		}
		
		LOG << "READ: there are " << buffer_length << " bytes in the buffer ("<< (buffer_length - buffer_position) << " unconsumed) ...\n";
		
		if(buffer_position < buffer_length) {
			// there is still unconsumed data in the buffer, so we return it
			int amount = MIN(n, buffer_length - buffer_position);
			memcpy(s, buffer + buffer_position, amount);
			buffer_position += amount;
			return amount;
		} else {
			// there is no more data, so we are done
			return -1;
		}
	} else {
		if(feof(handle.file)) {
			return -1;
		} else {
			return fread(s, sizeof(char), n, handle.file);			
		}
	}
}

/**
 * Write n characters to the file, starting at the current position. Returns number of
 * characters written.
 */
std::streamsize URLDeviceSink::write(const char* s, std::streamsize n)
{
	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		// append to write buffer at the end
		if(buffer_length + n > buffer_size) {
			buffer = (char*)realloc(buffer, sizeof(char) * (buffer_length + n));
			buffer_size += n;
		}
		memcpy(buffer + buffer_length, s, n * sizeof(char));
		buffer_length += n;
		
		
		LOG << "WRITE: there are " << buffer_length << " bytes in the buffer ("<< (buffer_length - buffer_position) << " unconsumed) ...\n";
		
		// notify cURL that data is available
		loop();
	} else {
		return fwrite(s, sizeof(char), n, handle.file);
	}	
}


/**
 * Close the opened URL file handle.
 */
void URLDeviceBase::close()
{
}


}
}
