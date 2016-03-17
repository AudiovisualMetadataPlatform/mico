/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid_generators.hpp>


#include "PersistenceService.hpp"
#include "SPARQLUtil.hpp"

#include "Item.hpp"

using namespace std;
using namespace boost;
using namespace uuids;
using namespace mico::util;
using namespace mico::rdf::model;
using namespace mico::rdf::query;

// extern references to constant SPARQL templates
//SPARQL_INCLUDE(askContentItem);
//SPARQL_INCLUDE(createContentItem);
//SPARQL_INCLUDE(deleteContentItem);
//SPARQL_INCLUDE(listContentItems);
//SPARQL_INCLUDE(deleteGraph);

// UUID generators
static random_generator rnd_gen;

//static init
JNIEnv* mico::persistence::PersistenceService::m_sEnv = nullptr;
JavaVM* mico::persistence::PersistenceService::m_sJvm = nullptr;

namespace mico {
    namespace persistence {

        PersistenceService::PersistenceService(std::string serverAddress)
            : marmottaServerUrl("http://" + serverAddress + ":8080/marmotta")
            , contentDirectory("hdfs://" + serverAddress)
            , metadata("http://" + serverAddress + ":8080/marmotta")
        {
            initService();
        }

        PersistenceService::PersistenceService(std::string serverAddress, int marmottaPort, std::string user, std::string password)
                : marmottaServerUrl("http://" + serverAddress + ":" + std::to_string(marmottaPort) + "/marmotta")
                , contentDirectory("hdfs://" + serverAddress)
                , metadata("http://" + serverAddress + ":" + std::to_string(marmottaPort) + "/marmotta")
        {
            initService();
        }

        PersistenceService::PersistenceService(std::string marmottaServerUrl, std::string contentDirectory)
                : marmottaServerUrl(marmottaServerUrl), contentDirectory(contentDirectory), metadata(marmottaServerUrl)
        {
            initService();
        }

        void PersistenceService::initService()
        {
            std::string JavaClassPath="-Djava.class.path=";
            JavaClassPath +=  std::string("");

            //TODO: add classpath mechanism for persitence service

            JavaVMOption options[1];    // JVM invocation options
            options[0].optionString = (char *) JavaClassPath.c_str();

            JavaVMInitArgs vm_args;

            vm_args.version = JNI_VERSION_1_6;              // minimum Java version
            vm_args.nOptions = 1;
            vm_args.options = options;
            vm_args.ignoreUnrecognized = false;

            jint rc = JNI_CreateJavaVM(&PersistenceService::m_sJvm, (void**)&PersistenceService::m_sEnv, &vm_args);

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<OrgOpenrdfIdGeneratorIDGenerator> gen =
                EuMicoPlatformPersistenceImplIDGeneratorAnno4j::construct(jnipp::String::create(marmottaServerUrl));

            this->m_anno4j = ComGithubAnno4jAnno4j::construct(gen);

            while (jnipp::Env::hasException()) {
                jnipp::LocalRef<JavaLangException> ex =  jnipp::Env::getException();
                std::string collected_exeptions;
                std::string collected_messages;
                collected_exeptions += ", " + ex->getClass()->getName()->std_str();
                collected_messages += ", " + ex->getMessage()->std_str();

                throw std::runtime_error("Java exceptions occured: " + collected_exeptions +
                                         ", messages: " + collected_messages);
            }
        }


        /**
        * Create a new content item with a random URI and return it. The content item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return a handle to the newly created ContentItem
        */
        Item *PersistenceService::createItem() {
            uuid UUID = rnd_gen();

            jnipp::LocalRef<EuMicoPlatformAnno4jModelItemMMM> jItemMMM =
                    this->m_anno4j->createObject(EuMicoPlatformAnno4jModelItemMMM::clazz());

            //String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

            // call persist to move item to corresponding sub-graph
//            URIImpl context = new URIImpl(itemMMM.getResourceAsString());
//            anno4j.persist(itemMMM, context);

//            itemMMM.setSerializedAt(dateTime);
//            log.trace("Created Item with id {} in the corresponding context graph", itemMMM.getResourceAsString());

//            return new ItemAnno4j(itemMMM, this);
//                 } catch (IllegalAccessException e) {
//                     throw new RepositoryException("Illegal access", e);
//                 } catch (InstantiationException e) {
//                     throw new RepositoryException("Could not instantiate ItemMMM", e);
//                 }

//            ContentItem* ci = new ContentItem(marmottaServerUrl,contentDirectory,UUID);

//            map<string,string> params;
//            params["g"] = marmottaServerUrl;
//            params["ci"] = marmottaServerUrl + "/" + boost::uuids::to_string(UUID);

//            metadata.update(SPARQL_FORMAT(createItem, params));

//            return ci;
        }      

        /**
        * Return the content item with the given URI if it exists. The content item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return a handle to the ContentItem with the given URI, or null if it does not exist
        */
        ContentItem* PersistenceService::getContentItem(const URI& id) {
//            map<string,string> params;
//            params["g"]  = marmottaServerUrl;
//            params["ci"] = id.stringValue();

//            if(metadata.ask(SPARQL_FORMAT(askContentItem,params))) {
//                return new ContentItem(marmottaServerUrl,contentDirectory,id);
//            } else {
                return NULL;
//            }
        }

        /**
        * Delete the content item with the given URI. If the content item does not exist, do nothing.
        */
        void PersistenceService::deleteContentItem(const URI& id) {
//            map<string,string> params;
//            params["g"] = marmottaServerUrl;
//            params["ci"] = id.stringValue();

//            metadata.update(SPARQL_FORMAT(deleteContentItem, params));

//            params["g"] = id.stringValue() + SUFFIX_METADATA;
//            metadata.update(SPARQL_FORMAT(deleteGraph, params));

//            params["g"] = id.stringValue() + SUFFIX_EXECUTION;
//            metadata.update(SPARQL_FORMAT(deleteGraph, params));

//            params["g"] = id.stringValue() + SUFFIX_RESULT;
//            metadata.update(SPARQL_FORMAT(deleteGraph, params));
        }

        /**
        * Return an iterator over all currently available content items.
        *
        * @return iterable
        */
        content_item_iterator PersistenceService::begin() {
//            map<string,string> params;
//            params["g"] = marmottaServerUrl;

//            const TupleResult* r = metadata.query(SPARQL_FORMAT(listContentItems,params));
//            if(r->size() > 0) {
//                return content_item_iterator(marmottaServerUrl,contentDirectory,r);
//            } else {
//                delete r;
                return content_item_iterator(marmottaServerUrl,contentDirectory);
//            }
        }


        content_item_iterator PersistenceService::end() {
            return content_item_iterator(marmottaServerUrl,contentDirectory);
        }



        void content_item_iterator::increment() {
            pos = pos+1 == result->size() ? -1 : pos + 1;
        };

        bool content_item_iterator::equal(content_item_iterator const& other) const {
            return this->pos == other.pos;
        };

        ContentItem* content_item_iterator::dereference() const {
            return new ContentItem(baseUrl, contentDirectory, *dynamic_cast<const URI*>( result->at(pos).at("p") ) );
        }

        jnipp::LocalRef<ComGithubAnno4jAnno4j> PersistenceService::getAnno4j()
        {
          //return m_anno4j;
          throw runtime_error("Not yet implemented");
        }

        std::string PersistenceService::getStoragePrefix()
        {
          return m_storagePrefix;
        }

    }
}
