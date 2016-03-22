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

        EuMicoPlatformPersistenceImplItemAnno4j::initContexts( itemMMM );

        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      }

      std::shared_ptr<Part> createPart(mico::rdf::model::URI extractorID);

      std::shared_ptr<Part> getPart(mico::rdf::model::URI uri) {
        jnipp::LocalRef<JavaLangString> juri = JavaLangString::create( uri.stringValue() );
        jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> partMMM = m_persistenceService.getAnno4j()->findByID(EuMicoPlatformAnno4jModelPartMMM::clazz(), juri);
        std::shared_ptr<PartAnno4cpp> part( new PartAnno4cpp(partMMM, std::dynamic_pointer_cast<Item>( shared_from_this() ), m_persistenceService) );
        return part;
      }

      mico::rdf::model::URI getURI() {
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(m_itemMMM)->getResourceAsString() );
        return mico::rdf::model::URI( juri->stringValue()->std_str() );
      }

      jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> getRDFObject() {
        return m_itemMMM;
      }

      std::list< std::shared_ptr<Part> > getParts();

      std::string getSyntacticalType() {
        return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getSyntacticalType()->std_str();
      }

      void setSyntacticalType(std::string syntacticalType) {
        jnipp::LocalRef<JavaLangString> jsyntacticalType = JavaLangString::create(syntacticalType);
        static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->setSyntacticalType(jsyntacticalType);
      }

      std::string getSemanticType() {
        return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getSemanticType()->std_str();
      }

      void setSemanticType(std::string semanticType) {
        jnipp::LocalRef<JavaLangString> jsemanticType = JavaLangString::create(semanticType);
        static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->setSemanticType(jsemanticType);
      }

      std::shared_ptr<Asset> getAsset();

      bool hasAsset() {
        jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset();
        return (jobject)asset != nullptr;
      }

      std::string getSerializedAt() {
        return m_itemMMM->getSerializedAt()->std_str();
      }

    };
  }
}
#endif
