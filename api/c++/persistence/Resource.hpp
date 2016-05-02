#ifndef RESOURCE_HPP
#define RESOURCE_HPP 1

#include<memory>
#include "Asset.hpp"
#include "anno4cpp.h"

namespace mico {
  namespace rdf {
    namespace model {
      class URI;
    }
  }

  namespace persistence {
      class PersistenceService;
    namespace model {
      /**
       * Super type of items and parts
       */
      class Resource: public std::enable_shared_from_this<Resource>
      {
        public:
          /**
           * Return the identifier (a unique URI) for this item. This URI will be based on the internal UUID of the
           * content item in the platform.
           *
           * @return
           */
          virtual mico::rdf::model::URI getURI() = 0;

          virtual jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> getRDFObject() = 0;

          /**
           * the mime type, e.g. "image/jpeg"
           */
          virtual std::string getSyntacticalType() = 0;

          virtual void setSyntacticalType(std::string syntacticalType) = 0;

          virtual std::string getSemanticType() = 0;

          virtual void setSemanticType(std::string semanticType) = 0;

          virtual std::shared_ptr<Asset> getAsset() = 0;

          virtual bool hasAsset() = 0;

          virtual mico::persistence::PersistenceService& getPersistenceService() = 0;
      };
    }
  }
}
#endif
