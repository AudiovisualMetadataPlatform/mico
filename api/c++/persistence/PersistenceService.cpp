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
#include "JnippExceptionHandling.hpp"

#include <anno4cpp.h>

#include "ItemAnno4cpp.hpp"


#ifndef ANNO4JDEPENDENCIES_PATH
    #define ANNO4JDEPENDENCIES_PATH ""
#endif

using namespace std;
using namespace boost;
using namespace uuids;

using namespace jnipp::java::lang;
using namespace jnipp::org::openrdf::idGenerator;
using namespace jnipp::org::openrdf::repository::sparql;
using namespace jnipp::org::openrdf::model;
using namespace jnipp::org::openrdf::model::impl;
using namespace jnipp::org::openrdf::repository::object;
using namespace jnipp::org::openrdf::sail::memory::model;
using namespace jnipp::com::github::anno4j;
using namespace jnipp::eu::mico::platform::anno4j::model;
using namespace jnipp::eu::mico::platform::persistence::impl;





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

            checkJavaExceptionNoThrow(m_jniErrorMessage);

            jnipp::LocalRef<String> jURIString = jnipp::String::create(marmottaServerUrl);

            checkJavaExceptionNoThrow(m_jniErrorMessage);
            assert((jobject)jURIString != 0);

            LOG_INFO("Using Marmotta URI: %s", jURIString->std_str().c_str());

            jnipp::LocalRef<IDGenerator> gen =
                IDGeneratorAnno4j::construct(jURIString);

            checkJavaExceptionNoThrow(m_jniErrorMessage);
            assert((jobject)gen != 0);

            LOG_DEBUG("IDGeneratorAnno4j  created");

            jnipp::LocalRef<SPARQLRepository> sparqlRepository =
                   SPARQLRepository::construct(
                  jnipp::String::create(marmottaServerUrl + std::string("/sparql/select")),
                  jnipp::String::create(marmottaServerUrl + std::string("/sparql/update")));

            LOG_DEBUG("SPARQLRepository  created");

            checkJavaExceptionNoThrow(m_jniErrorMessage);

            m_anno4j = Anno4j::construct(sparqlRepository, gen);

            checkJavaExceptionNoThrow(m_jniErrorMessage);

            LOG_DEBUG("anno4j object created.");
        }


        /**
        * Create a new item with a random URI and return it. The item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return a handle to the newly created Item
        */
        std::shared_ptr<model::Item> PersistenceService::createItem() {
          bool creation_error = false;

          jnipp::Env::Scope scope(PersistenceService::m_sJvm);

          LOG_DEBUG("PersistenceService::createItem()");
          assert((jobject) m_anno4j);

          jnipp::LocalRef<Resource> jItemResource =
              m_anno4j->getIdGenerator()->generateID(jnipp::java::util::HashSet::construct());

          jnipp::LocalRef<Transaction> jTransaction = m_anno4j->createTransaction();

          creation_error = creation_error & checkJavaExceptionNoThrow(m_jniErrorMessage);
          assert((jobject) jTransaction);

          jTransaction->begin();

          jTransaction->setAllContexts((jnipp::Ref<URI>) jItemResource);

          jnipp::GlobalRef<ItemMMM> jNewItemMMM =
                  jTransaction->createObject(ItemMMM::clazz(),jItemResource);

          creation_error = creation_error & checkJavaExceptionNoThrow(m_jniErrorMessage);
          assert((jobject) jNewItemMMM);

          LOG_DEBUG("ItemMMM created with anno4j");


          jnipp::LocalRef<jnipp::String> jDateTime =
                  jnipp::String::create(commons::TimeInfo::getTimestamp());

          assert((jobject) jDateTime);

          jNewItemMMM->setSerializedAt(jDateTime);
          creation_error = creation_error & checkJavaExceptionNoThrow(m_jniErrorMessage);
          LOG_DEBUG("date time object create and set");

          if (!creation_error) {
            jTransaction->commit();
            LOG_DEBUG("Transaction for ItemMMM commited");
          } else {
            if ((jobject) jTransaction) {
              jTransaction->rollback();
              jTransaction->close();
            }
          }

          auto newItem = std::make_shared<model::ItemAnno4cpp>(jNewItemMMM, *this);

          LOG_INFO("ItemMMM created and Item wrapper returned");

          return newItem;
        }      

        /**
        * Return the item with the given URI if it exists. The item should be suitable for reading and
        * updating and write all updates to the underlying low-level persistence layer.
        *
        * @return A handle to the Item with the given URI, or null if it does not exist
        */

        std::shared_ptr<model::Item> PersistenceService::getItem(const  mico::rdf::model::URI& id) {

            LOG_DEBUG("PersistenceService::getItem for [%s] requested.",  id.stringValue().c_str());

            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<URI> jItemURI =
                    URIImpl::construct((jnipp::Ref<String>) jnipp::String::create(id.stringValue()));

            checkJavaExceptionNoThrow(m_jniErrorMessage);
            assert((jobject) jItemURI);

            jnipp::LocalRef<Transaction> jTransaction = m_anno4j->createTransaction();

            checkJavaExceptionNoThrow(m_jniErrorMessage);
            assert((jobject) jTransaction);

            jTransaction->setAllContexts((jnipp::Ref<URI>) jItemURI);

            jnipp::GlobalRef<ItemMMM> jItemMMM=
                    jTransaction->findByID(ItemMMM::clazz(), jItemURI);

            bool isInstance = jItemMMM->isInstanceOf(ItemMMM::clazz());
            bool except = checkJavaExceptionNoThrow(m_jniErrorMessage);

            if (!isInstance || except) {
                LOG_WARN("PersistenceService::getItem - returned RDF object is NOT an instance of ItemMMM or null");
                return  std::shared_ptr<model::Item>();
            }

            jnipp::LocalRef<URI> jItemURIRet =
                ((jnipp::Ref<RDFObject>)jItemMMM)->getResource();

            LOG_DEBUG("Got item with URI [%s]", jItemURIRet->toString()->std_str().c_str());

            return std::make_shared<model::ItemAnno4cpp>(jItemMMM, *this);
        }

        /**
        * Delete the content item with the given URI. If the content item does not exist, do nothing.
        */
        void PersistenceService::deleteItem(const mico::rdf::model::URI& id) {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);

          jnipp::LocalRef<URI> jItemURI =
                  URIImpl::construct((jnipp::Ref<String>) jnipp::String::create(id.stringValue()));

          checkJavaExceptionNoThrow(m_jniErrorMessage);
          assert((jobject) jItemURI);

          m_anno4j->clearContext(jItemURI);
          LOG_DEBUG("Deleted item with id %s including all triples in the corresponding context graph", id.stringValue().c_str());
        }

        std::vector< std::shared_ptr<model::Item> > PersistenceService::getItems()
        {
          std::vector< std::shared_ptr<model::Item> > resultVec;

          jnipp::Env::Scope scope(PersistenceService::m_sJvm);

          jnipp::LocalRef<jnipp::java::util::List> jItemsMMM = m_anno4j->findAll(ItemMMM::clazz());

          checkJavaExceptionNoThrow(m_jniErrorMessage);
          assert((jobject) jItemsMMM);

          jint jNumItems = jItemsMMM->size();

          checkJavaExceptionNoThrow(m_jniErrorMessage);

          LOG_DEBUG("Found %d items.", jNumItems);

          resultVec.reserve(jNumItems);

          for (jint idx_item; idx_item < jNumItems; ++idx_item) {
              jnipp::GlobalRef<ItemMMM> jItemMMM = jItemsMMM->get(idx_item);
              resultVec.push_back(std::make_shared<model::ItemAnno4cpp>(jItemMMM, *this));
          }

          return resultVec;
        }


//        /**
//        * Return an iterator over all currently available content items.
//        *
//        * @return iterable
//        */
//        item_iterator PersistenceService::begin() {
//            map<string,string> params;
//            params["g"] = marmottaServerUrl;

//            const TupleResult* r = metadata.query(SPARQL_FORMAT(listContentItems,params));
//            if(r->size() > 0) {
//                return item_iterator(marmottaServerUrl,contentDirectory,r);
//            } else {
//                delete r;
//                return item_iterator(marmottaServerUrl,contentDirectory);
//            }
//        }


//        item_iterator PersistenceService::end() {
//            return item_iterator(marmottaServerUrl,contentDirectory);
//        }

//        void item_iterator::increment() {
//            pos = pos+1 == result->size() ? -1 : pos + 1;
//        };

//        bool item_iterator::equal(item_iterator const& other) const {
//            return this->pos == other.pos;
//        };

//        Item *item_iterator::dereference() const {
//            return new ContentItem(baseUrl, contentDirectory, *dynamic_cast<const URI*>( result->at(pos).at("p") ) );
//        }

        jnipp::LocalRef<Anno4j> PersistenceService::getAnno4j()
        {
          jnipp::Env::Scope scope(m_sJvm);
          return m_anno4j;
        }

        std::string PersistenceService::getContentDirectory()
        {
          return contentDirectory;
        }

        void PersistenceService::checkJavaExceptionThrow()
        {
          jnipp::Env::Scope scope(m_sJvm);
          jnipputil::checkJavaExceptionThrow();
        }

        void PersistenceService::checkJavaExceptionThrow(std::vector<std::string> exceptionNames)
        {
          jnipp::Env::Scope scope(m_sJvm);
          jnipputil::checkJavaExceptionThrow(exceptionNames);
        }

        bool PersistenceService::checkJavaExceptionNoThrow(std::string& msg)
        {
          jnipp::Env::Scope scope(m_sJvm);
          return jnipputil::checkJavaExceptionNoThrow(msg);
        }

        bool PersistenceService::checkJavaExceptionNoThrow(std::vector<std::string> exceptionNames, std::string& msg)
        {
          jnipp::Env::Scope scope(m_sJvm);
          return jnipputil::checkJavaExceptionNoThrow(exceptionNames, msg);
        }


        void PersistenceService::setContext(jnipp::Ref<ObjectConnection> con, jnipp::Ref<URI> context) {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            LOG_DEBUG("Setting context for object connection with object identity hash %d", System::identityHashCode(con));

            checkJavaExceptionNoThrow(m_jniErrorMessage);

            con->setReadContexts(context);
            con->setInsertContext(context);
            con->setRemoveContexts(context);
        }


    }
}
