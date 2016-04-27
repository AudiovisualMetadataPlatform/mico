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

        jnipp::LocalRef<Body> getBody() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          jnipp::LocalRef<Body> jBody = m_partMMM->getBody();
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          assert((jobject) jBody);
          return jBody;
        }

        void setBody(const jnipp::LocalRef<Body> &body) {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          m_partMMM->setBody(body);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        }

        std::list< jnipp::LocalRef<Target> > getTargets();

        void setTargets(std::list< jnipp::LocalRef<Target> > targets);

        void addTarget(const jnipp::LocalRef<Target> &target) {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          m_partMMM->addTarget(target);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        }

        std::list< std::shared_ptr<Resource> > getInputs();

        void setInputs(std::list<std::shared_ptr<Resource> > inputs);

        void addInput(Resource& input) {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          m_partMMM->addInput( input.getRDFObject() );
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        }

         std::string getSerializedAt() {
           jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          std::string sSerializedAt = static_cast< jnipp::LocalRef<Annotation> >(m_partMMM)->getSerializedAt()->std_str();
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          return sSerializedAt;
        }

        jnipp::LocalRef<Agent> getSerializedBy() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          jnipp::LocalRef<Agent> jAgent = static_cast< jnipp::LocalRef<Annotation> >(m_partMMM)->getSerializedBy();
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          assert((jobject) jAgent);
          return jAgent;
        }


      };
    }
  }
}
#endif
