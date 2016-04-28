
#include "ResourceAnno4cpp.hpp"

#include <jnipp.h>
#include <anno4cpp.h>
#include "Logging.hpp"


using namespace jnipp::java::lang;
using namespace jnipp::org::openrdf::idGenerator;
using namespace jnipp::org::openrdf::repository::sparql;
using namespace jnipp::org::openrdf::model;
using namespace jnipp::org::openrdf::model::impl;
using namespace jnipp::org::openrdf::repository::object;
using namespace jnipp::org::openrdf::sail::memory::model;
using namespace jnipp::com::github::anno4j;
using namespace jnipp::eu::mico::platform::anno4j::model;
using namespace jnipp::eu::mico::platform::persistence::impl;

namespace mico {
  namespace persistence {
    namespace model {

      ResourceAnno4cpp::ResourceAnno4cpp(jnipp::Ref<ResourceMMM> resourceMMM,
                       PersistenceService& persistenceService) :
      m_persistenceService(persistenceService),
      m_resourceMMM(resourceMMM)
      {
        if (resourceMMM->isInstanceOf(ItemMMM::clazz())) {
          LOG_DEBUG("ResourceAnno4cpp::ResourceAnno4cpp for ItemMMM created");
        } else if (resourceMMM->isInstanceOf(PartMMM::clazz())) {
          LOG_DEBUG("ResourceAnno4cpp::ResourceAnno4cpp for PartMMM created");
        } else {
          LOG_WARN("ResourceAnno4cpp::ResourceAnno4cpp for unknown MMM implementation created");
        }
      }

      mico::rdf::model::URI ResourceAnno4cpp::getURI() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);

        jnipp::LocalRef<URI> jResourceURI =
            ((jnipp::Ref<RDFObject>)m_resourceMMM)->getResource();

        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject)jResourceURI);

        return mico::rdf::model::URI(jResourceURI->toString()->std_str());
      }

      jnipp::Ref<ResourceMMM> ResourceAnno4cpp::getRDFObject() {
        return static_cast<jnipp::Ref<ResourceMMM>>(m_resourceMMM);
      }

      std::string ResourceAnno4cpp::getSyntacticalType() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::string type = static_cast< jnipp::LocalRef<ResourceMMM> >(m_resourceMMM)->getSyntacticalType()->std_str();
        LOG_DEBUG("ResourceAnno4cpp::getSyntacticalType() delivers %s", type.c_str());
        return type;
      }

      void ResourceAnno4cpp::setSyntacticalType(std::string syntacticalType) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<String> jsyntacticalType = String::create(syntacticalType);
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jsyntacticalType);
        static_cast< jnipp::LocalRef<ResourceMMM> >(m_resourceMMM)->setSyntacticalType(jsyntacticalType);
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
      }

      std::string ResourceAnno4cpp::getSemanticType() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        std::string type = static_cast< jnipp::LocalRef<ResourceMMM> >(m_resourceMMM)->getSemanticType()->std_str();
        LOG_DEBUG("ResourceAnno4cpp::getSemanticType() delivers %s", type.c_str());
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        return type;
      }

      void ResourceAnno4cpp::setSemanticType(std::string semanticType) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<String> jsemanticType = String::create(semanticType);
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject) jsemanticType);
        static_cast< jnipp::LocalRef<ResourceMMM> >(m_resourceMMM)->setSemanticType(jsemanticType);
        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
      }

      std::shared_ptr<Asset> ResourceAnno4cpp::getAsset()
      {
          throw std::runtime_error("Not implementend yet");
          return std::shared_ptr<Asset>();
      }

      bool ResourceAnno4cpp::hasAsset() {
        throw std::runtime_error("Not implemented yet!");
        return false;
      }
    }
  }
}
