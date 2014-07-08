#ifndef HAVE_PERSISTENCE_SERVICE_H
#define HAVE_PERSISTENCE_SERVICE_H 1

#include <string>
#include <iterator>

#include <boost/iterator/iterator_facade.hpp>


#include "Metadata.hpp"
#include "ContentItem.hpp"

#include "rdf_model.hpp"
#include "rdf_query.hpp"

namespace mico {
  namespace persistence {

    using namespace mico::rdf::model;   
    using namespace mico::rdf::query;   

    class content_item_iterator;

    class PersistenceMetadata : public Metadata {
      friend class PersistenceService;

    protected:
      PersistenceMetadata(string baseUrl) : Metadata(baseUrl)  {};

    };


    class PersistenceService {

    private:

      std::string marmottaServerUrl;
      PersistenceMetadata metadata;

    public:

      PersistenceService(std::string marmottaServerUrl) : marmottaServerUrl(marmottaServerUrl), metadata(marmottaServerUrl) {};


      /**
       * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
       * about existing content items.
       *
       * @return
       */
      PersistenceMetadata& getMetadata() { return metadata; };

      /**
       * Create a new content item with a random URI and return it. The content item should be suitable for reading and
       * updating and write all updates to the underlying low-level persistence layer.
       *
       * @return a handle to the newly created ContentItem
       */
      ContentItem* createContentItem();

      /**
       * Create a new content item with the given URI and return it. The content item should be suitable for reading and
       * updating and write all updates to the underlying low-level persistence layer.
       *
       * @return a handle to the newly created ContentItem
       */
      ContentItem* createContentItem(const URI& id);


      /**
       * Return the content item with the given URI if it exists. The content item should be suitable for reading and
       * updating and write all updates to the underlying low-level persistence layer.
       *
       * @return a handle to the ContentItem with the given URI, or null if it does not exist
       */
      ContentItem* getContentItem(const URI& id);

      /**
       * Delete the content item with the given URI. If the content item does not exist, do nothing.
       */
      void deleteContentItem(const URI& id);

      /**
       * Return an iterator over all currently available content items.
       *
       * @return iterable
       */
      content_item_iterator begin();


      content_item_iterator end();



    };


    class content_item_iterator  : public boost::iterator_facade<content_item_iterator, ContentItem*, boost::forward_traversal_tag, ContentItem*> {
    private:
      int pos;
      string baseUrl;
      const TupleResult* result;

    public:
      content_item_iterator() : baseUrl(""), pos(-1), result(NULL) {};
      content_item_iterator(const string baseUrl, const TupleResult* r) : baseUrl(baseUrl), pos(0), result(r) {};
      ~content_item_iterator() { if(result) { delete result; } };

      
    private:

      friend class boost::iterator_core_access;

      inline void increment() { pos = pos+1 == result->size() ? -1 : pos + 1; };

      inline bool equal(content_item_iterator const& other) const { return this->pos == other.pos; };

      inline ContentItem* dereference() const { 
	return new ContentItem(baseUrl, *dynamic_cast<const URI*>( result->at(pos).at("p") ) ); 
      }

    };
  }
}

#endif
