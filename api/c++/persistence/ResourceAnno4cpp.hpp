#ifndef RESOURCEANNO4CPP_HPP
#define RESOURCEANNO4CPP_HPP 1

#include "Resource.hpp"
#include "PersistenceService.hpp"
#include "JnippExcpetionHandling.hpp"
#include "Logging.hpp"
#include <jnipp.h>
#include <anno4cpp.h>

namespace mico {
  namespace persistence {

    class ResourceAnno4cpp : public Resource {

    private:
        jnipp::GlobalRef<EuMicoPlatformAnno4jModelResourceMMM> m_resourceMMM;

    protected:
        PersistenceService& m_persistenceService;
        std::string m_jnippErrorMessage;

        ResourceAnno4cpp(jnipp::Ref<EuMicoPlatformAnno4jModelResourceMMM> resourceMMM,
                         PersistenceService& persistenceService) :
        m_persistenceService(persistenceService),
        m_resourceMMM(resourceMMM)
        {
          LOG_DEBUG("ResourceAnno4cpp::ResourceAnno4cpp of type [%s] created", resourceMMM->getClass()->toString()->std_str().c_str());

          if (resourceMMM->isInstanceOf(EuMicoPlatformAnno4jModelItemMMM::clazz()))
            LOG_DEBUG("ResourceAnno4cpp::ResourceAnno4cpp of type [%s] created", resourceMMM->getClass()->toString()->std_str().c_str());
        }
    public:
        virtual mico::rdf::model::URI getURI();

        virtual jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> getRDFObject() {
          return m_resourceMMM;
        }

        virtual std::string getSyntacticalType() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          std::string type = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_resourceMMM)->getSyntacticalType()->std_str();
          LOG_DEBUG("ResourceAnno4cpp::getSyntacticalType() delivers %s", type.c_str());
          return type;
        }

        virtual void setSyntacticalType(std::string syntacticalType) {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          jnipp::LocalRef<JavaLangString> jsyntacticalType = JavaLangString::create(syntacticalType);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          assert((jobject) jsyntacticalType);
          static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_resourceMMM)->setSyntacticalType(jsyntacticalType);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        }

        virtual std::string getSemanticType() {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          std::string type = static_cast< jnipp::LocalRef<jnipp::eu::mico::platform::anno4j::model::ResourceMMM> >(m_resourceMMM)->getSemanticType()->std_str();
          LOG_DEBUG("ResourceAnno4cpp::getSemanticType() delivers %s", type.c_str());
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          return type;
        }

        virtual void setSemanticType(std::string semanticType) {
          jnipp::Env::Scope scope(PersistenceService::m_sJvm);
          jnipp::LocalRef<JavaLangString> jsemanticType = JavaLangString::create(semanticType);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
          assert((jobject) jsemanticType);
          static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_resourceMMM)->setSemanticType(jsemanticType);
          checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        }




//        @Override
//        public final ResourceMMM getRDFObject() {
//            return resourceMMM;
//        }

//        @Override
//        public final String getSyntacticalType() {
//            return resourceMMM.getSyntacticalType();
//        }

//        @Override
//        public final void setSyntacticalType(String syntacticalType) throws RepositoryException {
//            resourceMMM.setSyntacticalType(syntacticalType);
//        }

//        @Override
//        public final String getSemanticType() {
//            return resourceMMM.getSemanticType();
//        }

//        @Override
//        public final void setSemanticType(String semanticType) throws RepositoryException {
//            resourceMMM.setSemanticType(semanticType);
//        }

//        @Override
//        public final Asset getAsset() throws RepositoryException {
//            if (resourceMMM.getAsset() == null) {
//                AssetMMM assetMMM = createObject(AssetMMM.class);
//                StringBuilder location = new StringBuilder()
//                        .append(persistenceService.getStoragePrefix())
//                        .append(this.getURI().getLocalName())
//                        .append("/")
//                        .append(new URIImpl(assetMMM.getResourceAsString()).getLocalName());
//                assetMMM.setLocation(location.toString());

//                resourceMMM.setAsset(assetMMM);

//                log.trace("No Asset available for Resource {} - Created new Asset with id {} and location {}",
//                        this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
//            }
//            return new AssetAnno4j(this.resourceMMM.getAsset(), this.persistenceService.getStorage());
//        }

//        @Override
//        public final boolean hasAsset() throws RepositoryException {
//            return resourceMMM.getAsset() != null;
//        }

//        protected <T extends RDFObject> T createObject(Class<T> clazz) throws RepositoryException{
//            return createObject(null,clazz);
//        }
//        protected <T extends RDFObject> T createObject(URI resource, Class<T> clazz) throws RepositoryException{
//            ObjectConnection con = resourceMMM.getObjectConnection();
//            return con.addDesignation(con.getObjectFactory().createObject(
//                    resource == null ? IDGenerator.BLANK_RESOURCE : resource , clazz), clazz);
//        }
    };
  }
}

#endif
