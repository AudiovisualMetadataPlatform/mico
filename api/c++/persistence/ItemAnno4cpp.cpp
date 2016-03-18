#include "ItemAnno4cpp.hpp"

namespace mico {
  namespace persistence {

    std::shared_ptr<Part> ItemAnno4cpp::createPart(mico::rdf::model::URI extractorID)
    {
      try {
        jnipp::LocalRef<JavaLangString> jsuri = JavaLangString::create(this->getURI().stringValue());
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( jsuri );
        jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> partMMM = m_persistenceService.getAnno4j()->createObject(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
        jnipp::LocalRef<JavaLangString> dateTime = JavaLangString::create( getTimestamp() );
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

    //Iterable<? extends Part> ItemAnno4cpp::getParts() {
    //  ArrayList<PartAnno4j> partsAnno4j = new ArrayList<>();
    //  List<PartMMM> partsMMM = persistenceService.getAnno4j().findAll(PartMMM.class, this.getURI());
    //  for (PartMMM partMMM : partsMMM) {
    //    partsAnno4j.add(new PartAnno4j(partMMM, this, persistenceService));
    //  }
    //  return partsAnno4j;
    //}

    std::shared_ptr<Asset> ItemAnno4cpp::getAsset() {
      //if (m_itemMMM.getAsset() == null) {
      //  try {
      //    Anno4j anno4j = m_persistenceService.getAnno4j();
      //    EuMicoPlatformAnno4jModelAssetMMM assetMMM = anno4j.createObject(AssetMMM.class, this.getURI());
      //    StringBuilder location = new StringBuilder()
      //            .append(persistenceService.getStoragePrefix())
      //            .append(this.getURI().getLocalName())
      //            .append("/")
      //            .append(new URIImpl(assetMMM.getResourceAsString()).getLocalName());
      //    assetMMM.setLocation(location.toString());
      //    m_itemMMM.setAsset(assetMMM);
      //      log.trace("No Asset available for Item {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
      //  } catch (IllegalAccessException e) {
      //      throw new RepositoryException("Illegal access", e);
      //  } catch (InstantiationException e) {
      //      throw new RepositoryException("Couldn´t instantiate AssetMMM", e);
      //  }
      //}
      //return new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset(), m_persistenceService);
      throw std::runtime_error("ItemAnno4cpp::getAsset(): Not yet implemented!");
    }
  }
}
