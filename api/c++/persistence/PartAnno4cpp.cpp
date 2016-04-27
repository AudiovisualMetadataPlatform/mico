#include "PartAnno4cpp.hpp"
#include "ItemAnno4cpp.hpp"

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
        jnipp::LocalRef< JavaUtilHashSet > jtargetSet = JavaUtilHashSet::construct();
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
        std::list< std::shared_ptr<Resource> > resourceSet;

  //      jnipp::LocalRef<JavaUtilSet> jset = m_partMMM->getInputs();
  //      jnipp::LocalRef< jnipp::Array<JavaLangObject> > jarray = static_cast< jnipp::LocalRef<JavaUtilHashSet> >(jset)->toArray();
  //      for (jsize i = 0; i < jarray->length(); i++) {
  //        jnipp::LocalRef<JavaLangObject> jobject = jarray->get(i);
  //        if ( jobject->isInstanceOf(EuMicoPlatformAnno4jModelItemMMM::clazz()) ) {
  //          std::shared_ptr<ItemAnno4cpp> item(new ItemAnno4cpp(jobject, m_persistenceService));
  //          resourceSet.push_back( item );
  //        } else {
  //          std::shared_ptr<PartAnno4cpp> part(new PartAnno4cpp(jobject, m_item, m_persistenceService));
  //          resourceSet.push_back( part );
  //        }
  //      }
        return resourceSet;
      }

      void PartAnno4cpp::setInputs(std::list< std::shared_ptr<Resource> > inputs)
      {
        jnipp::LocalRef< JavaUtilHashSet > jresourceMMMSet = JavaUtilHashSet::construct();
        for(auto iter = inputs.begin(); iter != inputs.end(); iter++) {
            jresourceMMMSet->add( (*iter)->getRDFObject() );
        }
        m_partMMM->setInputs(jresourceMMMSet);
      }

      void PartAnno4cpp::addInput(Resource& input) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        m_partMMM->addInput( input.getRDFObject() );
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

        jnipp::LocalRef<JavaUtilSet> jset = m_partMMM->getTarget();
        jnipp::LocalRef< jnipp::Array<JavaLangObject> > jarray = static_cast< jnipp::LocalRef<JavaUtilHashSet> >(jset)->toArray();
        for (jsize i = 0; i < jarray->length(); i++) {
          jnipp::LocalRef<JavaLangObject> jobject = jarray->get(i);
          list.push_back( jobject );
        }
        return list;
      }
    }
  }
}

