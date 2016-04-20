#ifndef ITEMANNO4CPP_HPP
#define ITEMANNO4CPP_HPP 1

#include "Item.hpp"
#include "PartAnno4cpp.hpp"

#include "JnippExcpetionHandling.hpp"

namespace mico {
  namespace persistence {
    class ItemAnno4cpp: public Item
    {
    private:
      PersistenceService& m_persistenceService;
      jnipp::GlobalRef<EuMicoPlatformAnno4jModelItemMMM> m_itemMMM;
      std::string m_jnippErrorMessage;

    public:
      ItemAnno4cpp(jnipp::Ref<EuMicoPlatformAnno4jModelItemMMM> itemMMM, PersistenceService& persistenceService)
        : m_persistenceService(persistenceService),
          m_itemMMM(itemMMM)
      {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        //EuMicoPlatformPersistenceImplItemAnno4j::initContexts( itemMMM );
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      }

      std::shared_ptr<Part> createPart(mico::rdf::model::URI extractorID);

      std::shared_ptr<Part> getPart(mico::rdf::model::URI uri);

      mico::rdf::model::URI getURI();

      jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> getRDFObject() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        return m_itemMMM;
      }

      std::list< std::shared_ptr<Part> > getParts();

      std::string getSyntacticalType() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getSyntacticalType()->std_str();
      }

      void setSyntacticalType(std::string syntacticalType) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<JavaLangString> jsyntacticalType = JavaLangString::create(syntacticalType);
        static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->setSyntacticalType(jsyntacticalType);
      }

      std::string getSemanticType() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getSemanticType()->std_str();
      }

      void setSemanticType(std::string semanticType) {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<JavaLangString> jsemanticType = JavaLangString::create(semanticType);
        static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->setSemanticType(jsemanticType);
      }

      std::shared_ptr<Asset> getAsset();

      bool hasAsset() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset();
        return (jobject)asset != nullptr;
      }

      std::string getSerializedAt() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        return m_itemMMM->getSerializedAt()->std_str();
      }

    };
  }
}
#endif
