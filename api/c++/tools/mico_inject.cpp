#include <unistd.h>
#include <iostream>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>

#include <magic.h>

#include "EventManager.hpp"
#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"

#include "../config.h"


using namespace mico::event;

std::string getMimeType(const void* buffer, size_t length) {
	magic_t cookie = magic_open(MAGIC_MIME_TYPE);
	if(cookie == NULL) {
		std::cerr << "unable to initialize magic library" << std::endl;
		exit(1);
	}
	if(magic_load(cookie,NULL) != 0) {
		std::cerr << "unable to load magic database" << std::endl;
		exit(1);		
	}
	std::string result(magic_buffer(cookie,buffer,length));
	magic_close(cookie);
	return result;
}


void usage() {
	std::cout << "Usage: mico_inject SERVER_IP FILENAMES" << std::endl;
}

int main(int argc, char **argv) {
	if(argc < 3) {
		usage();
	}

	// initialise event manager; throws an exception on failure
	try {
		EventManager eventManager(argv[1]);
		
		ContentItem* item = eventManager.getPersistenceService().createContentItem();
		
		for(int i=2; i<argc; i++) {
			int fd = open(argv[i], O_RDONLY);

			
			if(fd >= 0) {
				struct stat st;
				fstat(fd,&st);
			
				size_t len = st.st_size;
				char* buffer = (char*)mmap(NULL, len, PROT_READ, MAP_SHARED, fd, 0);
				
				std::cout << "creating new content part for file " << argv[i] << " of size " << len << " with type " << getMimeType(buffer,len) << std::endl;
				
				Content* c = item->createContentPart();
				c->setType(getMimeType(buffer,len));
				std::ostream* os = c->getOutputStream();
				os->write(buffer, len);
				delete os;
				delete c;
				munmap(buffer, len);
			} else {
				std::cerr << "could not open file " << argv[i] << std::endl;
			}			
		}
		
		eventManager.injectContentItem(*item);
		
		std::cout << "created content item with URI " << item->getURI().stringValue() << std::endl;
		
		delete item;
	} catch(EventManagerException ex) {
		std::cerr << "could not initialise event manager: " << ex.getMessage() << std::endl;
	}
}