#include "HDFSStream.hpp"

namespace mico {
  namespace hdfs {


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
    HDFSDevice::HDFSDevice(hdfsFS hdfsHandle, const char* filename, int flags, int bufferSize = 0, short replication = 0, int blockSize = 0) : hdfsHandle(hdfsHandle) {
      fileHandle = hdfsOpenFile(hdfsHandle, filename, flags, bufferSize, replication, blockSize);
    }
      
    /**
     * Read at most n characters from the file, starting at the current position. Returns number
     * of characters read, or -1 in case of EOF.
     */
    std::streamsize HDFSDevice::read(char* s, std::streamsize n) {
      if(hdfsFileIsOpenForRead(fileHandle)) {
	int c = hdfsRead(hdfsHandle,fileHandle, (void*)s, n);
	if(c == 0) {
	  return -1;
	} else {
	  return c;
	}
      } else {
	return -1;
      }
    }

    /**
     * Write n characters to the file, starting at the current position. Returns number of
     * characters written.
     */
    std::streamsize HDFSDevice::write(const char* s, std::streamsize n) {
      if(hdfsFileIsOpenForWrite(fileHandle)) {
	return hdfsWrite(hdfsHandle,fileHandle, (void*)s, n);
      } else {
	return -1;
      }
    }

    /**
     * Seek a certain position in the file.
     */
    stream_offset HDFSDevice::seek(stream_offset off, std::ios_base::seekdir way) {
      if(way == std::ios_base::beg) {
	hdfsSeek(hdfsHandle, fileHandle, off);
      } else if(way == std::ios_base::cur) {
	tOffset cur = hdfsTell(hdfsHandle, fileHandle);
	hdfsSeek(hdfsHandle, fileHandle, cur+off);	
      }

      return hdfsTell(hdfsHandle, fileHandle);
    }


    /**
     * Close the opened HDFS file handle.
     */
    void HDFSDevice::close() { 
      if(hdfsFileIsOpenForWrite(fileHandle)) {
	hdfsFlush(hdfsHandle,fileHandle); 
      }
      hdfsCloseFile(hdfsHandle,fileHandle); 
    };

  }
}
