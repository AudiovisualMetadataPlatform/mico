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
    /*  if (this.partMMM.getAsset() == null) {
        try {
              Anno4j anno4j = m_persistenceService.getAnno4j();
              EuMicoPlatformAnno4jModelAssetMMM assetMMM = anno4j.createObject(AssetMMM.class, this.item.getURI());

              StringBuilder location = new StringBuilder()
                      .append(persistenceService.getStoragePrefix())
                      .append(this.item.getURI().getLocalName())
                      .append("/")
                      .append(this.getURI().getLocalName())
                      .append("/")
                      .append(new URIImpl(assetMMM.getResourceAsString()).getLocalName());

              assetMMM.setLocation(location.toString());

              this.partMMM.setAsset(assetMMM);

              log.trace("No Asset available for Part {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
          } catch (IllegalAccessException e) {
              throw new RepositoryException("Illegal access", e);
          } catch (InstantiationException e) {
              throw new RepositoryException("Couldn´t instantiate AssetMMM", e);
          }
      }
      return new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getAsset(), m_persistenceService);
    */
      throw std::runtime_error("PartAnno4cpp::getAsset(): Not yet implemented!");
    }
  }
}
