#ifndef ITEMANNO4CPP_HPP
#define ITEMANNO4CPP_HPP 1

#include "rdf_model.hpp"
#include "Item.hpp"
#include "PartAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"
#include "JnippExcpetionHandling.hpp"

namespace mico {
  namespace persistence {
    namespace model {
      class ItemAnno4cpp: public Item, public ResourceAnno4cpp
      {
      protected:
        //PersistenceService& m_persistenceService;

        //the item hold the Global JNI reference to the ItemMMM object
        jnipp::GlobalRef<jnipp::eu::mico::platform::anno4j::model::ItemMMM> m_itemMMM;

      public:
        ItemAnno4cpp(jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ItemMMM> itemMMM, PersistenceService& persistenceService)
          : ResourceAnno4cpp(itemMMM, persistenceService),
            //m_persistenceService(persistenceService),
            m_itemMMM(itemMMM)

        {
        }

        std::shared_ptr<Part> createPart(const rdf::model::URI& extractorID);

        std::shared_ptr<Part> getPart(const rdf::model::URI& uri);

        //mico::rdf::model::URI getURI();


        std::list< std::shared_ptr<Part> > getParts();


        std::shared_ptr<Asset> getAsset();

        bool hasAsset() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          jnipp::LocalRef<jnipp::eu::mico::platform::anno4j::model::AssetMMM> asset =
              static_cast< jnipp::LocalRef<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> >(m_itemMMM)->getAsset();
          return (jobject)asset != nullptr;
        }

        std::string getSerializedAt() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          std::string timestamp =  m_itemMMM->getSerializedAt()->std_str();
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          return timestamp;
        }



      };
    }
  }
}
#endif
