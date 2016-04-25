#include "ItemAnno4cpp.hpp"

#include "TimeInfo.h"

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

        jnipp::GlobalRef<EuMicoPlatformAnno4jModelItemMMM> jNewPartMMM =
                m_persistenceService.getAnno4j()->createObject(EuMicoPlatformAnno4jModelPartMMM::clazz());

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jNewPartMMM);

        jnipp::LocalRef<OrgOpenrdfRepositoryObjectObjectConnection> jItemConn =
            ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)jNewPartMMM)->getObjectConnection();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jItemConn);

        LOG_DEBUG("Creating part using object connection with object identity hash %d", JavaLangSystem::identityHashCode(jItemConn));

        jnipp::LocalRef<OrgOpenrdfSailMemoryModelMemValueFactory> jMemValueFactory =
                OrgOpenrdfSailMemoryModelMemValueFactory::construct();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jMemValueFactory);

        jnipp::Ref<OrgOpenrdfModelResource> jResourceBlank =
                jMemValueFactory->createURI(jnipp::String::create("urn:anno4j:BLANK"));

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jResourceBlank);

        LOG_DEBUG("blank resource created");

        jnipp::LocalRef<OrgOpenrdfModelURI> jPartURI =
                ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)jNewPartMMM)->getResource();

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jPartURI);
        LOG_DEBUG("Part URI retrieved: %s", jPartURI->toString()->std_str().c_str());

        jItemConn->addDesignation((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>) jNewPartMMM,
                                  (jnipp::Ref<JavaLangClass>) EuMicoPlatformAnno4jModelPartMMM::clazz());

        checkJavaExcpetionThrow({"IllegalAccessException", "InstantiationException"});


        LOG_DEBUG("Created Part in context %s", jItemConn->getInsertContext()->toString()->std_str().c_str());

  //      jnipp::LocalRef<JavaLangString> jsuri = JavaLangString::create(this->getURI().stringValue());
  //      jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( jsuri );
  //      jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> partMMM = m_persistenceService.getAnno4j()->createObject(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
  //      jnipp::LocalRef<JavaLangString> dateTime = JavaLangString::create( commons::TimeInfo::getTimestamp() );
  //      static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(partMMM)->setSerializedAt( dateTime );

  //      jnipp::LocalRef<ComGithubAnno4jModelAgent> agent = m_persistenceService.getAnno4j()->createObject(ComGithubAnno4jModelAgent::clazz(), juri);
  //      jnipp::LocalRef<JavaLangString> jsextractorID = JavaLangString::create(extractorID.stringValue());
  //      jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> jextractorID = OrgOpenrdfModelImplURIImpl::construct( jsextractorID );
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
        jnipp::LocalRef<JavaLangString> juri = JavaLangString::create( uri.stringValue() );

        jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> partMMM = m_persistenceService.getAnno4j()->findByID(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
        std::shared_ptr<PartAnno4cpp> part( new PartAnno4cpp(partMMM, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService) );
        return part;
      }

  //    mico::rdf::model::URI ItemAnno4cpp::getURI() {
  //      jnipp::Env::Scope scope(PersistenceService::m_sJvm);

  //        jnipp::LocalRef<OrgOpenrdfModelURI> jItemURIRet =
  //            ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)m_itemMMM)->getResource();

  //        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
  //        assert((jobject) jItemURIRet);


  //        return mico::rdf::model::URI( jItemURIRet->toString()->std_str() );
  //    }

      std::list< std::shared_ptr<Part> > ItemAnno4cpp::getParts()
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::list< std::shared_ptr<Part> > partSet;
        jnipp::LocalRef<JavaLangString> jsuri = JavaLangString::create(this->getURI().stringValue());
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( jsuri );
        jnipp::LocalRef<JavaUtilList> jpartList = m_persistenceService.getAnno4j()->findAll(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
        for (jsize i = 0; i < jpartList->size(); i++) {
          jnipp::LocalRef<JavaLangObject> jobject = jpartList->get(i);
          std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(jobject, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService));
          partSet.push_back( part );
        }
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        return partSet;
      }

      std::shared_ptr<Asset> ItemAnno4cpp::getAsset()
      {
  //      jnipp::Env::Scope scope(PersistenceService::m_sJvm);
  //      jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset();
  //      if ((jobject)asset == nullptr) {
  //        jnipp::LocalRef<ComGithubAnno4jAnno4j> anno4j = m_persistenceService.getAnno4j();
  //        jnipp::LocalRef<JavaLangString> jsuri_item = JavaLangString::create(this->getURI().stringValue());
  //        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri_item = OrgOpenrdfModelImplURIImpl::construct( jsuri_item );
  //        jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> assetMMM = anno4j->createObject(EuMicoPlatformAnno4jModelAssetMMM::clazz(), juri_item);

  //        std::string location = m_persistenceService.getStoragePrefix();
  //        location += this->getURI().getLocalName();
  //        location += "/";
  //        jnipp::LocalRef<JavaLangString> jsasset = static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(assetMMM)->getResourceAsString();
  //        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri_asset = OrgOpenrdfModelImplURIImpl::construct( jsasset );
  //        jnipp::LocalRef<JavaLangString> jsuri_asset = juri_asset->getLocalName();
  //        location += jsuri_asset->std_str();

  //        assetMMM->setLocation( JavaLangString::create(location) );

  //        static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->setAsset(assetMMM);

  //        //log.trace("No Asset available for Item {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
  //        checkJavaExcpetionThrow({"IllegalAccessException", "InstantiationException"});
  //      }

  //      std::shared_ptr<AssetAnno4cpp> passet( new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset(), m_persistenceService) );
  //      return passet;
  //    }
        throw std::runtime_error("Not implementend yet");
        return std::shared_ptr<Asset>();
  //  }
      }
    }
  }
}
