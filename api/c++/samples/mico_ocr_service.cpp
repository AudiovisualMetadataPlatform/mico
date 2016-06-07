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
#include <boost/program_options.hpp>
#include <Logging.hpp>
#include "AnalysisService.hpp"
#include "Daemon.hpp"


// for constant RDF property definitions of common vocabularies
#include "vocabularies.hpp"


using std::string;

// this namespace contains EventManager and AnalysisService
using namespace mico::event;

// this namespace contains Item, Part, etc
using namespace mico::persistence::model;

// define dublin core vocabulary shortcut
namespace DC = mico::rdf::vocabularies::DC;

namespace po  = boost::program_options;

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
            : AnalysisService("http://www.mico-project.org/services/OCR-"+id, "OCR", id, "2.0.0",requires, "text/plain", "ocr-queue-"+id) {
        if(api.Init(NULL, language.c_str())) {
            LOG_ERROR( "could not initialise tesseract instance" );
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
    * @param item  the content item to analyse
    * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
    */
    void call(mico::event::AnalysisResponse& resp,
              std::shared_ptr< mico::persistence::model::Item > item,
              std::vector< std::shared_ptr<mico::persistence::model::Resource> > resources,
              std::map<std::string,std::string>& params) {
        // retrieve the content part identified by the object URI

        if (resources.size() > 1) {
          LOG_ERROR("Expected only one input to process, got %d", resources.size());
        }

        std::shared_ptr<mico::persistence::model::Resource> imgResource = resources[0];


        // read content into a buffer, since tesseract cannot work with C++ streams
        std::shared_ptr<Asset> imgAsset = imgResource->getAsset();
        std::istream* in = imgAsset->getInputStream();
        std::vector<char> buf = std::vector<char>(std::istreambuf_iterator<char>(*in), std::istreambuf_iterator<char>());
        delete in;

        Pix* pic = pixReadMem((const unsigned char*)buf.data(),buf.size());

        // let tesseract do its magic
        api.SetImage(pic);
        char* plainText = api.GetUTF8Text();

        // write plain text to a new content part
        std::shared_ptr<Part> txtPart = item->createPart(mico::persistence::model::URI("http://dont_know_what_to_write_here"));
        std::shared_ptr<Resource> txtResource = std::dynamic_pointer_cast<Resource>(txtPart);
        txtResource->setSyntacticalType( "text/plain" );

        std::shared_ptr<Asset> asset = txtResource->getAsset();
        std::ostream* out = asset->getOutputStream();
        *out << plainText;
        delete out;

        LOG_INFO("Sending OCR results");
        // notify broker that we created a new content part by calling functions from AnalysisResponse passed as argument
        resp.sendNew(item, txtResource->getURI());
        resp.sendFinish(item);

        // clean up
        delete pic;
        delete [] plainText;


    };

};

/****** globals ******/
EventManager* mgr = 0;
OCRAnalysisService* ocrService = 0;
bool loop = true;

void signal_handler(int signum) {
  std::cout << "shutting down ocr service ... " << std::endl;

  if (mgr) {
    mgr->unregisterService(ocrService);
  }

  if (ocrService)
    delete ocrService;

  if (mgr)
    delete mgr;

  loop = false;
}

static  std::string ip;
static  std::string user;
static  std::string passw;

static bool setCommandLineParams(int argc, char** argv, po::variables_map& vm)
{
  po::options_description desc("Allowed options");
  desc.add_options()
    ("serverIP,i",  po::value<std::string>(&ip)->required(), "IP of the MICO system server.")
    ("userName,u",  po::value<std::string>(&user)->required(), "MICO system user name")
    ("userPassword,p", po::value<std::string>(&passw)->required(), "MICO system user password")
    ("kill,k","Shuts down the service.")
    ("foreground,f","Runs the png extractor as foreground process (by default it runs as daemon)")
    ("help,h","Prints this help message.");

  po::positional_options_description p;
  p.add("serverIP",1);
  p.add("userName",1);
  p.add("userPassword",1);

  bool printHelp = false;
  std::string ex("") ;
  try {
    po::store(po::command_line_parser(argc, argv).options(desc).positional(p).run() , vm);
    po::notify(vm);
  } catch (std::exception& e) {
    ex = e.what();
    printHelp = true;
  }
  if (vm.count("help")) {
    printHelp = true;
  }

  if (printHelp) {
    if (ex.size() > 0)
      std::cout << std::endl << ex << "\n";
    std::cout << "\nUsage:   " << argv[0];
    for (unsigned int i=0; i < p.max_total_count(); ++i)
      std::cout << " " << p.name_for_position(i);
    std::cout << " [options]" << "\n";
    std::cout << "\n" << desc << "\n";
    return false;
  }
  return true;
}

int main(int argc, char **argv) {
  po::variables_map vm;
  if (!setCommandLineParams(argc, argv, vm)) {
   exit(EXIT_FAILURE);
  }

  bool   doKill    = false;
  bool   asDaemon  = true;

  if (vm.count("kill")) {
    doKill = true;
  }

  if (vm.count("foreground")) {
    doKill   = false;
    asDaemon = false;
  }
  std::string s_daemon_id(argv[0]);

  if (!doKill && asDaemon)
     std::cout << "Setting up ocr extractor as deamon" << std::endl;
  else if (doKill && asDaemon)
     std::cout << "Shutting down ocr extractor deamon " << std::endl;
  else
     std::cout << "Starting ocr extractor " << std::endl;

  if(doKill) {
    return mico::daemon::stop(s_daemon_id.c_str());
  }
  if (asDaemon) {
    mico::log::set_log_backend (mico::daemon::createDaemonLogBackend());
    // create a new instance of a MICO daemon, auto-registering two instances of the OCR analysis service
    return mico::daemon::start(s_daemon_id.c_str(), ip.c_str(), user.c_str(), passw.c_str(),
    {new OCRAnalysisService("png", "image/png", "eng"), new OCRAnalysisService("jpeg", "image/jpeg", "eng")});
  } else {
    mico::log::set_log_backend (new mico::log::StdOutBackend());
    mgr = new EventManager(ip.c_str(), user.c_str(), passw.c_str());
    ocrService = new OCRAnalysisService("png", "image/png", "eng");

    mgr->registerService(ocrService);

    signal(SIGINT,  &signal_handler);
    signal(SIGTERM, &signal_handler);
    signal(SIGHUP,  &signal_handler);

    while(loop) {
      sleep(1);
    }
  }
}
