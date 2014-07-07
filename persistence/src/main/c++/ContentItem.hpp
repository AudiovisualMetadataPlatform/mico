#include <iterator>
#include <boost/iterator/iterator_facade.hpp>

#include "Content.hpp"
#include "Metadata.hpp"

namespace mico {
  namespace persistence {


    class content_part_iterator;

    /**
     * Representation of a ContentItem. A ContentItem is a collection of ContentParts, e.g. an HTML page together with
     * its embedded images. ContentParts can be either original content or created during analysis.
     *
     * @author Sebastian Schaffert (sschaffert@apache.org)
     */
    class ContentItem {

    public:

      /**
       * Return the identifier (a unique URI) for this content item.
       *
       * @return
       */
      const URI& getID() const;

      /**
       * Return (read-only) content item metadata part of the initial content item, e.g. provenance information etc.
       *
       * TODO: could return a specialised Metadata object with fast access to commonly used properties once we know the
       *       schema
       *
       * @return a handle to a Metadata object that is suitable for reading
       */
      Metadata* getMetadata();

      /**
       * Return execution plan and metadata (e.g. dependencies, profiling information, execution information). Can be
       * updated by other components to add their execution information.
       *
       * TODO: could return a specialised Metadata object once we know the schema for execution metadata
       *
       * @return a handle to a Metadata object that is suitable for reading and updating
       */
      Metadata* getExecution();

      /**
       * Return the current state of the analysis result (as RDF metadata). Can be updated by other components to extend
       * the result with new information. This will hold the final analysis results.
       *
       * @return a handle to a Metadata object that is suitable for reading and updating
       */
      Metadata&* getResult();


      /**
       * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
       * content and metadata of the content part.
       *
       * @return a handle to a ContentPart object that is suitable for reading and updating
       */
      ContentPart* createContentPart();

      /**
       * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
       * content and metadata of the content part.
       *
       * @param id the URI of the content part to create
       * @return a handle to a ContentPart object that is suitable for reading and updating
       */
      ContentPart* createContentPart(const URI& id);

      /**
       * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
       * content part.
       *
       * @param id the URI of the content part to return
       * @return a handle to a ContentPart object that is suitable for reading and updating
       */
      ContentPart* getContentPart(const URI& id);


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
      ContentPart& operator[](const URI& id) = 0;


      /**
       * Return an iterator over all content parts contained in this content item.
       *
       * @return an iterable that (lazily) iterates over the content parts
       */
      content_part_iterator begin();


      content_part_iterator end();



    };


    class content_part_iterator  : public boost::iterator_facade<content_part_iterator, ContentPart, boost::forward_traversal_tag> {
      // TODO

    };

  }
}
