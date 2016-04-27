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

        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jTransaction);

        jTransaction->begin();
        jTransaction->setAllContexts(((jnipp::Ref<RDFObject>)m_itemMMM)->getResource());
        jnipp::GlobalRef <PartMMM> jNewPartMMM = jTransaction->createObject(PartMMM::clazz());

        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jNewPartMMM);

        jnipp::LocalRef<jnipp::String> jDateTime =
                jnipp::String::create(commons::TimeInfo::getTimestamp());

        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jDateTime);

        ((jnipp::Ref<Annotation>)jNewPartMMM)->setSerializedAt(jDateTime);
        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);

        jnipp::LocalRef<URI> jExtractorURI = URIImpl::construct(jnipp::String::create(extractorID.stringValue()));
        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jExtractorURI);

        jnipp::LocalRef<Agent> jAgent = jTransaction->createObject(Agent::clazz(), (jnipp::Ref<jnipp::org::openrdf::model::Resource>) jExtractorURI);


        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jAgent);

        ((jnipp::Ref<Annotation>)jNewPartMMM)->setSerializedBy(jAgent);

        error = error & checkJavaExcpetionNoThrow(m_jnippErrorMessage);

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


        checkJavaExcpetionNoThrow(m_jnippErrorMessage);

        std::shared_ptr<Item> this_ptr= std::dynamic_pointer_cast<Item>(shared_from_this());

        auto newPart = std::make_shared<model::PartAnno4cpp>(jNewPartMMM, this_ptr, m_persistenceService);

        LOG_INFO("PartMMM created and Part wrapper returned");

        return newPart;
      }

      std::shared_ptr<Part> ItemAnno4cpp::getPart(const rdf::model::URI &uri)
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<String> juri = String::create( uri.stringValue() );

        jnipp::LocalRef<PartMMM> partMMM = m_persistenceService.getAnno4j()->findByID(PartMMM::clazz(), juri);
        std::shared_ptr<PartAnno4cpp> part( new PartAnno4cpp(partMMM, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService) );
        return part;
      }

      std::list< std::shared_ptr<Part> > ItemAnno4cpp::getParts()
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::list< std::shared_ptr<Part> > partSet;
        jnipp::LocalRef<String> jsuri = String::create(this->getURI().stringValue());
        jnipp::LocalRef<URIImpl> juri = URIImpl::construct( jsuri );
        jnipp::LocalRef<List> jpartList = m_persistenceService.getAnno4j()->findAll(PartMMM::clazz(), juri);
        for (jsize i = 0; i < jpartList->size(); i++) {
          jnipp::LocalRef<Object> jobject = jpartList->get(i);
          std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(jobject, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService));
          partSet.push_back( part );
        }
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        return partSet;
      }
    }
  }
}
