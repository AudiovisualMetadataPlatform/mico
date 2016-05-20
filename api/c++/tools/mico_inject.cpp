/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <unistd.h>
#include <iostream>

#include <ctime>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>

#include <magic.h>

#include "EventManager.hpp"
#include "Item.hpp"
#include "SPARQLUtil.hpp"
#include "vocabularies.hpp"

#include "Logging.hpp"

using namespace mico::event;
using namespace mico::persistence::model;

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
  mico::log::set_log_level(mico::log::LoggingLevel::INFO);

  if(argc < 5) {
    usage();
    exit(1);
  }

  char *mico_user = argv[2];
  char *mico_pass = argv[3];

  try {
    EventManager eventManager(argv[1], mico_user, mico_pass);

    for(int i=4; i<argc; i++) {
      int fd = open(argv[i], O_RDONLY);



      if(fd >= 0) {
        struct stat st;
        fstat(fd,&st);

        size_t len = st.st_size;
        char* buffer = (char*)mmap(NULL, len, PROT_READ, MAP_SHARED, fd, 0);

        std::string mimeType = getMimeType(buffer,len);

        std::cout << "creating new Item for file " << argv[i] << " of size " << len << " with type " << mimeType << std::endl;
        std::shared_ptr<Item> item = eventManager.getPersistenceService()->createItem();
        std::shared_ptr<Resource> itemResource =  std::dynamic_pointer_cast<Resource>(item);

        std::dynamic_pointer_cast<Resource>(item)->setSemanticType("application/cpp_mico_inject");
        std::dynamic_pointer_cast<Resource>(item)->setSyntacticalType( getMimeType(buffer,len) );
        std::shared_ptr<Asset> asset = std::dynamic_pointer_cast<Resource>(item)->getAsset();

        asset->setFormat(mimeType);

        std::ostream* os = asset->getOutputStream();

        os->write(buffer, len);
        os->flush();
        delete os;

        std::cout << "Item successfully created." << std::endl;

        eventManager.injectItem(item);

        std::cout << "Item with URI [ " << itemResource->getURI().stringValue() <<" ] has been send to broker" << std::endl;

        munmap(buffer, len);
      } else {
        std::cerr << "Could not open file " << argv[i] << std::endl;
      }
    }

  } catch(EventManagerException ex) {
    std::cerr << "could not initialise event manager: " << ex.getMessage() << std::endl;
  } catch (std::string ex) {
    std::cerr << "other error: " << ex << std::endl;
  }
}
