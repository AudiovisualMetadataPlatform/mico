#ifndef HAVE_PERSISTENCE_SERVICE_H
#define HAVE_PERSISTENCE_SERVICE_H 1

#include <string>
#include <iterator>
#include <memory>

#include <boost/iterator/iterator_facade.hpp>


#include "Metadata.hpp"
#include "ContentItem.hpp"
#include "anno4cpp.h"

#include "rdf_model.hpp"
#include "rdf_query.hpp"



namespace mico {
    namespace persistence {

        using namespace mico::rdf::query;

        class content_item_iterator;

        namespace model {
          class Item;
        }


        /**
        * Specialised support for persistence service metadata. Might in the future be extended with
        * additional methods for simplified use of certain vocabularies.
        */
        class PersistenceMetadata : public Metadata {
            friend class PersistenceService;

        protected:
            PersistenceMetadata(std::string baseUrl) : Metadata(baseUrl)  {};

        };


        /**
        * Main service for accessing the MICO persistence API. The persistence service can be used for
        * managing ContentItems stored in the peristence API of the MICO platform. Services working
        * with ContentItems should only use instances of this class for accessing the persistence API.
        */
        class PersistenceService {

        private:

            std::string marmottaServerUrl;
            std::string contentDirectory;
            PersistenceMetadata metadata;
            std::string m_storagePrefix;
            std::string m_jniErrorMessage;

            static JNIEnv* m_sEnv;

            jnipp::GlobalRef<ComGithubAnno4jAnno4j> m_anno4j;


            /** Inits Java VM and connects to Marmotta via JNI->anno4j */
            void initService();

            void setContext(jnipp::Ref<OrgOpenrdfRepositoryObjectObjectConnection> con, jnipp::Ref<OrgOpenrdfModelURI> context);


        public:
            static JavaVM* m_sJvm;

            /**
            * Initialise persistence service with the address of a server running the standard installation of
            * the MICO platform with Marmotta at port 8080 under context /marmotta, RabbitMQ at port 5672, and
            * an HDFS server, all with login/password mico/mico.
            */
            PersistenceService(std::string serverAddress);


            /**
            * Initialise persistence service with the address of a server running the standard installation of
            * the MICO platform with Marmotta at port 8080 under context /marmotta, RabbitMQ at port 5672, and
            * an HDFS server, all with login/password mico/mico.
            */
            PersistenceService(std::string serverAddress, int marmottaPort, std::string user, std::string password);


            /**
            * Initialise an instance of the PersistenceService using the Marmotta server with the given
            * URL as backend.
            *
            * @param marmottaServerUrl the URL of the Apache Marmotta server, e.g. http://localhost:8080/marmotta
            */
            PersistenceService(std::string marmottaServerUrl, std::string contentDirectory);


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
            std::shared_ptr<model::Item> createItem();

            /**
            * Return the item with the given URI if it exists. The item should be suitable for reading and
            * updating and write all updates to the underlying low-level persistence layer.
            *
            * @return A handle to the Item with the given URI, or null if it does not exist
            */
            std::shared_ptr<model::Item> getItem(const mico::rdf::model::URI& id);

            /**
            * Delete the content item with the given URI. If the content item does not exist, do nothing.
            */
            void deleteItem(const mico::rdf::model::URI& id);

            /**
            * Return an iterator over all currently available content items.
            *public void deleteItem(URI id) throws RepositoryException {
        anno4j.clearContext(id);
        log.trace("Deleted item with id {} including all triples in the corresponding context graph", id.toString());
    }
            * @return iterable
            */
            content_item_iterator begin();


            /**
            * Return the end iterator for checking when iteration has completed.
            */
            content_item_iterator end();


            jnipp::LocalRef<jnipp::com::github::anno4j::Anno4j> getAnno4j();

            std::string getStoragePrefix();

            std::string getContentDirectory();
        };


#ifndef DOXYGEN_SHOULD_SKIP_THIS
        /**
        * 	Internal implementation of iterators over the content items managed by a PersistenceService
        */
        class content_item_iterator  : public boost::iterator_facade<content_item_iterator, ContentItem*, boost::forward_traversal_tag, ContentItem*> {
        private:
            int pos;
            const std::string& baseUrl;
            const std::string& contentDirectory;
            const mico::rdf::query::TupleResult* result;

        public:
            content_item_iterator(const std::string& baseUrl, const std::string& contentDirectory)
                    : pos(-1), baseUrl(baseUrl), contentDirectory(contentDirectory), result(NULL) {};

            content_item_iterator(const std::string& baseUrl, const std::string& contentDirectory, const mico::rdf::query::TupleResult* r)
                    : pos(0), baseUrl(baseUrl), contentDirectory(contentDirectory), result(r) {};

            ~content_item_iterator() { if(result) { delete result; } };


        private:

            friend class boost::iterator_core_access;

            void increment();
            bool equal(content_item_iterator const& other) const;
            ContentItem* dereference() const;

        };
#endif
    }
}

#endif
