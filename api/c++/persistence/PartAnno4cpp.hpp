#ifndef PARTANNO4CPP_HPP
#define PARTANNO4CPP_HPP 1

#include "Part.hpp"
#include "AssetAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"

using namespace jnipp::java::lang;
using namespace jnipp::org::openrdf::model::impl;
using namespace jnipp::eu::mico::platform::anno4j::model;
using namespace jnipp::com::github::anno4j::model;
using namespace jnipp::com::github::anno4j::model::impl;

namespace mico {
  namespace persistence {
    namespace model {
      class PartAnno4cpp: public Part, public ResourceAnno4cpp
      {

      private:
        PersistenceService& m_persistenceService;
        std::shared_ptr<Item> m_item;
        jnipp::GlobalRef<PartMMM> m_partMMM;

      public:
        PartAnno4cpp(jnipp::Ref<PartMMM> partMMM, std::shared_ptr<Item> item, PersistenceService& persistenceService)
          : ResourceAnno4cpp(partMMM, persistenceService),
            m_persistenceService(persistenceService),
            m_item(item),
            m_partMMM(partMMM)
        {}

        std::shared_ptr<Item> getItem() {
          return m_item;
        }

        jnipp::LocalRef<Body> getBody();

        void setBody(const jnipp::LocalRef<Body> &body);

        std::list< jnipp::LocalRef<Target> > getTargets();

        void setTargets(std::list< jnipp::LocalRef<Target> > targets);

        void addTarget(const jnipp::LocalRef<Target> &target);

        std::list< std::shared_ptr<Resource> > getInputs();

        void setInputs(std::list<std::shared_ptr<Resource> > inputs);

        void addInput(Resource& input);

         std::string getSerializedAt();

        jnipp::LocalRef<Agent> getSerializedBy();

      };
    }
  }
}
#endif
