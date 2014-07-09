#ifndef HAVE_HDFS_STREAM_H
#define HAVE_HDFS_STREAM_H 1

#include <hdfs.h>
#include <fcntl.h>
#include <iosfwd> 
#include <boost/iostreams/stream.hpp>
#include <boost/iostreams/stream_buffer.hpp>
#include <boost/iostreams/categories.hpp>
#include <boost/iostreams/positioning.hpp>

namespace mico
{
namespace hdfs
{

	namespace io = boost::iostreams;
	
class HDFSDevice 
{
private:

	hdfsFS hdfsHandle;   //!< an initialised handle to the HDFS file system
	hdfsFile fileHandle; //!< a handle to the file opened by this stream

public:
	typedef char                 char_type;
	typedef io::seekable_device_tag  category;

	/**
	 * Open HDFS device using the given HDFS file handle.
	 *
	 * @param fileHandle an opened handle to a file in the HDFS file system
	 */
	HDFSDevice(hdfsFS hdfsHandle, hdfsFile fileHandle) : hdfsHandle(hdfsHandle), fileHandle(fileHandle) {};

	/**
	 * Open HDFS device using the given HDFS handle and a file name in HDFS
	 *
	 * @param hdfsHandle an initialised handle to the HDFS file system
	 * @param fileName   the full path to the file in the HDFS file system
	 * @param flags      or'ed fcntl flags used for opening the file; supported: supported flags
	 *                   are O_RDONLY, O_WRONLY (meaning create or overwrite i.e., implies O_TRUNCAT),
	 *                   O_WRONLY|O_APPEND. Other flags are generally ignored other than (O_RDWR
	 *                   || (O_EXCL & O_CREAT)) which return NULL and set errno equal ENOTSUP.
	 */
	HDFSDevice(hdfsFS hdfsHandle, const char* filename, int flags, int bufferSize, short replication, int blockSize);

	/**
	 * Read at most n characters from the file, starting at the current position. Returns number
	 * of characters read, or -1 in case of EOF.
	 */
	std::streamsize read(char* s, std::streamsize n);

	/**
	 * Write n characters to the file, starting at the current position. Returns number of
	 * characters written.
	 */
	std::streamsize write(const char* s, std::streamsize n);

	/**
	 * Seek a certain position in the file.
	 */
	io::stream_offset seek(io::stream_offset off, std::ios_base::seekdir way);

	/**
	 * Close the opened HDFS file handle.
	 */
	void close();
};

typedef boost::iostreams::stream<HDFSDevice> hdfs_stream;
typedef boost::iostreams::stream_buffer<HDFSDevice> hdfs_streambuf;
}
}


#endif
