#include <boost/algorithm/string.hpp>

#include "Content.hpp"
#include "ContentItem.hpp"
#include "URLStream.hpp"
#include "SPARQLUtil.hpp"

#include "../logging.h"

using namespace std;
using namespace boost;

SPARQL_INCLUDE(setContentType);
SPARQL_INCLUDE(getContentType);


namespace mico {
  namespace persistence {

    Content::Content(ContentItem& item, const string baseUrl, const string& contentDirectory, const URI& uri) : item(item), baseUrl(baseUrl), contentDirectory(contentDirectory) {
      if(!starts_with(uri.stringValue(),baseUrl)) {
		throw string("the baseUrl is not a prefix of the URI, invalid argument");
      }

      id = uri.stringValue().substr(baseUrl.length() + 1);
    }

    /**
     * Set the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     *
     * @param type
     */
	void Content::setType(const string type) {
		Metadata metadata = item.getMetadata();
		
		map<string,string> params;
		params["ci"] = item.getURI().stringValue();
		params["cp"] = baseUrl + "/" + id;
		params["type"] = type;

		metadata.update(SPARQL_FORMAT(setContentType, params));		
	}

    /**
     * Return the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     */
    string Content::getType() {
		Metadata metadata = item.getMetadata();
		
		map<string,string> params;
		params["ci"] = item.getURI().stringValue();
		params["cp"] = baseUrl + "/" + id;
		
		const TupleResult* r = metadata.query(SPARQL_FORMAT(getContentType,params));
		if(r->size() > 0) {
			string ret = r->at(0).at("t")->stringValue(); // TODO: check if we need to manually delete all values in the binding set!
			delete r;
			return ret;
		} else {
			delete r;
			return "";
		}
	}


    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     * @return
     */
    std::ostream* Content::getOutputStream() {
		LOG_DEBUG << "new output stream connection to " << std::string(contentDirectory + "/" + id + ".bin") << std::endl;
		return new mico::io::url_ostream(contentDirectory + "/" + id + ".bin");
    }

    /**
     *  Return a new input stream for reading the content.
     * @return
     */
    std::istream* Content::getInputStream() {
		LOG_DEBUG << "new input stream connection to " << std::string(contentDirectory + "/" + id + ".bin") << std::endl;
		return new mico::io::url_istream(contentDirectory + "/" + id + ".bin");
    }

  }
}
