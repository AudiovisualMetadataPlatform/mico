#include "ItemAnno4cpp.hpp"

#include "TimeInfo.h"

namespace mico {
  namespace persistence {

    std::shared_ptr<Part> ItemAnno4cpp::createPart(mico::rdf::model::URI extractorID)
    {
      try {
        jnipp::LocalRef<JavaLangString> jsuri = JavaLangString::create(this->getURI().stringValue());
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( jsuri );
        jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> partMMM = m_persistenceService.getAnno4j()->createObject(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
        jnipp::LocalRef<JavaLangString> dateTime = JavaLangString::create( commons::TimeInfo::getTimestamp() );
        static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(partMMM)->setSerializedAt( dateTime );

        jnipp::LocalRef<ComGithubAnno4jModelAgent> agent = m_persistenceService.getAnno4j()->createObject(ComGithubAnno4jModelAgent::clazz(), juri);
        jnipp::LocalRef<JavaLangString> jsextractorID = JavaLangString::create(extractorID.stringValue());
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> jextractorID = OrgOpenrdfModelImplURIImpl::construct( jsextractorID );
        static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(agent)->setResource( jextractorID );
        static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(partMMM)->setSerializedBy(agent);

        m_itemMMM->addPart(partMMM);

        //log.trace("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);

        std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(partMMM, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService));
        return part;
      } catch (...) {
        throw std::runtime_error("ItemAnno4cpp::createPart(): something went wrong )-;");
      }
    }

    std::list< std::shared_ptr<Part> > ItemAnno4cpp::getParts()
    {
      std::list< std::shared_ptr<Part> > partSet;
      jnipp::LocalRef<JavaLangString> jsuri = JavaLangString::create(this->getURI().stringValue());
      jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( jsuri );
      jnipp::LocalRef<JavaUtilList> jpartList = m_persistenceService.getAnno4j()->findAll(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
      for (jsize i = 0; i < jpartList->size(); i++) {
        jnipp::LocalRef<JavaLangObject> jobject = jpartList->get(i);
        std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(jobject, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService));
        partSet.push_back( part );
      }
      return partSet;
    }

    std::shared_ptr<Asset> ItemAnno4cpp::getAsset()
    {
      jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset();
      if ((jobject)asset == nullptr) {
        try {
          jnipp::LocalRef<ComGithubAnno4jAnno4j> anno4j = m_persistenceService.getAnno4j();
          jnipp::LocalRef<JavaLangString> jsuri_item = JavaLangString::create(this->getURI().stringValue());
          jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri_item = OrgOpenrdfModelImplURIImpl::construct( jsuri_item );
          jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> assetMMM = anno4j->createObject(EuMicoPlatformAnno4jModelAssetMMM::clazz(), juri_item);

          std::string location = m_persistenceService.getStoragePrefix();
          location += this->getURI().getLocalName();
          location += "/";
          jnipp::LocalRef<JavaLangString> jsasset = static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(assetMMM)->getResourceAsString();
          jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri_asset = OrgOpenrdfModelImplURIImpl::construct( jsasset );
          jnipp::LocalRef<JavaLangString> jsuri_asset = juri_asset->getLocalName();
          location += jsuri_asset->std_str();

          assetMMM->setLocation( JavaLangString::create(location) );

          static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->setAsset(assetMMM);

          //log.trace("No Asset available for Item {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
        } catch (...) {
            throw std::runtime_error("creation of new Asset failed");
        }
      }

      std::shared_ptr<AssetAnno4cpp> passet( new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset(), m_persistenceService) );
      return passet;
    }
  }
}
