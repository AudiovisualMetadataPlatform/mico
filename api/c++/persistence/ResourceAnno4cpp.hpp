#ifndef RESOURCEANNO4CPP_HPP
#define RESOURCEANNO4CPP_HPP 1

#include "Resource.hpp"
#include "Uri.hpp"
#include "PersistenceService.hpp"
#include "Logging.hpp"
#include <jnipp.h>
#include <anno4cpp.h>

namespace mico {
  namespace persistence {
    namespace model {

      class ResourceAnno4cpp : public Resource {

      private:
          jnipp::WeakRef<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> m_resourceMMM;
          std::shared_ptr<Asset> createAsset(std::string location = "");

      protected:
          PersistenceService& m_persistenceService;
          std::string m_jnippErrorMessage;

          ResourceAnno4cpp(jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> resourceMMM,
                           PersistenceService& persistenceService);

      public:
          virtual mico::persistence::model::URI getURI();

          virtual jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> getRDFObject();

          virtual std::string getSyntacticalType();

          virtual void setSyntacticalType(std::string syntacticalType);

          virtual std::string getSemanticType();

          virtual void setSemanticType(std::string semanticType);


          virtual std::shared_ptr<Asset> getAsset();

          virtual std::shared_ptr<Asset> getAssetWithLocation(mico::persistence::model::URI);

          virtual bool hasAsset();

          virtual mico::persistence::PersistenceService& getPersistenceService() {return m_persistenceService; }

      };
    }
  }
}

#endif
