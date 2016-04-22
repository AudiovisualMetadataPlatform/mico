
#include "ResourceAnno4cpp.hpp"

#include <jnipp.h>
#include <anno4cpp.h>
#include "JnippExcpetionHandling.hpp"
#include "Logging.hpp"


namespace mico {
  namespace persistence {
    mico::rdf::model::URI ResourceAnno4cpp::getURI() {
      LOG_DEBUG("ResourceAnno4cpp::getURI()");
      jnipp::Env::Scope scope(PersistenceService::m_sJvm);


      jnipp::LocalRef<OrgOpenrdfModelURI> jResourceURI =
          ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)m_resourceMMM)->getResource();

      LOG_DEBUG("ResourceAnno4cpp:: URI is [%s]", jResourceURI->toString()->std_str().c_str());

      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      assert((jobject)jResourceURI);

      return mico::rdf::model::URI(jResourceURI->toString()->std_str());
    }

    jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> ResourceAnno4cpp::getRDFObject() {
      return static_cast<jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM>>(m_resourceMMM);
    }

    std::string ResourceAnno4cpp::getSyntacticalType() {
      jnipp::Env::Scope scope(PersistenceService::m_sJvm);
      std::string type = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_resourceMMM)->getSyntacticalType()->std_str();
      LOG_DEBUG("ResourceAnno4cpp::getSyntacticalType() delivers %s", type.c_str());
      return type;
    }

    void ResourceAnno4cpp::setSyntacticalType(std::string syntacticalType) {
      jnipp::Env::Scope scope(PersistenceService::m_sJvm);
      jnipp::LocalRef<JavaLangString> jsyntacticalType = JavaLangString::create(syntacticalType);
      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      assert((jobject) jsyntacticalType);
      static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_resourceMMM)->setSyntacticalType(jsyntacticalType);
      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
    }

    std::string ResourceAnno4cpp::getSemanticType() {
      jnipp::Env::Scope scope(PersistenceService::m_sJvm);
      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      std::string type = static_cast< jnipp::LocalRef<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> >(m_resourceMMM)->getSemanticType()->std_str();
      LOG_DEBUG("ResourceAnno4cpp::getSemanticType() delivers %s", type.c_str());
      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      return type;
    }

    void ResourceAnno4cpp::setSemanticType(std::string semanticType) {
      jnipp::Env::Scope scope(PersistenceService::m_sJvm);
      jnipp::LocalRef<JavaLangString> jsemanticType = JavaLangString::create(semanticType);
      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      assert((jobject) jsemanticType);
      static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_resourceMMM)->setSemanticType(jsemanticType);
      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
    }

//    protected <T extends RDFObject> T createObject(Class<T> clazz) throws RepositoryException{
//          return createObject(null,clazz);
//      }
//      protected <T extends RDFObject> T createObject(URI resource, Class<T> clazz) throws RepositoryException{
//          ObjectConnection con = resourceMMM.getObjectConnection();
//          return con.addDesignation(con.getObjectFactory().createObject(
//                  resource == null ? IDGenerator.BLANK_RESOURCE : resource , clazz), clazz);
//      }



  }
}
