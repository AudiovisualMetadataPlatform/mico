#include <boost/algorithm/string.hpp>
#include <curl/curl.h>

#include "Content.hpp"
#include "ContentItem.hpp"
#include "URLStream.hpp"
#include "SPARQLUtil.hpp"
#include "rdf_model.hpp"
#include "rdf_query.hpp"


#include "../logging.h"

using namespace std;
using namespace boost;
using namespace mico::rdf::model;
using namespace mico::rdf::query;

SPARQL_INCLUDE(setContentType);
SPARQL_INCLUDE(getContentType);
SPARQL_INCLUDE(setContentProperty);
SPARQL_INCLUDE(getContentProperty);
SPARQL_INCLUDE(setContentRelation);
SPARQL_INCLUDE(getContentRelation);


namespace mico {
    namespace persistence {

        Content::Content(ContentItem& item, const string baseUrl, const string& contentDirectory, const URI& uri) : item(item), baseUrl(baseUrl), contentDirectory(contentDirectory) {
            if(!starts_with(uri.stringValue(),baseUrl)) {
                throw string("the baseUrl is not a prefix of the URI, invalid argument");
            }

            id = uri.stringValue().substr(baseUrl.length() + 1);
        }

        /**
        *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
        *  that it derives from the ContentItem this part belongs to.
        * @return
        */
        const mico::rdf::model::URI Content::getURI() {
            return URI(baseUrl + "/" + id);
        };


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
                string ret = r->at(0).at("t")->stringValue();
                for(size_t i=0; i<r->size(); i++) {
                    delete r->at(i).at("t");
                }
                delete r;
                return ret;
            } else {
                delete r;
                return "";
            }
        }


        /**
        * Set the property with the given URI to the given value. Use e.g. in combination with fixed vocabularies.
        */
        void Content::setProperty(const URI& property, const string value) {
            Metadata metadata = item.getMetadata();

            map<string,string> params;
            params["ci"] = item.getURI().stringValue();
            params["cp"] = baseUrl + "/" + id;
            params["p"]  = property.stringValue();
            params["value"] = value;

            metadata.update(SPARQL_FORMAT(setContentProperty, params));
        }

        /**
        * Return the property value of this content part for the given property. Use e.g. in combination with fixed vocabularies.
        */
        string Content::getProperty(const URI& property) {
            Metadata metadata = item.getMetadata();

            map<string,string> params;
            params["ci"] = item.getURI().stringValue();
            params["p"]  = property.stringValue();
            params["cp"] = baseUrl + "/" + id;

            const TupleResult* r = metadata.query(SPARQL_FORMAT(getContentProperty,params));
            if(r->size() > 0) {
                string ret = r->at(0).at("t")->stringValue();
                for(size_t i=0; i<r->size(); i++) {
                    delete r->at(i).at("t");
                }
                delete r;
                return ret;
            } else {
                delete r;
                return "";
            }
        }


        /**
        * Set the relation with the given URI to the given target resource. Use e.g. in combination with fixed vocabularies.
        */
        void Content::setRelation(const URI& property, const URI& value) {
            Metadata metadata = item.getMetadata();

            map<string,string> params;
            params["ci"] = item.getURI().stringValue();
            params["cp"] = baseUrl + "/" + id;
            params["p"]  = property.stringValue();
            params["value"] = value.stringValue();

            metadata.update(SPARQL_FORMAT(setContentRelation, params));
        }

        /**
        * Return the relation target of this content part for the given property. Use e.g. in combination with fixed vocabularies.
        */
        Value* Content::getRelation(const URI& property) {
            Metadata metadata = item.getMetadata();

            map<string,string> params;
            params["ci"] = item.getURI().stringValue();
            params["p"]  = property.stringValue();
            params["cp"] = baseUrl + "/" + id;

            const TupleResult* r = metadata.query(SPARQL_FORMAT(getContentRelation,params));
            if(r->size() > 0) {
                Value* ret = r->at(0).at("t");
                for(size_t i=1; i<r->size(); i++) {
                    delete r->at(i).at("t");
                }
                delete r;
                return ret;
            } else {
                delete r;
                return NULL;
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


        // needed to suppress libcurl output
        static size_t write_callback(void *ptr, size_t size, size_t nmemb, void *_device) {
            return size*nmemb;
        }

        void Content::deleteContent() {
            std::string path(contentDirectory + "/" + id + ".bin");

            LOG_DEBUG << "deleting binary content from " << path << std::endl;


            if(strncmp("ftp://",path.c_str(),6) == 0)  {
                // FTP URL mode
                CURL *curl;
                CURLcode res;

                curl_global_init(CURL_GLOBAL_DEFAULT);
                curl = curl_easy_init();
                if(curl) {
                    // TODO: need to split URL at least into path and filename
                    int pathSep = path.find('/', 7);
                    std::string host = path.substr(0, pathSep);
                    std::string file = path.substr(pathSep +1);

                    //std::string command1 = "CD " + dir;
                    std::string command2 = "DELE " + file;

                    curl_slist* commands = NULL;
                    commands = curl_slist_append(commands,command2.c_str());


                    curl_easy_setopt(curl, CURLOPT_URL, host.c_str());
                    curl_easy_setopt(curl, CURLOPT_POSTQUOTE, commands);
                    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);

                    res = curl_easy_perform(curl);
                    curl_easy_cleanup(curl);
                    if(CURLE_OK != res) {
                        std::cerr << "could not delete resource: " << res << std::endl;
                    }

                    curl_slist_free_all(commands);
                }
                curl_global_cleanup();


            } else if(strncmp("http://",path.c_str(),7) == 0 || strncmp("https://",path.c_str(),8) == 0) {
                std::cerr << "deleting HTTP data not yet supportedd ..." << std::endl;
            } else if(strncmp("file://",path.c_str(),7) == 0) {
                unlink(path.c_str()+7);
            } else if(strstr(path.c_str(), "://") != NULL) {
                // unknown protocol
                throw std::string("unsupported URL protocol: ") + path;
            } else {
                // file mode
                unlink(path.c_str());
            }

        }
    }
}
