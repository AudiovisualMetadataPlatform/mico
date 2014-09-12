#include <unistd.h>
#include <iostream>

#include <ctime>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>

#include <magic.h>

#include "EventManager.hpp"
#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"
#include "vocabularies.hpp"

#include "../config.h"
#include "../logging.h"

using namespace mico::event;
using namespace mico::persistence;
using namespace mico::rdf::model;

namespace DC = mico::rdf::vocabularies::DC;

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

// helper function to get time stamp
std::string getTimestamp() {
	time_t now;
	time(&now);
	char buf[sizeof "2011-10-08T07:07:09Z"];
	strftime(buf, sizeof buf, "%FT%TZ", gmtime(&now));				
	return std::string(buf);
}


void usage() {
	std::cout << "Usage: mico_inject SERVER_IP USER PASSWORD FILENAMES" << std::endl;
}

int main(int argc, char **argv) {
    boost::log::core::get()->set_filter
    (
        boost::log::trivial::severity >= boost::log::trivial::warning
    );

	
	if(argc < 5) {
		usage();
	}

    char *mico_user = argv[2];
    char *mico_pass = argv[3];


	// initialise event manager; throws an exception on failure
	try {
		EventManager eventManager(argv[1], mico_user, mico_pass);
		
		ContentItem* item = eventManager.getPersistenceService().createContentItem();
		
		for(int i=4; i<argc; i++) {
			int fd = open(argv[i], O_RDONLY);

			
			if(fd >= 0) {
				struct stat st;
				fstat(fd,&st);
			
				size_t len = st.st_size;
				char* buffer = (char*)mmap(NULL, len, PROT_READ, MAP_SHARED, fd, 0);
				
				std::cout << "creating new content part for file " << argv[i] << " of size " << len << " with type " << getMimeType(buffer,len) << std::endl;
				
				Content* c = item->createContentPart();
				c->setType(getMimeType(buffer,len));
				c->setProperty(DC::source, argv[i]);
				c->setRelation(DC::creator, URI("http://www.mico-project.org/tools/mico_inject"));
				c->setProperty(DC::created, getTimestamp());
				std::ostream* os = c->getOutputStream();
				os->write(buffer, len);
				delete os;
				
				std::cout << "content part URI: " << c->getURI().stringValue() << std::endl;
				
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