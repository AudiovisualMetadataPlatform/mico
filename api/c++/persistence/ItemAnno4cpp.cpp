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

namespace mico {
  namespace persistence {
    namespace model {

      std::shared_ptr<Part> ItemAnno4cpp::createPart(const rdf::model::URI &extractorID)
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);

  //    PartMMM partMMM = createObject(PartMMM.class);
  //    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
  //    partMMM.setSerializedAt(dateTime);

  //    Agent agent = createObject(extractorID, Agent.class);
  //    partMMM.setSerializedBy(agent);

  //    this.itemMMM.addPart(partMMM);

  //    log.trace("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);

  //    return new PartAnno4j(partMMM, this, persistenceService);

        jnipp::GlobalRef<ItemMMM> jNewPartMMM =
                m_persistenceService.getAnno4j()->createObject(PartMMM::clazz());

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jNewPartMMM);

        jnipp::LocalRef<ObjectConnection> jItemConn =
            ((jnipp::Ref<RDFObject>)jNewPartMMM)->getObjectConnection();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jItemConn);

        LOG_DEBUG("Creating part using object connection with object identity hash %d", System::identityHashCode(jItemConn));

        jnipp::LocalRef<MemValueFactory> jMemValueFactory =
                MemValueFactory::construct();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jMemValueFactory);

        jnipp::Ref<jnipp::org::openrdf::model::Resource> jResourceBlank =
                jMemValueFactory->createURI(jnipp::String::create("urn:anno4j:BLANK"));

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jResourceBlank);

        LOG_DEBUG("blank resource created");

        jnipp::LocalRef<URI> jPartURI =
                ((jnipp::Ref<RDFObject>)jNewPartMMM)->getResource();

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jPartURI);
        LOG_DEBUG("Part URI retrieved: %s", jPartURI->toString()->std_str().c_str());

        jItemConn->addDesignation((jnipp::Ref<RDFObject>) jNewPartMMM,
                                  (jnipp::Ref<Class>) PartMMM::clazz());

        checkJavaExcpetionThrow({"IllegalAccessException", "InstantiationException"});


        LOG_DEBUG("Created Part in context %s", jItemConn->getInsertContext()->toString()->std_str().c_str());

  //      jnipp::LocalRef<String> jsuri = String::create(this->getURI().stringValue());
  //      jnipp::LocalRef<ImplURIImpl> juri = ImplURIImpl::construct( jsuri );
  //      jnipp::LocalRef<PartMMM> partMMM = m_persistenceService.getAnno4j()->createObject(PartMMM::clazz(), juri);
  //      jnipp::LocalRef<String> dateTime = String::create( commons::TimeInfo::getTimestamp() );
  //      static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(partMMM)->setSerializedAt( dateTime );

  //      jnipp::LocalRef<ComGithubAnno4jModelAgent> agent = m_persistenceService.getAnno4j()->createObject(ComGithubAnno4jModelAgent::clazz(), juri);
  //      jnipp::LocalRef<String> jsextractorID = String::create(extractorID.stringValue());
  //      jnipp::LocalRef<ImplURIImpl> jextractorID = ImplURIImpl::construct( jsextractorID );
  //      static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(agent)->setResource( jextractorID );
  //      static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(partMMM)->setSerializedBy(agent);

  //      m_itemMMM->addPart(partMMM);

        //log.trace("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);



        //std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(partMMM, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService));
        return nullptr;
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
