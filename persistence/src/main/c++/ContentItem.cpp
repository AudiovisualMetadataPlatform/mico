#include <boost/uuid/uuid_generators.hpp>
#include <boost/algorithm/string.hpp>

#include <map>
#include <string>

#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"

using namespace std;
using namespace boost;
using namespace uuids;
using namespace mico::util;

// extern references to constant SPARQL templates
extern unsigned char src_main_resources_sparql_askContentPart_sparql[];
extern unsigned int src_main_resources_sparql_askContentPart_sparql_len;
extern unsigned char src_main_resources_sparql_createContentPart_sparql[];
extern unsigned int src_main_resources_sparql_createContentPart_sparql_len;
extern unsigned char src_main_resources_sparql_deleteContentPart_sparql[];
extern unsigned int src_main_resources_sparql_deleteContentPart_sparql_len;
extern unsigned char src_main_resources_sparql_listContentParts_sparql[];
extern unsigned int src_main_resources_sparql_listContentParts_sparql_len;

 
const std::string sparql_askContentPart((char*)src_main_resources_sparql_askContentPart_sparql, src_main_resources_sparql_askContentPart_sparql_len);
const std::string sparql_createContentPart((char*)src_main_resources_sparql_createContentPart_sparql,src_main_resources_sparql_createContentPart_sparql_len);
const std::string sparql_deleteContentPart((char*)src_main_resources_sparql_deleteContentPart_sparql,src_main_resources_sparql_deleteContentPart_sparql_len);
const std::string sparql_listContentParts((char*)src_main_resources_sparql_listContentParts_sparql,src_main_resources_sparql_listContentParts_sparql_len);

// UUID generators
static random_generator rnd_gen;
static string_generator str_gen;

namespace mico {
  namespace persistence {


    ContentItem::ContentItem(const string baseUrl, const uuid& id) 
      : baseUrl(baseUrl), id(id)
      , metadata(baseUrl, boost::uuids::to_string(id) + SUFFIX_METADATA)
      , execution(baseUrl, boost::uuids::to_string(id) + SUFFIX_EXECUTION)
      , result(baseUrl, boost::uuids::to_string(id) + SUFFIX_RESULT)
    { };


    ContentItem::ContentItem(const string baseUrl, const URI& uri) 
      : baseUrl(baseUrl)
      , metadata(baseUrl, boost::uuids::to_string(id) + SUFFIX_METADATA)
      , execution(baseUrl, boost::uuids::to_string(id) + SUFFIX_EXECUTION)
      , result(baseUrl, boost::uuids::to_string(id) + SUFFIX_RESULT)
    {
      if(!starts_with(uri.stringValue(),baseUrl)) {
	throw string("the baseUrl is not a prefix of the URI, invalid argument");
      }

      id = str_gen(uri.stringValue().substr(baseUrl.length() + 1));
    }



    /**
     * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    Content* ContentItem::createContentPart() {
      uuid contentUUID = rnd_gen();

      Content* content = new Content(baseUrl,boost::uuids::to_string(id) + "/" + boost::uuids::to_string(contentUUID));

      map<string,string> params;
      params["ci"] = baseUrl + "/" + boost::uuids::to_string(id);
      params["cp"] = baseUrl + "/" + boost::uuids::to_string(id) + "/" + boost::uuids::to_string(contentUUID);

      metadata.update(sparql_format_query(sparql_createContentPart, params));

      return content;
    }

    /**
     * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * @param id the URI of the content part to create
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    Content* ContentItem::createContentPart(const URI& id) {
      Content* content = new Content(baseUrl,id);

      map<string,string> params;
      params["ci"] = baseUrl + "/" + boost::uuids::to_string(this->id);
      params["cp"] = id.stringValue();

      metadata.update(sparql_format_query(sparql_createContentPart, params));

      return content;
    }

    /**
     * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
     * content part.
     *
     * @param id the URI of the content part to return
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    Content* ContentItem::getContentPart(const URI& id) {
      map<string,string> params;
      params["ci"] = baseUrl + "/" + boost::uuids::to_string(this->id);
      params["cp"] = id.stringValue();

      if(metadata.ask(sparql_format_query(sparql_askContentPart,params))) {
	return new Content(baseUrl,id);
      } else {
	return NULL;
      }
    }


    /**
     * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
     * nothing.
     *
     * @param id the URI of the content part to delete
     */
    void ContentItem::deleteContentPart(const URI& id) {
      map<string,string> params;
      params["ci"] = baseUrl + "/" + boost::uuids::to_string(this->id);
      params["cp"] = id.stringValue();

      metadata.update(sparql_format_query(sparql_deleteContentPart, params));
    }


    /**
     * Convenient C++ style operator for accessing and constructing content parts. Returns
     * the content part with the given ID if found or a newly created content part if not found.
     */
    Content* ContentItem::operator[](const URI& id) {
      Content* r = getContentPart(id);
      if(!r) {
	r = createContentPart(id);
      }
      return r;
    }


    /**
     * Return an iterator over all content parts contained in this content item.
     *
     * @return an iterable that (lazily) iterates over the content parts
     */
    content_part_iterator ContentItem::begin() {
      map<string,string> params;
      params["ci"] = baseUrl + "/" + boost::uuids::to_string(id);

      const TupleResult* r = metadata.query(sparql_format_query(sparql_listContentParts,params));
      if(r->size() > 0) {
	return content_part_iterator(baseUrl,r);
      } else {
	delete r;
	return content_part_iterator();
      }
    }


    content_part_iterator ContentItem::end() {
      return content_part_iterator();
    };
  }
}
