#include "ItemAnno4cpp.hpp"

#include "TimeInfo.h"

using namespace jnipp::java::lang;
using namespace jnipp::java::util;
using namespace jnipp::org::openrdf::idGenerator;
using namespace jnipp::org::openrdf::repository::sparql;
using namespace jnipp::org::openrdf::model;
using namespace jnipp::org::openrdf::model::impl;
using namespace jnipp::org::openrdf::repository::object;
using namespace jnipp::org::openrdf::sail::memory::model;
using namespace jnipp::com::github::anno4j;
using namespace jnipp::eu::mico::platform::anno4j::model;
using namespace jnipp::eu::mico::platform::persistence::impl;
using namespace jnipp::com::github::anno4j::model;
using namespace jnipp::com::github::anno4j::model::impl;

namespace mico {
  namespace persistence {
    namespace model {

      std::shared_ptr<Part> ItemAnno4cpp::createPart(const rdf::model::URI &extractorID)
      {
        bool error = false;
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);

        jnipp::LocalRef<Transaction> jTransaction = m_persistenceService.getAnno4j()->createTransaction();

        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jTransaction);

        jTransaction->begin();
        jTransaction->setAllContexts(((jnipp::Ref<RDFObject>)m_itemMMM)->getResource());
        jnipp::GlobalRef <PartMMM> jNewPartMMM = jTransaction->createObject(PartMMM::clazz());

        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jNewPartMMM);

        jnipp::LocalRef<jnipp::String> jDateTime =
                jnipp::String::create(commons::TimeInfo::getTimestamp());

        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jDateTime);

        ((jnipp::Ref<Annotation>)jNewPartMMM)->setSerializedAt(jDateTime);
        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

        jnipp::LocalRef<URI> jExtractorURI = URIImpl::construct(jnipp::String::create(extractorID.stringValue()));
        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jExtractorURI);

        jnipp::LocalRef<Agent> jAgent = jTransaction->createObject(Agent::clazz(), (jnipp::Ref<jnipp::org::openrdf::model::Resource>) jExtractorURI);


        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jAgent);

        ((jnipp::Ref<Annotation>)jNewPartMMM)->setSerializedBy(jAgent);

        error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

        if (error) {
          jTransaction->rollback(); //rollback any triples created during this method
          jTransaction->close(); //in case we have not succeeded we can close the connection
        } else {
          jTransaction->commit(); //commit the item before returning
        }

        this->m_itemMMM->addPart(jNewPartMMM);

        LOG_DEBUG("Created Part with id %s in the context graph %s - Creator %s",
                  ((jnipp::Ref<ResourceObject>) jNewPartMMM)->getResourceAsString()->std_str().c_str(),
                  this->getURI().stringValue().c_str(), extractorID.stringValue().c_str());


        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

        std::shared_ptr<Item> this_ptr= std::dynamic_pointer_cast<Item>(shared_from_this());

        auto newPart = std::make_shared<model::PartAnno4cpp>(jNewPartMMM, this_ptr, m_persistenceService);

        LOG_INFO("PartMMM created and Part wrapper returned");

        return newPart;
      }

      std::shared_ptr<Part> ItemAnno4cpp::getPart(const rdf::model::URI &uri)
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<Transaction> jTransaction = m_persistenceService.getAnno4j()->createTransaction();
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jTransaction);

        jnipp::LocalRef<PartMMM> jPartMMM =
            jTransaction->findByID(PartMMM::clazz(), (jnipp::Ref<String>) jnipp::String::create(uri.stringValue()));

        bool isInstance = jPartMMM->isInstanceOf(PartMMM::clazz());
        bool except = m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

        if (!isInstance || except) {
            LOG_DEBUG("ItemAnno4cpp::getPart - Returned RDF object is NOT an instance of PartMMM or null");
            return  std::shared_ptr<model::Part>();
        }

        jnipp::LocalRef<URI> jPartURIRet =
            ((jnipp::Ref<RDFObject>)jPartMMM)->getResource();

        LOG_DEBUG("Got part with URI [%s]", jPartURIRet->toString()->std_str().c_str());

        return std::make_shared<PartAnno4cpp> (jPartMMM, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService);
      }

      std::list< std::shared_ptr<mico::persistence::model::Part> > ItemAnno4cpp::getParts()
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::list< std::shared_ptr<Part> > nativePartSet;

        jnipp::LocalRef<Set> jpartSet = m_itemMMM->getParts();

        jnipp::LocalRef< jnipp::Array<JavaLangObject> > jpartArray = jpartSet->toArray();
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jpartArray);

        LOG_DEBUG("Retrieved %d part(s) in array for item %s", jpartArray->length(), this->getURI().stringValue().c_str());

        for (auto it = jpartArray->begin();  it!= jpartArray->end(); ++it) {
          jnipp::LocalRef<Object> jObject = *it;
          m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
          assert((jobject) jObject);
          std::shared_ptr<Part> part =
              std::make_shared<PartAnno4cpp> (jObject, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService);

          nativePartSet.push_back( part );
        }
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        return nativePartSet;
      }
    }
  }
}
