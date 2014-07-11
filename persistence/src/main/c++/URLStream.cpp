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
	URLStreambufBase* d = static_cast<URLStreambufBase*>(_device);
	LOG << "CALLBACK (0x"<<(long)_device<<"): sending up to " << nmemb << " bytes (pos: "<< (int)(d->buffer_position - d->buffer)<< ", len: "<< (int)(d->pptr()-d->buffer) <<")\n";
	d->waiting = false;
	if(d->mode == URL_MODE_READ) {
		// we are in read mode, so we won't give cURL any more data anyways
		LOG << "CALLBACK: read mode, not sending any data!\n";
		return 0;		
	} else if(d->finishing) {
		// we are finishing the connection, no need to send any more data
		LOG << "CALLBACK: sending finished!\n";
		return 0;
	} else if(d->buffer_position < d->pptr()) {
		// read either as many bytes as are still in the buffer or at most as many as indicated by the function parameters
		int amount = MIN(size * nmemb, (int)(d->pptr()-d->buffer_position));
		memcpy(ptr, d->buffer_position, amount);
		d->buffer_position += amount;

		LOG << "CALLBACK: sending "<<amount<<" bytes of data...\n";

		return amount;
	} else {
		LOG << "no more data, waiting for more...\n";
		
		// there is no more data in the buffer, wait for more data to be written by stream user
		d->waiting = true;
		d->setp(d->buffer, d->buffer+d->buffer_size);
		d->buffer_position = d->buffer;
		return CURL_READFUNC_PAUSE;
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
	URLStreambufBase* d = static_cast<URLStreambufBase*>(_device);
	LOG << "CALLBACK (0x"<<(long)_device<<"): receiving up to " << nmemb << " bytes (pos: "<< (int)(d->gptr()-d->buffer)<< ", len: "<< (int)(d->egptr()-d->buffer) <<", size: "<<d->buffer_size<<")\n";

	d->waiting = false;
	if(d->mode == URL_MODE_WRITE) {
		// we are not interested in receiving any data, because we are in write mode, so we 
		// just ignore the data
		LOG << "CALLBACK: write mode, ignoring received data!\n";
		return nmemb*size;			
	} else if(d->gptr() == d->egptr()) {
		LOG << "CALLBACK: adding data to buffer ...\n";
		
		memcpy(d->buffer, ptr, size*nmemb);
		
		// set new buffer positions for streambuf
		d->setg(d->buffer, d->buffer, d->buffer+size*nmemb);
		
		return size * nmemb;
	} else if(!d->finishing) {
		// there is still data in the buffer, wait until the buffer is consumed
		d->waiting = true;
		return CURL_WRITEFUNC_PAUSE;
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
URLStreambufBase::URLStreambufBase(const char* url, URLMode mode, int bufsize) 
	: mode(mode), buffer_size(bufsize), finishing(false), waiting(false), running_handles(0)
{
	// allocate buffer for reading/writing
	buffer = (char*)malloc(bufsize * sizeof(char));
	buffer_position = buffer;
	
	// set stream pointers and indicate that in the beginning we always have buffer overflow/underflow
	setg(buffer, buffer+bufsize, buffer+bufsize);
	setp(buffer, buffer+bufsize);	
	
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
 * Clean up resources occupied by device, e.g. remote or local file handles and connections.
 */
URLStreambufBase::~URLStreambufBase() 
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


void URLStreambufBase::loop() 
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
 * Underflow, so we need to fill the buffer again with more data.
 */ 
int URLIStreambuf::underflow() {
	if(gptr() < egptr()) {
		// buffer not exhausted, return current byte
		return traits_type::to_int_type(*gptr());
	}

	LOG << "READ: no more data, retrieving ...\n";
	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		// try reading more data from URL by triggering retrieval
		loop();	
	} else {
		if(feof(handle.file)) {
			return traits_type::eof();
		} else {
			// read more data from file starting at current position
			int n = fread(buffer, sizeof(char), buffer_size, handle.file);
			setg(buffer,buffer,buffer+n);
		}		
	}

	LOG << "READ: there are " << (int)(egptr()-buffer) << " bytes in the buffer ("<< (int)(egptr() - gptr()) << " unconsumed) ...\n";
	if(gptr() < egptr()) {
		// buffer not exhausted, return current byte
		return traits_type::to_int_type(*gptr());
	} else {
		return traits_type::eof();
	}
}


/**
 * Buffer overflow, so we need to write out the buffer to the URL connection.
 */ 
int URLOStreambuf::overflow(int c) {
	LOG << "OVERFLOW: there are " << (int)(epptr()-buffer) << " bytes in the buffer ...\n";
	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		// notify cURL that data is available
		loop();
	} else {
		// write buffer data to file and reset pointers
		int n = fwrite(buffer, sizeof(char), (int)(epptr()-buffer), handle.file);
		if(n < (int)(epptr()-buffer)) {
			return traits_type::eof();
		}
		setp(buffer,buffer+buffer_size);
	}
	return c;
}

/**
 * Explicit call to write out the buffer to the URL connection even when it is not full
 */ 
int URLOStreambuf::sync() {
	LOG << "SYNC: there are " << (int)(epptr()-buffer) << " bytes in the buffer ...\n";
	if(type == URL_TYPE_HTTP || type == URL_TYPE_FTP) {
		// notify cURL that data is available
		loop();
	} else {
		// write buffer data to file and reset pointers
		int n = fwrite(buffer, sizeof(char), (int)(pptr()-buffer), handle.file);
		if(n < (int)(epptr()-buffer)) {
			return -1;
		}
		setp(buffer,buffer+buffer_size);
	}
	return 0;
}



}
}
