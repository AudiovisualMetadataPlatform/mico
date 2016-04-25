#ifndef RESOURCEANNO4CPP_HPP
#define RESOURCEANNO4CPP_HPP 1

#include "Resource.hpp"
#include "PersistenceService.hpp"
#include "JnippExcpetionHandling.hpp"
#include "Logging.hpp"
#include <jnipp.h>
#include <anno4cpp.h>

namespace mico {
  namespace persistence {
    namespace model {

      class ResourceAnno4cpp : public Resource {

      private:
          jnipp::WeakRef<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> m_resourceMMM;

      protected:
          PersistenceService& m_persistenceService;
          std::string m_jnippErrorMessage;

          ResourceAnno4cpp(jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> resourceMMM,
                           PersistenceService& persistenceService);

      public:
          virtual mico::rdf::model::URI getURI();

          virtual jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> getRDFObject();

          virtual std::string getSyntacticalType();

          virtual void setSyntacticalType(std::string syntacticalType);

          virtual std::string getSemanticType();

          virtual void setSemanticType(std::string semanticType);




  //        @Override
  //        public final Asset getAsset() throws RepositoryException {
  //            if (resourceMMM.getAsset() == null) {
  //                AssetMMM assetMMM = createObject(AssetMMM.class);
  //                StringBuilder location = new StringBuilder()
  //                        .append(persistenceService.getStoragePrefix())
  //                        .append(this.getURI().getLocalName())
  //                        .append("/")
  //                        .append(new URIImpl(assetMMM.getResourceAsString()).getLocalName());
  //                assetMMM.setLocation(location.toString());

  //                resourceMMM.setAsset(assetMMM);

  //                log.trace("No Asset available for Resource {} - Created new Asset with id {} and location {}",
  //                        this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
  //            }
  //            return new AssetAnno4j(this.resourceMMM.getAsset(), this.persistenceService.getStorage());
  //        }

  //        @Override
  //        public final boolean hasAsset() throws RepositoryException {
  //            return resourceMMM.getAsset() != null;
  //        }

  //        protected <T extends RDFObject> T createObject(Class<T> clazz) throws RepositoryException{
  //            return createObject(null,clazz);
  //        }
  //        protected <T extends RDFObject> T createObject(URI resource, Class<T> clazz) throws RepositoryException{
  //            ObjectConnection con = resourceMMM.getObjectConnection();
  //            return con.addDesignation(con.getObjectFactory().createObject(
  //                    resource == null ? IDGenerator.BLANK_RESOURCE : resource , clazz), clazz);
  //        }
      };
    }
  }
}

#endif
