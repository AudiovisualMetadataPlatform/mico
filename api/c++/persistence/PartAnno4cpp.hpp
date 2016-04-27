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

        mico::rdf::model::URI getURI() {
          jnipp::LocalRef<URIImpl> juri = URIImpl::construct( static_cast< jnipp::LocalRef<ResourceObject> >(m_partMMM)->getResourceAsString() );
          return mico::rdf::model::URI( juri->stringValue()->std_str() );
        }

        jnipp::LocalRef<Body> getBody() {
          return m_partMMM->getBody();
        }

        void setBody(const jnipp::LocalRef<Body> &body) {
          m_partMMM->setBody(body);
        }

        std::list< jnipp::LocalRef<Target> > getTargets();

        void setTargets(std::list< jnipp::LocalRef<Target> > targets);

        void addTarget(const jnipp::LocalRef<Target> &target) {
          m_partMMM->addTarget(target);
        }

        std::list< std::shared_ptr<Resource> > getInputs();

        void setInputs(std::list<std::shared_ptr<Resource> > inputs);

        void addInput(Resource& input) {
          m_partMMM->addInput( input.getRDFObject() );
        }

         std::string getSerializedAt() {
          return static_cast< jnipp::LocalRef<Annotation> >(m_partMMM)->getSerializedAt()->std_str();
        }

        jnipp::LocalRef<Agent> getSerializedBy() {
          return static_cast< jnipp::LocalRef<Annotation> >(m_partMMM)->getSerializedBy();
        }


      };
    }
  }
}
#endif
