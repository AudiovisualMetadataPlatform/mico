#ifndef ITEMANNO4CPP_HPP
#define ITEMANNO4CPP_HPP 1

#include "rdf_model.hpp"
#include "Item.hpp"
#include "PartAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"

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

        std::shared_ptr<Part> createPart(const mico::persistence::model::URI& extractorID);

        std::shared_ptr<Part> getPart(const mico::persistence::model::URI& uri);

        std::list< std::shared_ptr<Part> > getParts();

        std::string getSerializedAt() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          std::string timestamp =  m_itemMMM->getSerializedAt()->std_str();
          m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
          return timestamp;
        }



      };
    }
  }
}
#endif
