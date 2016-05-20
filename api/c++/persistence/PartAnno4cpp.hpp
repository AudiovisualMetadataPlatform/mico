#ifndef PARTANNO4CPP_HPP
#define PARTANNO4CPP_HPP 1

#include "Part.hpp"
#include "AssetAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"


namespace mico {
  namespace persistence {
    namespace model {
      class PartAnno4cpp: public Part, public ResourceAnno4cpp
      {

      private:
        PersistenceService& m_persistenceService;
        std::shared_ptr<Item> m_item;
        jnipp::GlobalRef<jnipp::eu::mico::platform::anno4j::model::PartMMM> m_partMMM;

      public:
        PartAnno4cpp(jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::PartMMM> partMMM,
                     std::shared_ptr<Item> item, PersistenceService& persistenceService)
          : ResourceAnno4cpp(partMMM, persistenceService),
            m_persistenceService(persistenceService),
            m_item(item),
            m_partMMM(partMMM)
        {}

        std::shared_ptr<Item> getItem() {
          return m_item;
        }

        jnipp::LocalRef<jnipp::com::github::anno4j::model::Body> getBody();

        void setBody(const jnipp::LocalRef<jnipp::com::github::anno4j::model::Body> &body);

        std::list< jnipp::LocalRef<jnipp::com::github::anno4j::model::Target> > getTargets();

        void setTargets(std::list< jnipp::LocalRef<jnipp::com::github::anno4j::model::Target> > targets);

        void addTarget(const jnipp::LocalRef<jnipp::com::github::anno4j::model::Target> &target);

        std::list< std::shared_ptr<Resource> > getInputs();

        void setInputs(std::list<std::shared_ptr<Resource> > inputs);

        void addInput(std::shared_ptr<Resource> input);

         std::string getSerializedAt();

        jnipp::LocalRef<jnipp::com::github::anno4j::model::Agent> getSerializedBy();

      };
    }
  }
}
#endif
