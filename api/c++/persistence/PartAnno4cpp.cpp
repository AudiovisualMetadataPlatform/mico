#include "PartAnno4cpp.hpp"
#include "ItemAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"


using namespace jnipp::java::lang;
using namespace jnipp::java::util;
using namespace jnipp::org::openrdf::model::impl;
using namespace jnipp::eu::mico::platform::anno4j::model;
using namespace jnipp::com::github::anno4j::model;
using namespace jnipp::com::github::anno4j::model::impl;


namespace mico {
  namespace persistence {
    namespace model {

      jnipp::LocalRef<Body> PartAnno4cpp::getBody() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<Body> jBody = m_partMMM->getBody();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jBody);
        return jBody;
      }

      void PartAnno4cpp::setBody(const jnipp::LocalRef<Body> &body) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        m_partMMM->setBody(body);
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      }


      void PartAnno4cpp::setTargets(std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > targets)
      {
        jnipp::LocalRef< HashSet > jtargetSet = HashSet::construct();
        for(auto iter = targets.begin(); iter != targets.end(); iter++) {
          jtargetSet->add(*iter);
        }
        m_partMMM->setTarget(jtargetSet);
      }

      void PartAnno4cpp::addTarget(const jnipp::LocalRef<Target> &target) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        m_partMMM->addTarget(target);
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      }

      std::list< std::shared_ptr<Resource> > PartAnno4cpp::getInputs()
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);

        std::list< std::shared_ptr<Resource> > nativeResourceSet;

        jnipp::LocalRef<Set> jInputSet = m_partMMM->getInputs();

        jnipp::LocalRef< jnipp::Array<Object> > jInputArray = jInputSet->toArray();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jInputArray);

        LOG_DEBUG("Retrieved %d inputs(s) in array for part %s", jInputArray->length(), this->getURI().stringValue().c_str());

        for (auto it = jInputArray->begin();  it!= jInputArray->end(); ++it) {
          jnipp::LocalRef<Object> jObject = *it;
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          assert((jobject) jObject);

          std::shared_ptr<Resource> res;

          bool foundKnown = false;
          std::string typeName;

          if (jObject->isInstanceOf(ItemMMM::clazz())) {
            res =  std::make_shared<ItemAnno4cpp> (jObject, m_persistenceService);
            foundKnown = true;
            typeName = "ItemMMM";
          }

          if (jObject->isInstanceOf(PartMMM::clazz())) {
            res =  std::make_shared<PartAnno4cpp> (jObject, this->m_item, m_persistenceService);
            foundKnown = true;
            typeName = "PartMMM";
          }

          if (foundKnown) {
          LOG_DEBUG("Retrieved input %s of type %s for part %s",
                    res->getURI().stringValue().c_str(),
                    typeName.c_str(),
                    this->getURI().stringValue().c_str());
          } else {
            LOG_WARN("Retrieved %s which is neither PartMMM nor ItemMMM for part %s",
                      res->getURI().stringValue().c_str(),
                      this->getURI().stringValue().c_str());
          }
          nativeResourceSet.push_back( res );
        }

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        return nativeResourceSet;
      }

      void PartAnno4cpp::setInputs(std::list< std::shared_ptr<mico::persistence::model::Resource> > inputs)
      {
        jnipp::LocalRef< HashSet > jresourceMMMSet = HashSet::construct();
        for(auto iter = inputs.begin(); iter != inputs.end(); iter++) {
            jresourceMMMSet->add( (*iter)->getRDFObject() );
        }
        m_partMMM->setInputs(jresourceMMMSet);
      }

      void PartAnno4cpp::addInput( std::shared_ptr<mico::persistence::model::Resource> input) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        m_partMMM->addInput( input->getRDFObject() );
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      }

       std::string PartAnno4cpp::getSerializedAt() {
         jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::string sSerializedAt = static_cast< jnipp::LocalRef<Annotation> >(m_partMMM)->getSerializedAt()->std_str();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        return sSerializedAt;
      }

      jnipp::LocalRef<Agent> PartAnno4cpp::getSerializedBy() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<Agent> jAgent = static_cast< jnipp::LocalRef<Annotation> >(m_partMMM)->getSerializedBy();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        assert((jobject) jAgent);
        return jAgent;
      }


      std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > PartAnno4cpp::getTargets() {
        std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > list;

        jnipp::LocalRef<Set> jset = m_partMMM->getTarget();
        jnipp::LocalRef< jnipp::Array<Object> > jarray = static_cast< jnipp::LocalRef<HashSet> >(jset)->toArray();
        for (jsize i = 0; i < jarray->length(); i++) {
          jnipp::LocalRef<Object> jobject = jarray->get(i);
          list.push_back( jobject );
        }
        return list;
      }
    }
  }
}

