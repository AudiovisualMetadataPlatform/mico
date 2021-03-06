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


        /**
        * Specialised support for content item metadata of content item processing. Currently just an
        * empty subclass of Metadata.
        */
        class ContentItemMetadata : public Metadata {

            friend class ContentItem;

        protected:
            ContentItemMetadata(std::string baseUrl, std::string context) : Metadata(baseUrl, context)  {};

        };


        /**
        * Specialised support for execution metadata of content item processing. Currently just an
        * empty subclass of Metadata.
        */
        class ExecutionMetadata : public Metadata {

            friend class ContentItem;

        protected:
            ExecutionMetadata(std::string baseUrl, std::string context) : Metadata(baseUrl, context)  {};

        };


        /**
        * Specialised support for result metadata of content item processing. Currently just an
        * empty subclass of Metadata.
        */
        class ResultMetadata : public Metadata {

            friend class ContentItem;

        protected:
            ResultMetadata(std::string baseUrl, std::string context) : Metadata(baseUrl, context)  {};

        };


        class content_part_iterator;

        // suffixes for named graph URIs of a content item
        const std::string SUFFIX_METADATA  = "-metadata";
        const std::string SUFFIX_EXECUTION = "-execution";
        const std::string SUFFIX_RESULT    = "-result";

        /**
        * Representation of a ContentItem. A ContentItem is a collection of ContentParts, e.g. an HTML page together with
        * its embedded images. ContentParts can be either original content or created during analysis.
        *
        * @author Sebastian Schaffert (sschaffert@apache.org)
        */
        class ContentItem {

            friend bool operator==(const ContentItem& ci1, const ContentItem& ci2);

        protected:
            const std::string& baseUrl;
            const std::string& contentDirectory;
            boost::uuids::uuid id;

            ContentItemMetadata metadata;
            ExecutionMetadata   execution;
            ResultMetadata      result;

        public:
            /**
            * Create a new content item using the given server base URL and a unique UUID as content item
            * identifier. The URI of the content item will be composed of the base URL and the id.
            *
            * @param baseUrl base URL of the marmotta server
            * @param id      a unique UUID identifying the content item
            */
            ContentItem(const std::string& baseUrl, const std::string& contentDirectory, const boost::uuids::uuid& id);

            /**
            * Create a new content item using the given server base URL and a URI as content item
            * identifier. The base URL must be a prefix of the URI of the content item.
            *
            * @param baseUrl base URL of the marmotta server
            * @param uri     a unique URI identifying the content item (must have baseUrl as prefix)
            */
            ContentItem(const std::string& baseUrl, const std::string& contentDirectory, const mico::rdf::model::URI& uri);

            /**
            * Return the (base URL) used by this content item
            *
            * @return The base url as string
            */
            inline  std::string getBaseUrl() const { return baseUrl; }


            /**
            * Return the identifier (a unique URI) for this content item.
            *
            * @return the URI identifying this content item
            */
            inline const mico::rdf::model::URI getURI() const { return mico::rdf::model::URI(baseUrl + "/" + boost::uuids::to_string(id)); }

            /**
            * Return (read-only) content item metadata part of the initial content item, e.g. provenance information etc.
            *
            * TODO: could return a specialised Metadata object with fast access to commonly used properties once we know the
            *       schema
            *
            * @return a handle to a Metadata object that is suitable for reading
            */
            ContentItemMetadata& getMetadata() { return metadata; }

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
            Content* createContentPart(const mico::rdf::model::URI& id);

            /**
            * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
            * content part.
            *
            * @param id the URI of the content part to return
            * @return a handle to a ContentPart object that is suitable for reading and updating
            */
            Content* getContentPart(const mico::rdf::model::URI& id);


            /**
            * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
            * nothing.
            *
            * @param id the URI of the content part to delete
            */
            void deleteContentPart(const mico::rdf::model::URI& id);


            /**
            * Convenient C++ style operator for accessing and constructing content parts. Returns
            * the content part with the given ID if found or a newly created content part if not found.
            */
            Content* operator[](const mico::rdf::model::URI& id);


            /**
            * Return an iterator over all content parts contained in this content item.
            *
            * @return an iterable that (lazily) iterates over the content parts
            */
            content_part_iterator begin();


            /**
            * Return the end iterator for content parts. Can be used to check when iteration is completed.
            */
            content_part_iterator end();



        };

#ifndef DOXYGEN_SHOULD_SKIP_THIS
        /**
        * Internal implementation of iterators over the parts of a content item. Uses Boost
        * iterator_facade to simplify the implementation.
        */
        class content_part_iterator  : public boost::iterator_facade<content_part_iterator, Content*, boost::forward_traversal_tag, Content*> {
        private:
            int pos;
            ContentItem& item;
            const std::string& baseUrl;
            const std::string& contentDirectory;
            const mico::rdf::query::TupleResult* result;

        public:
            content_part_iterator(ContentItem& item, const std::string& baseUrl, const std::string& contentDirectory)
                    : pos(-1), item(item), baseUrl(baseUrl), contentDirectory(contentDirectory), result(NULL) {}

            content_part_iterator(ContentItem& item, const std::string& baseUrl, const std::string& contentDirectory, const mico::rdf::query::TupleResult* r)
                    :  pos(0), item(item), baseUrl(baseUrl), contentDirectory(contentDirectory), result(r) {}

            ~content_part_iterator() { if(result) { delete result; } }


        private:

            friend class boost::iterator_core_access;

            void increment();
            bool equal(content_part_iterator const& other) const;
            Content* dereference() const;

        };
#endif

        inline bool operator==(const ContentItem& ci1, const ContentItem& ci2) {
            return ci1.baseUrl == ci2.baseUrl && ci1.id == ci2.id;
        }
    }
}
#endif
