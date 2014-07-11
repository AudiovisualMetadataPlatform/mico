#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid_generators.hpp>


#include "PersistenceService.hpp"
#include "SPARQLUtil.hpp"

using namespace std;
using namespace boost;
using namespace uuids;
using namespace mico::util;

// extern references to constant SPARQL templates
extern unsigned char src_main_resources_sparql_askContentItem_sparql[];
extern unsigned int src_main_resources_sparql_askContentItem_sparql_len;
extern unsigned char src_main_resources_sparql_createContentItem_sparql[];
extern unsigned int src_main_resources_sparql_createContentItem_sparql_len;
extern unsigned char src_main_resources_sparql_deleteContentItem_sparql[];
extern unsigned int src_main_resources_sparql_deleteContentItem_sparql_len;
extern unsigned char src_main_resources_sparql_listContentItems_sparql[];
extern unsigned int src_main_resources_sparql_listContentItems_sparql_len;
extern unsigned char src_main_resources_sparql_deleteGraph_sparql[];
extern unsigned int src_main_resources_sparql_deleteGraph_sparql_len;

const std::string sparql_askContentItem((char*)src_main_resources_sparql_askContentItem_sparql, src_main_resources_sparql_askContentItem_sparql_len);
const std::string sparql_createContentItem((char*)src_main_resources_sparql_createContentItem_sparql,src_main_resources_sparql_createContentItem_sparql_len);
const std::string sparql_deleteContentItem((char*)src_main_resources_sparql_deleteContentItem_sparql,src_main_resources_sparql_deleteContentItem_sparql_len);
const std::string sparql_listContentItems((char*)src_main_resources_sparql_listContentItems_sparql,src_main_resources_sparql_listContentItems_sparql_len);
const std::string sparql_deleteGraph((char*)src_main_resources_sparql_deleteGraph_sparql,src_main_resources_sparql_deleteGraph_sparql_len);

// UUID generators
static random_generator rnd_gen;
static string_generator str_gen;


namespace mico {
  namespace persistence {

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    ContentItem* PersistenceService::createContentItem() {
      uuid UUID = rnd_gen();

      ContentItem* ci = new ContentItem(marmottaServerUrl,contentDirectory,UUID);

      map<string,string> params;
      params["g"] = marmottaServerUrl;
      params["ci"] = marmottaServerUrl + "/" + boost::uuids::to_string(UUID);

      metadata.update(sparql_format_query(sparql_createContentItem, params));

      return ci;
    }

    /**
     * Create a new content item with the given URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    ContentItem* PersistenceService::createContentItem(const URI& id) {
      ContentItem* ci = new ContentItem(marmottaServerUrl, contentDirectory, id);

      map<string,string> params;
      params["g"] = marmottaServerUrl;
      params["ci"] = id.stringValue();

      metadata.update(sparql_format_query(sparql_createContentItem, params));

      return ci;
    }


    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the ContentItem with the given URI, or null if it does not exist
     */
    ContentItem* PersistenceService::getContentItem(const URI& id) {
      map<string,string> params;
      params["g"]  = marmottaServerUrl;
      params["ci"] = id.stringValue();

      if(metadata.ask(sparql_format_query(sparql_askContentItem,params))) {
		return new ContentItem(marmottaServerUrl,contentDirectory,id);
      } else {
		return NULL;
      }
    }

    /**
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    void PersistenceService::deleteContentItem(const URI& id) {
      map<string,string> params;
      params["g"] = marmottaServerUrl;
      params["ci"] = id.stringValue();

      metadata.update(sparql_format_query(sparql_deleteContentItem, params));

      params["g"] = id.stringValue() + SUFFIX_METADATA;
      metadata.update(sparql_format_query(sparql_deleteGraph, params));

      params["g"] = id.stringValue() + SUFFIX_EXECUTION;
      metadata.update(sparql_format_query(sparql_deleteGraph, params));

      params["g"] = id.stringValue() + SUFFIX_RESULT;
      metadata.update(sparql_format_query(sparql_deleteGraph, params));
    }

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    content_item_iterator PersistenceService::begin() {
      map<string,string> params;
      params["g"] = marmottaServerUrl;

      const TupleResult* r = metadata.query(sparql_format_query(sparql_listContentItems,params));
      if(r->size() > 0) {
		return content_item_iterator(marmottaServerUrl,contentDirectory,r);
      } else {
		delete r;
		return content_item_iterator(marmottaServerUrl,contentDirectory);
      }
    }


    content_item_iterator PersistenceService::end() {
      return content_item_iterator(marmottaServerUrl,contentDirectory);
    }

  }
}
