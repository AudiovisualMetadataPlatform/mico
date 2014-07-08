#ifndef HAVE_CONTENTITEM_H
#define HAVE_CONTENTITEM_H 1

#include <iterator>
#include <string>

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/iterator/iterator_facade.hpp>

#include "Content.hpp"
#include "Metadata.hpp"

#include "rdf_model.hpp"
#include "rdf_query.hpp"

namespace mico {
  namespace persistence {

    using std::string;
    using namespace boost::uuids;
    using namespace mico::rdf::model;   


    /**
     * Specialised support for content item metadata of content item processing. Currently just an
     * empty subclass of Metadata.
     */
    class ContentItemMetadata : public Metadata {

      friend class ContentItem;

    protected:
      ContentItemMetadata(string baseUrl, string context) : Metadata(baseUrl, context)  {};

    };


    /**
     * Specialised support for execution metadata of content item processing. Currently just an
     * empty subclass of Metadata.
     */
    class ExecutionMetadata : public Metadata {

      friend class ContentItem;

    protected:
      ExecutionMetadata(string baseUrl, string context) : Metadata(baseUrl, context)  {};

    };


    /**
     * Specialised support for result metadata of content item processing. Currently just an
     * empty subclass of Metadata.
     */
    class ResultMetadata : public Metadata {

      friend class ContentItem;

    protected:
      ResultMetadata(string baseUrl, string context) : Metadata(baseUrl, context)  {};

    };


    class content_part_iterator;

    // suffixes for named graph URIs of a content item
    const string SUFFIX_METADATA  = "-metadata";
    const string SUFFIX_EXECUTION = "-execution";
    const string SUFFIX_RESULT    = "-result";

    /**
     * Representation of a ContentItem. A ContentItem is a collection of ContentParts, e.g. an HTML page together with
     * its embedded images. ContentParts can be either original content or created during analysis.
     *
     * @author Sebastian Schaffert (sschaffert@apache.org)
     */
    class ContentItem {
    protected:
      string baseUrl;
      uuid id;

      ContentItemMetadata metadata;
      ExecutionMetadata   execution;
      ResultMetadata      result;

    public:
      ContentItem(string baseUrl, uuid& id);

      ContentItem(string baseUrl, URI& uri);


      /**
       * Return the identifier (a unique URI) for this content item.
       *
       * @return
       */
      inline const URI getURI() const { return URI(baseUrl + "/" + boost::uuids::to_string(id)); };

      /**
       * Return (read-only) content item metadata part of the initial content item, e.g. provenance information etc.
       *
       * TODO: could return a specialised Metadata object with fast access to commonly used properties once we know the
       *       schema
       *
       * @return a handle to a Metadata object that is suitable for reading
       */
      ContentItemMetadata& getMetadata() { return metadata; };

      /**
       * Return execution plan and metadata (e.g. dependencies, profiling information, execution information). Can be
       * updated by other components to add their execution information.
       *
       * TODO: could return a specialised Metadata object once we know the schema for execution metadata
       *
       * @return a handle to a Metadata object that is suitable for reading and updating
       */
      ExecutionMetadata& getExecution() { return execution; }

      /**
       * Return the current state of the analysis result (as RDF metadata). Can be updated by other components to extend
       * the result with new information. This will hold the final analysis results.
       *
       * @return a handle to a Metadata object that is suitable for reading and updating
       */
      ResultMetadata& getResult() { return result; }


      /**
       * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
       * content and metadata of the content part.
       *
       * @return a handle to a ContentPart object that is suitable for reading and updating
       */
      Content* createContentPart();

      /**
       * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
       * content and metadata of the content part.
       *
       * @param id the URI of the content part to create
       * @return a handle to a ContentPart object that is suitable for reading and updating
       */
      Content* createContentPart(const URI& id);

      /**
       * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
       * content part.
       *
       * @param id the URI of the content part to return
       * @return a handle to a ContentPart object that is suitable for reading and updating
       */
      Content* getContentPart(const URI& id);


      /**
       * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
       * nothing.
       *
       * @param id the URI of the content part to delete
       */
      void deleteContentPart(const URI& id);


      /**
       * Convenient C++ style operator for accessing and constructing content parts. Returns
       * the content part with the given ID if found or a newly created content part if not found.
       */
      Content* operator[](const URI& id);


      /**
       * Return an iterator over all content parts contained in this content item.
       *
       * @return an iterable that (lazily) iterates over the content parts
       */
      content_part_iterator begin();


      content_part_iterator end();



    };


    class content_part_iterator  : public boost::iterator_facade<content_part_iterator, Content*, boost::forward_traversal_tag, Content*> {
    private:
      int pos;
      string baseUrl;
      const TupleResult* result;

    public:
      content_part_iterator() : baseUrl(""), pos(-1), result(NULL) {};
      content_part_iterator(const string baseUrl, const TupleResult* r) : baseUrl(baseUrl), pos(0), result(r) {};
      ~content_part_iterator() { if(result) { delete result; } };

      
    private:

      friend class boost::iterator_core_access;

      inline void increment() { pos = pos+1 == result->size() ? -1 : pos + 1; };

      inline bool equal(content_part_iterator const& other) const { return this->pos == other.pos; };

      inline Content* dereference() const { 
	return new Content(baseUrl, *dynamic_cast<const URI*>( result->at(pos).at("p") ) ); 
      }

    };

  }
}
#endif
