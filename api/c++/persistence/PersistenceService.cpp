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

#include <anno4cpp.h>

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
                //JavaClassPath +=  "/home/christian/mico/anno4cpp/java/anno4jdependencies/target/anno4jdependencies-2.0.0-SNAPSHOT.jar";

                JavaVMOption options[1];    // JVM invocation options
                options[0].optionString = (char *) JavaClassPath.c_str();
//                options[1].optionString = "-Xdebug";
//                options[2].optionString = "-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n";
                //options[1].optionString = "-Xint";

                JavaVMInitArgs vm_args;

                vm_args.version = JNI_VERSION_1_6;              // minimum Java version
                vm_args.nOptions = 1;
                vm_args.options = options;
                vm_args.ignoreUnrecognized = false;

                jint rc = JNI_CreateJavaVM(&PersistenceService::m_sJvm, (void**)&PersistenceService::m_sEnv, &vm_args);


            }
            LOG_INFO("JavaVM initialized");

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            checkJavaExcpetionNoThrow(m_jniErrorMessage);

            jnipp::LocalRef<JavaLangString> jURIString = jnipp::String::create(marmottaServerUrl);

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject)jURIString != 0);

            LOG_INFO("Using Marmotta URI: %s", jURIString->std_str().c_str());

            jnipp::LocalRef<OrgOpenrdfIdGeneratorIDGenerator> gen =
                EuMicoPlatformPersistenceImplIDGeneratorAnno4j::construct(jURIString);

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject)gen != 0);

            LOG_DEBUG("IDGeneratorAnno4j  created");

            jnipp::LocalRef<OrgOpenrdfRepositorySparqlSPARQLRepository> sparqlRepository =
                   OrgOpenrdfRepositorySparqlSPARQLRepository::construct(
                  jnipp::String::create(marmottaServerUrl + std::string("/sparql/select")),
                  jnipp::String::create(marmottaServerUrl + std::string("/sparql/update")));

            LOG_DEBUG("SPARQLRepository  created");

            checkJavaExcpetionNoThrow(m_jniErrorMessage);

            m_anno4j = ComGithubAnno4jAnno4j::construct(sparqlRepository,gen);

            checkJavaExcpetionNoThrow(m_jniErrorMessage);

            LOG_DEBUG("anno4j object created.");
        }


        /**
        * Create a new item with a random URI and return it. The item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return a handle to the newly created Item
        */
        std::shared_ptr<Item> PersistenceService::createItem() {           

            LOG_DEBUG("PersistenceService::createItem()");
            assert((jobject) m_anno4j);

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::GlobalRef<EuMicoPlatformAnno4jModelItemMMM> jNewItemMMM =
                    m_anno4j->createObject(EuMicoPlatformAnno4jModelItemMMM::clazz());

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject) jNewItemMMM);

            LOG_DEBUG("item created with anno4j");

            auto jItemConn = ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)jNewItemMMM)->getObjectConnection();

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject) jItemConn);
            LOG_DEBUG("item connection retrieved");

            jnipp::LocalRef<OrgOpenrdfSailMemoryModelMemValueFactory> jMemValueFactory =
                    OrgOpenrdfSailMemoryModelMemValueFactory::construct();

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject) jMemValueFactory);

            jnipp::Ref<OrgOpenrdfModelResource> jResourceBlank =
                    jMemValueFactory->createURI(jnipp::String::create("urn:anno4j:BLANK"));

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject) jResourceBlank);

            LOG_DEBUG("blank resource created");

            jnipp::LocalRef<OrgOpenrdfModelURI> jItemURI =
                    ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)jNewItemMMM)->getResource();

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            assert((jobject) jItemURI);
            LOG_DEBUG("Item URI retrieved: %s", jItemURI->toString()->std_str().c_str());

            setContext(jItemConn, jItemURI); //now set the correct context on the connection

            LOG_DEBUG("context set");

                jItemConn->addDesignation((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>) jNewItemMMM,
                                          (jnipp::Ref<JavaLangClass>) EuMicoPlatformAnno4jModelItemMMM::clazz());

            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            LOG_DEBUG("designation added");

            jnipp::LocalRef<jnipp::String> jDateTime =
                    jnipp::String::create(commons::TimeInfo::getTimestamp());

            assert((jobject) jDateTime);

            jNewItemMMM->setSerializedAt(jDateTime);
            checkJavaExcpetionNoThrow(m_jniErrorMessage);
            LOG_DEBUG("date time object create and set");

            auto newItem = std::make_shared<ItemAnno4cpp>(jNewItemMMM, *this);

            LOG_INFO("ItemMMM created and Item wrapper returned");

            return newItem;
        }      

        /**
        * Return the item with the given URI if it exists. The item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return A handle to the Item with the given URI, or null if it does not exist
        */

        std::shared_ptr<Item> PersistenceService::getItem(const  mico::rdf::model::URI& id) {

            LOG_DEBUG("PersistenceService::getItem for [%s] requested.",  id.stringValue().c_str());

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<OrgOpenrdfModelURI> itemURI =
                    OrgOpenrdfModelImplURIImpl::construct((jnipp::Ref<JavaLangString>) jnipp::String::create(id.stringValue()));

             checkJavaExcpetionNoThrow(m_jniErrorMessage);

            jnipp::GlobalRef<EuMicoPlatformAnno4jModelItemMMM> jItemMMM=
                    this->m_anno4j->findByID(EuMicoPlatformAnno4jModelItemMMM::clazz(), itemURI);

            bool isInstance = jItemMMM->isInstanceOf(EuMicoPlatformAnno4jModelItemMMM::clazz());
            bool except = checkJavaExcpetionNoThrow(m_jniErrorMessage);

            if (!isInstance || except) {
                LOG_DEBUG("returned RDF object is NOT an instance of ItemMMM");
                return  std::shared_ptr<Item>();
            } else {
                LOG_DEBUG("returned RDF object is instance of ItemMMM");
            }

            jnipp::LocalRef<OrgOpenrdfModelURI> jItemURIRet =
                ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)jItemMMM)->getResource();

            LOG_DEBUG("Got item with URI [%s]", jItemURIRet->toString()->std_str().c_str());

            return std::make_shared<ItemAnno4cpp>(jItemMMM, *this);
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
          return m_anno4j;
        }

        std::string PersistenceService::getStoragePrefix()
        {
          return m_storagePrefix;
        }

        std::string PersistenceService::getContentDirectory()
        {
          return contentDirectory;
        }

        void PersistenceService::setContext(jnipp::Ref<OrgOpenrdfRepositoryObjectObjectConnection> con, jnipp::Ref<OrgOpenrdfModelURI> context) {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            con->setReadContexts(context);
            con->setInsertContext(context);
            con->setRemoveContexts(context);
        }


    }
}
