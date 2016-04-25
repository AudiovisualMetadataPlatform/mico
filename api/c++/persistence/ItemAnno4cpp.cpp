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

  //    mico::rdf::model::URI ItemAnno4cpp::getURI() {
  //      jnipp::Env::Scope scope(PersistenceService::m_sJvm);

  //        jnipp::LocalRef<URI> jItemURIRet =
  //            ((jnipp::Ref<RDFObject>)m_itemMMM)->getResource();

  //        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
  //        assert((jobject) jItemURIRet);


  //        return mico::rdf::model::URI( jItemURIRet->toString()->std_str() );
  //    }

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

      std::shared_ptr<Asset> ItemAnno4cpp::getAsset()
      {
  //      jnipp::Env::Scope scope(PersistenceService::m_sJvm);
  //      jnipp::LocalRef<AssetMMM> asset = static_cast< jnipp::LocalRef<ResourceMMM> >(m_itemMMM)->getAsset();
  //      if ((jobject)asset == nullptr) {
  //        jnipp::LocalRef<ComGithubAnno4jAnno4j> anno4j = m_persistenceService.getAnno4j();
  //        jnipp::LocalRef<String> jsuri_item = String::create(this->getURI().stringValue());
  //        jnipp::LocalRef<ImplURIImpl> juri_item = ImplURIImpl::construct( jsuri_item );
  //        jnipp::LocalRef<AssetMMM> assetMMM = anno4j->createObject(AssetMMM::clazz(), juri_item);

  //        std::string location = m_persistenceService.getStoragePrefix();
  //        location += this->getURI().getLocalName();
  //        location += "/";
  //        jnipp::LocalRef<String> jsasset = static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(assetMMM)->getResourceAsString();
  //        jnipp::LocalRef<ImplURIImpl> juri_asset = ImplURIImpl::construct( jsasset );
  //        jnipp::LocalRef<String> jsuri_asset = juri_asset->getLocalName();
  //        location += jsuri_asset->std_str();

  //        assetMMM->setLocation( String::create(location) );

  //        static_cast< jnipp::LocalRef<ResourceMMM> >(m_itemMMM)->setAsset(assetMMM);

  //        //log.trace("No Asset available for Item {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
  //        checkJavaExcpetionThrow({"IllegalAccessException", "InstantiationException"});
  //      }

  //      std::shared_ptr<AssetAnno4cpp> passet( new AssetAnno4cpp(static_cast< jnipp::LocalRef<ResourceMMM> >(m_itemMMM)->getAsset(), m_persistenceService) );
  //      return passet;
  //    }
        throw std::runtime_error("Not implementend yet");
        return std::shared_ptr<Asset>();
  //  }
      }
    }
  }
}
