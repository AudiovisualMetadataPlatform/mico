#ifndef ITEMANNO4CPP_HPP
#define ITEMANNO4CPP_HPP 1

#include "rdf_model.hpp"
#include "Item.hpp"
#include "PartAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"
#include "anno4cpp.h"

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

        /**
         * Create a new jnipp Object of the desired class.
         */
        jnipp::Ref<jnipp::java::lang::Object>& createObject(const jnipp::Ref<jnipp::Class>& clazz);

        /**
         * Create a new jnipp Object of the desired class using the connection.
         *
         * Notice that this method does not commit the transaction.
         *
         */
        jnipp::Ref<jnipp::java::lang::Object>& createObjectNoCommit(
            jnipp::Ref<jnipp::org::openrdf::repository::object::ObjectConnection> con,
            const jnipp::Ref<jnipp::Class>& clazz);

        /**
         * Retrieve an existing jnipp Object of the desired class.
         */
        jnipp::Ref<jnipp::java::lang::Object>& findObject(const  mico::persistence::model::URI& uri,
                                                          const jnipp::Ref<jnipp::Class>& clazz);




      };
    }
  }
}
#endif
