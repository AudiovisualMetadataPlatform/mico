/**
 * This is an example showing how to implement an analysis service in C++. It uses the tesseract library to provide simple OCR
 * functionality for extracting plain text from images.
 * 
 * The service can be started from the commandline by calling 
 * 
 * ./mico_ocr_service SERVER_IP
 * 
 * It will then automatically connect to the MICO platform running at the given IP and offer its functionality to content items
 * which are injected. Note that this is a sample only. A proper implementation would probably better be started as Unix daemon
 * and detached from the tty console.
 * 
 * Go to http://SERVER_IP:8080/broker/ to get a simple overview over the registered services. Use the mico_inject tool to inject
 * an image into the platform and let it be analysed.
 * 
 * Dependencies:
 * libtesseract-dev
 * libleptonica-dev
 */ 

#include <ctime>

#include <tesseract/baseapi.h>
#include <leptonica/allheaders.h>

#include "EventManager.hpp"

// for constant RDF property definitions of common vocabularies
#include "vocabularies.hpp"

// for configuring logging levels
#include "../logging.h"

using std::string;

// this namespace contains EventManager and AnalysisService
using namespace mico::event;

// this namespace contains Content, ContentItem, etc
using namespace mico::persistence;

// this namespace contains the RDF data model
using namespace mico::rdf::model;

// define dublin core vocabulary shortcut
namespace DC = mico::rdf::vocabularies::DC;


// helper function to get time stamp
std::string getTimestamp() {
	time_t now;
	time(&now);
	char buf[sizeof "2011-10-08T07:07:09Z"];
	strftime(buf, sizeof buf, "%FT%TZ", gmtime(&now));				
	return std::string(buf);
}

/**
 * The base implementation of the OCR service. Since the service is capable of handling different image formats,
 * we then build different instances with different ids, requires and languages that we each register separately.
 */ 
class OCRAnalysisService : public AnalysisService {

private:
	tesseract::TessBaseAPI api;
	
public:

	/**
	 * Create an OCR analysis service by calling the constructor of the super class and handing it the service ID,
	 * requires and provides type, and an (optional) queue name.
	 */
	OCRAnalysisService(string id, string requires, string language) 
		: AnalysisService("http://www.mico-project.org/services/OCR-"+id, requires, "text/plain", "ocr-queue-"+id) {
		if(api.Init(NULL, language.c_str())) {
			std::cerr << "could not initialise tesseract instance" << std::endl;
			throw string("could not initialise tesseract instance");
		}
	};

	~OCRAnalysisService() {
		api.End();
	}

    /**
     * Call this service for the given content item and object. This method is called by the event manager whenever
     * a new analysis event for this service has been received in its queue. The API takes care of automatically
     * resolving the content item in the persistence service.
     *
     * @param resp   a response object that can be used to send back notifications about new objects to the broker
     * @param ci     the content item to analyse
     * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
     */
    void call(std::function<void(const ContentItem& ci, const URI& object)> resp, ContentItem& ci, URI& object) {
		// retrieve the content part identified by the object URI
		Content* imgPart = ci.getContentPart(object);
		
		if(imgPart != NULL) {
			// read content into a buffer, since tesseract cannot work with C++ streams
			std::istream* in = imgPart->getInputStream();
			std::vector<char> buf = std::vector<char>(std::istreambuf_iterator<char>(*in), std::istreambuf_iterator<char>());
			delete in;
			
			Pix* pic = pixReadMem((const unsigned char*)buf.data(),buf.size());
			
			// let tesseract do its magic
			api.SetImage(pic);		
			char* plainText = api.GetUTF8Text();
			
			// write plain text to a new content part
			Content *txtPart = ci.createContentPart();
			txtPart->setType("text/plain");
			
			// set some metadata properties (provenance information etc)
			txtPart->setRelation(DC::creator, getServiceID());
			txtPart->setRelation(DC::provenance, getServiceID());
			txtPart->setProperty(DC::created, getTimestamp());
			txtPart->setRelation(DC::source, object.stringValue());
			
			std::ostream* out = txtPart->getOutputStream();
			*out << plainText;
			delete out;
			
			// notify broker that we created a new content part by calling the callback function passed as argument
			resp(ci, txtPart->getURI());
			
			// clean up
			delete imgPart;
			delete txtPart;
			delete pic;
			delete [] plainText;		
		} else {
			std::cerr << "content item part " << object.stringValue() << " of content item " << ci.getURI().stringValue() << " does not exist!\n";
		}
	};
		
};


void usage() {
	std::cerr << "Usage: mico_ocr_service SERVER_IP [USER PASSWORD]" << std::endl;
}


// declare global scope services, because signal handler needs access to them
EventManager* mgr;	
OCRAnalysisService* pngAnalyser;
OCRAnalysisService* jpgAnalyser;

// indicate if we should continue looping
bool loop = true;

void signal_handler(int signum) {
	std::cout << "shutting down OCR analysers ... " << std::endl;
	
	mgr->unregisterService(pngAnalyser);
	mgr->unregisterService(jpgAnalyser);
	
	delete pngAnalyser;
	delete jpgAnalyser;
	delete mgr;	
	
	loop = false;
}

int main(int argc, char **argv) {
	if(argc != 2 && argc != 4) {
		usage();
		exit(1);
	}

    char *mico_user;
    char *mico_pass;

    if(argc == 4) {
        mico_user = argv[2];
        mico_pass = argv[3];
    } else {
        mico_user = "mico";
        mico_pass = "mico";
    }
	
	// configure Boost logging (only messages of level INFO or above)
    boost::log::core::get()->set_filter
    (
        boost::log::trivial::severity >= boost::log::trivial::info
    );

	
	
	const char* server_name = argv[1];
	
	// initialise an instance of EventManager 
	mgr = new EventManager(server_name, mico_user, mico_pass);
	
	// register several instances of the OCR analysis service for different types
	pngAnalyser = new OCRAnalysisService("png","image/png","eng");
	jpgAnalyser = new OCRAnalysisService("jpeg","image/jpeg","eng");
	
	mgr->registerService(pngAnalyser);
	mgr->registerService(jpgAnalyser);
	
	
	signal(SIGINT,  &signal_handler);
	signal(SIGTERM, &signal_handler);
	signal(SIGHUP,  &signal_handler);
	
	// go into an endless loop; the event manager has its own threads for running the services
	while(loop) {
		sleep(1);
	}
}
