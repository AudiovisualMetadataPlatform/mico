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
SPARQL_INCLUDE(askContentItem);
SPARQL_INCLUDE(createContentItem);
SPARQL_INCLUDE(deleteContentItem);
SPARQL_INCLUDE(listContentItems);
SPARQL_INCLUDE(deleteGraph);

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

      metadata.update(SPARQL_FORMAT(createContentItem, params));

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

      metadata.update(SPARQL_FORMAT(createContentItem, params));

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

      if(metadata.ask(SPARQL_FORMAT(askContentItem,params))) {
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

      metadata.update(SPARQL_FORMAT(deleteContentItem, params));

      params["g"] = id.stringValue() + SUFFIX_METADATA;
      metadata.update(SPARQL_FORMAT(deleteGraph, params));

      params["g"] = id.stringValue() + SUFFIX_EXECUTION;
      metadata.update(SPARQL_FORMAT(deleteGraph, params));

      params["g"] = id.stringValue() + SUFFIX_RESULT;
      metadata.update(SPARQL_FORMAT(deleteGraph, params));
    }

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    content_item_iterator PersistenceService::begin() {
      map<string,string> params;
      params["g"] = marmottaServerUrl;

      const TupleResult* r = metadata.query(SPARQL_FORMAT(listContentItems,params));
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