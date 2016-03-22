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

#include <ctime>
#include <iomanip>
#include <cassert>

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/uuid_generators.hpp>


#include "PersistenceService.hpp"
#include "SPARQLUtil.hpp"
#include "FileOperations.h"
#include "TimeInfo.h"
#include "Logging.hpp"
#include "JnippExcpetionHandling.hpp"

#include "ItemAnno4cpp.hpp"


#ifndef ANNO4JDEPENDENCIES_PATH
    #define ANNO4JDEPENDENCIES_PATH ""
#endif

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
jnipp::GlobalRef<ComGithubAnno4jAnno4j> mico::persistence::PersistenceService::m_sAnno4j;


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
            std::string exceptionMsg;

            log::set_log_level(log::DEBUG);

            if (!m_sEnv || ! m_sJvm) {
                std::vector<std::string> filePatterns = {std::string(".*anno4jdependencies.*")};
                std::vector<std::string> paths =
                    {std::string(ANNO4JDEPENDENCIES_PATH),"/usr/share","/usr/local/share"};

                std::map<std::string,std::string> jar_file =
                      commons::FileOperations::findFiles(filePatterns, paths);

                if (jar_file.size() == 0) {
                    throw std::runtime_error("Could not find appropriate anno4jdependencies jar.");
                }

                std::string JavaClassPath="-Djava.class.path=";
                JavaClassPath +=  jar_file[".*anno4jdependencies.*"];

                JavaVMOption options[1];    // JVM invocation options
                options[0].optionString = (char *) JavaClassPath.c_str();
                //options[1].optionString = "-Xint";

                JavaVMInitArgs vm_args;

                vm_args.version = JNI_VERSION_1_6;              // minimum Java version
                vm_args.nOptions = 1;
                vm_args.options = options;
                vm_args.ignoreUnrecognized = false;

                jint rc = JNI_CreateJavaVM(&PersistenceService::m_sJvm, (void**)&PersistenceService::m_sEnv, &vm_args);
            }

            LOG_DEBUG("######################################################## JavaVM initialized");

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<JavaLangString> jURIString = jnipp::String::create(marmottaServerUrl);

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject)jURIString != 0);

            LOG_DEBUG("######################################################## jURIString created");

            jnipp::LocalRef<OrgOpenrdfIdGeneratorIDGenerator> gen =
                EuMicoPlatformPersistenceImplIDGeneratorAnno4j::construct(jURIString);

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject)gen != 0);

            LOG_DEBUG("######################################################## IDGeneratorAnno4j  created");

            jnipp::LocalRef<OrgOpenrdfRepositorySparqlSPARQLRepository> sparqlRepository =
                   OrgOpenrdfRepositorySparqlSPARQLRepository::construct(
                  jnipp::String::create(marmottaServerUrl + std::string("/sparql/select")),
                  jnipp::String::create(marmottaServerUrl + std::string("/sparql/update")));

            LOG_DEBUG("######################################################## SPARQLRepository  created");

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject)sparqlRepository != 0);

            sparqlRepository->initialize();

            assert(sparqlRepository->isInitialized());

            m_sAnno4j = ComGithubAnno4jAnno4j::construct(sparqlRepository,gen);
            checkJavaExcpetionNoThrow(exceptionMsg);

            LOG_DEBUG("######################################################## anno4j object created.");
        }


        /**
        * Create a new item with a random URI and return it. The item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return a handle to the newly created Item
        */
        std::shared_ptr<Item> PersistenceService::createItem() {

            std::string exceptionMsg;

            LOG_DEBUG("######################################################## createItem called");
            assert((jobject) m_sAnno4j);

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            uuid UUID = rnd_gen();

            auto jItemMMM =
                    m_sAnno4j->createObject(EuMicoPlatformAnno4jModelItemMMM::clazz());

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject) jItemMMM);

            LOG_DEBUG("########################################################  ItemMMM created");

            jnipp::LocalRef<jnipp::String> jDateTime =
                    jnipp::String::create(commons::TimeInfo::getTimestamp());

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject) jDateTime);

            LOG_DEBUG("########################################################  jDateTime created");

            auto sContextURI =
                    ((jnipp::Ref<ComGithubAnno4jModelImplResourceObject>) jItemMMM )->getResourceAsString();

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject) sContextURI);

            LOG_DEBUG("########################################################  sContextURI retrieved");

             jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> contextFromInject =
                    OrgOpenrdfModelImplURIImpl::construct(sContextURI);

            LOG_DEBUG("########################################################  contextFromInject created");

            checkJavaExcpetionNoThrow(exceptionMsg);
            assert((jobject) contextFromInject);

            m_sAnno4j->persist(jItemMMM);

            LOG_DEBUG("########################################################  persist called ");

            ((jnipp::Ref<EuMicoPlatformAnno4jModelItemMMM>) jItemMMM)->setSerializedAt(jDateTime);

            checkJavaExcpetionNoThrow(exceptionMsg);

            //auto newItem = std::make_shared<ItemAnno4cpp>(jItemMMM, *this);

            LOG_DEBUG("######################################################## ItemMMM created and Item wrapper returned");

            return nullptr;
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
