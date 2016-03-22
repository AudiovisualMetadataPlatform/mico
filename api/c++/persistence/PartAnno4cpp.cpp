#include "PartAnno4cpp.hpp"
#include "ItemAnno4cpp.hpp"

namespace mico {
  namespace persistence {

    std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > PartAnno4cpp::getTargets() {
      std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > list;

      jnipp::LocalRef<JavaUtilSet> jset = m_partMMM->getTarget();
      jnipp::LocalRef< jnipp::Array<JavaLangObject> > jarray = static_cast< jnipp::LocalRef<JavaUtilHashSet> >(jset)->toArray();
      for (jsize i = 0; i < jarray->length(); i++) {
        jnipp::LocalRef<JavaLangObject> jobject = jarray->get(i);
        list.push_back( jobject );
      }
      return list;
    }

    void PartAnno4cpp::setTargets(std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > targets)
    {
      jnipp::LocalRef< JavaUtilHashSet > jtargetSet = JavaUtilHashSet::construct();
      for(auto iter = targets.begin(); iter != targets.end(); iter++) {
        jtargetSet->add(*iter);
      }
      m_partMMM->setTarget(jtargetSet);
    }

    std::list< std::shared_ptr<Resource> > PartAnno4cpp::getInputs()
    {
      std::list< std::shared_ptr<Resource> > resourceSet;

      jnipp::LocalRef<JavaUtilSet> jset = m_partMMM->getInputs();
      jnipp::LocalRef< jnipp::Array<JavaLangObject> > jarray = static_cast< jnipp::LocalRef<JavaUtilHashSet> >(jset)->toArray();
      for (jsize i = 0; i < jarray->length(); i++) {
        jnipp::LocalRef<JavaLangObject> jobject = jarray->get(i);
        if ( jobject->isInstanceOf(EuMicoPlatformAnno4jModelItemMMM::clazz()) ) {
          std::shared_ptr<ItemAnno4cpp> item(new ItemAnno4cpp(jobject, m_persistenceService));
          resourceSet.push_back( item );
        } else {
          std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(jobject, m_item, m_persistenceService));
          resourceSet.push_back( part );
        }
      }
      return resourceSet;
    }

    void PartAnno4cpp::setInputs(std::list< std::shared_ptr<Resource> > inputs)
    {
      jnipp::LocalRef< JavaUtilHashSet > jresourceMMMSet = JavaUtilHashSet::construct();
      for(auto iter = inputs.begin(); iter != inputs.end(); iter++) {
          jresourceMMMSet->add( (*iter)->getRDFObject() );
      }
      m_partMMM->setInputs(jresourceMMMSet);
    }

    std::shared_ptr<Asset> PartAnno4cpp::getAsset()
    {
      jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getAsset();
      if ((jobject)asset == nullptr) {
        try {
          jnipp::LocalRef<ComGithubAnno4jAnno4j> anno4j = m_persistenceService.getAnno4j();
          jnipp::LocalRef<JavaLangString> jsuri_item = JavaLangString::create(m_item->getURI().stringValue());
          jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri_item = OrgOpenrdfModelImplURIImpl::construct( jsuri_item );
          jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> assetMMM = anno4j->createObject(EuMicoPlatformAnno4jModelAssetMMM::clazz(), juri_item);

          std::string location = m_persistenceService.getStoragePrefix();
          location += m_item->getURI().getLocalName();
          location += "/";
          location += this->getURI().getLocalName();
          location += "/";
          jnipp::LocalRef<JavaLangString> jsasset = static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(assetMMM)->getResourceAsString();
          jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri_asset = OrgOpenrdfModelImplURIImpl::construct( jsasset );
          jnipp::LocalRef<JavaLangString> jsuri_asset = juri_asset->getLocalName();
          location += jsuri_asset->std_str();

          assetMMM->setLocation( JavaLangString::create(location) );

          static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->setAsset(assetMMM);

          //log.trace("No Asset available for Part {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
        } catch (...) {
            throw std::runtime_error("creation of new Asset failed");
        }
      }

      std::shared_ptr<AssetAnno4cpp> passet( new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getAsset(), m_persistenceService) );
      return passet;
    }
  }
}
