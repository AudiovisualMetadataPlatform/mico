

#include "ResourceAnno4cpp.hpp"
#include "ItemAnno4cpp.hpp"
#include "PartAnno4cpp.hpp"
#include "AssetAnno4cpp.hpp"
#include "Uri.hpp"

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
using namespace jnipp::com::github::anno4j::model::impl;
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

      mico::persistence::model::URI ResourceAnno4cpp::getURI() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);

        jnipp::LocalRef<jnipp::org::openrdf::model::URI> jResourceURI =
            ((jnipp::Ref<RDFObject>)m_resourceMMM)->getResource();

        m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
        assert((jobject)jResourceURI);

        return mico::persistence::model::URI(jResourceURI->toString()->std_str());
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
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::string assetType;

        jnipp::GlobalRef<AssetMMM> jAssetMMM =
            ((jnipp::LocalRef<ResourceMMM>)m_resourceMMM)->getAsset();

        bool error = false;

        if (!m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage)) {

          if (!(jobject) jAssetMMM) {

            jnipp::LocalRef<Transaction> jTransaction = m_persistenceService.getAnno4j()->createTransaction();
            assert(jTransaction);
            m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

            jTransaction->begin();

            std::shared_ptr<Item> asItem = std::dynamic_pointer_cast<Item>(shared_from_this());
            std::shared_ptr<Part> asPart = std::dynamic_pointer_cast<Part>(shared_from_this());

            if(asItem) {
                jTransaction->setAllContexts(((jnipp::Ref<RDFObject>)m_resourceMMM)->getResource());
                assetType = "Item";
            } else {
                assert(asPart);
                std::shared_ptr<model::Resource> parentItemResource = std::dynamic_pointer_cast<model::Resource>(asPart->getItem());
                jTransaction->setAllContexts( ((jnipp::Ref<RDFObject>) parentItemResource->getRDFObject())->getResource());
                assetType = "Part";
            }

            jAssetMMM = jTransaction->createObject(AssetMMM::clazz());

            assert(jAssetMMM);
            error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

            std::stringstream ss;

            jnipp::LocalRef<URIImpl> jUriImpl =
                URIImpl::construct( ((jnipp::Ref<ResourceObject>)jAssetMMM)->getResourceAsString() );

            jnipp::LocalRef<URIImpl> jResourceUriImpl =
                URIImpl::construct( ((jnipp::Ref<ResourceObject>)m_resourceMMM)->getResourceAsString() );

            ss << m_persistenceService.getContentDirectory()
               << "/" << jResourceUriImpl->getLocalName()->std_str()
               << "/" << jUriImpl->getLocalName()->std_str();

            jAssetMMM->setLocation(jnipp::String::create(ss.str()));
            error = error & m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);


            if(error){
                jTransaction->rollback();
                jTransaction->close();
                return std::shared_ptr<Asset>();
            } else {
                jTransaction->commit();
            }

            ((jnipp::Ref<ResourceMMM>)m_resourceMMM)->setAsset(jAssetMMM);
            m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);

            LOG_DEBUG("No Asset available for Resource(%s) %s - Created new Asset with id %s and location %s",
                      assetType.c_str(),
                      getURI().stringValue().c_str(),
                      ((jnipp::Ref<ResourceObject>)jAssetMMM)->getResourceAsString()->std_str().c_str(),
                      jAssetMMM->getLocation()->std_str().c_str());
         }
         return std::make_shared<AssetAnno4cpp>(jAssetMMM,m_persistenceService);
        }
        return std::shared_ptr<Asset>();
      }

      bool ResourceAnno4cpp::hasAsset() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        if (!((jobject)((jnipp::LocalRef<ResourceMMM>)m_resourceMMM)->getAsset())) {
          return false;
        }
        return true;
      }
    }
  }
}
