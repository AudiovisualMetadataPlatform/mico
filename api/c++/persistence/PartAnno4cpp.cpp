#include "PartAnno4cpp.hpp"

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

    //std::list<Resource> PartAnno4cpp::getInputs()
    //{
    //  std::list<Resource> resourceSet;
      //for(EuMicoPlatformAnno4jModelResourceMMM resourceMMM : m_partMMM.getInputs()) {
      //  if(resourceMMM instanceof EuMicoPlatformAnno4jModelItemMMM) {
      //    resourceSet.push_back(new ItemAnno4j((EuMicoPlatformAnno4jModelItemMMM) resourceMMM, m_persistenceService));
      //    } else {
      //      resourceSet.add(new PartAnno4j((PartMMM) resourceMMM, m_item, m_persistenceService));
      //  }
      //}
    //  return resourceSet;
    //}

    void PartAnno4cpp::setInputs(std::list<Resource> inputs)
    {
      std::list< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> > resourceMMMSet;
      //jnipp::LocalRef< JavaUtilSet > jresourceMMMSet;
      //for(Resource resource : inputs) {
      //    resourceMMMSet.push_back(resource.getRDFObject());
      //}
      //m_partMMM->setInputs(jresourceMMMSet);
      throw std::runtime_error("PartAnno4cpp::setInputs(): Not yet implemented!");
    }

    Asset* PartAnno4cpp::getAsset()
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
    */
      return new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getAsset(), m_persistenceService);
    }
  }
}
