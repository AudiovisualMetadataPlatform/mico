#include "ItemAnno4cpp.hpp"

namespace mico {
  namespace persistence {

    Part* ItemAnno4cpp::createPart(mico::rdf::model::URI extractorID)
    {
      //try {
        //EuMicoPlatformAnno4jModelPartMMM partMMM = persistenceService.getAnno4j().createObject(PartMMM.class, this.getURI());
        //std::string dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        //partMMM.setSerializedAt(dateTime);

        //ComGithubAnno4jModelAgent agent = this.persistenceService.getAnno4j().createObject(Agent.class, this.getURI());
        //agent.setResource(extractorID);
        //partMMM.setSerializedBy(agent);

        //m_itemMMM.addPart(partMMM);

        //log.trace("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);

      //  return new PartAnno4cpp(partMMM, this, persistenceService);
      //} catch (...) {
        throw std::runtime_error("ItemAnno4cpp::createPart(): Not yet implemented!");
      //}
    }

    //Iterable<? extends Part> ItemAnno4cpp::getParts() {
    //  ArrayList<PartAnno4j> partsAnno4j = new ArrayList<>();
    //  List<PartMMM> partsMMM = persistenceService.getAnno4j().findAll(PartMMM.class, this.getURI());
    //  for (PartMMM partMMM : partsMMM) {
    //    partsAnno4j.add(new PartAnno4j(partMMM, this, persistenceService));
    //  }
    //  return partsAnno4j;
    //}

    Asset* ItemAnno4cpp::getAsset() {
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
      return new AssetAnno4cpp(static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset(), m_persistenceService);
    }
  }
}
