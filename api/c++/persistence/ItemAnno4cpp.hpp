#ifndef ITEMANNO4CPP_HPP
#define ITEMANNO4CPP_HPP 1

#include "Item.hpp"
#include "PartAnno4cpp.hpp"

namespace mico {
  namespace persistence {
    class ItemAnno4cpp: public Item
    {
    private:
      const PersistenceService& m_persistenceService;
      jnipp::LocalRef<EuMicoPlatformAnno4jModelItemMMM> m_itemMMM;

    public:
      ItemAnno4cpp(jnipp::LocalRef<EuMicoPlatformAnno4jModelItemMMM> itemMMM, const PersistenceService& persistenceService)
        : m_persistenceService(persistenceService),
          m_itemMMM(itemMMM)
      {
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> context = OrgOpenrdfModelImplURIImpl::construct( static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(m_itemMMM)->getResourceAsString() );
        jnipp::LocalRef<OrgOpenrdfRepositoryObjectObjectConnection> objectConnection = static_cast< jnipp::LocalRef<OrgOpenrdfRepositoryObjectRDFObject> >(m_itemMMM)->getObjectConnection();
        objectConnection->setInsertContext(context);
        //itemMMM.getObjectConnection().setReadContexts(context);
        //itemMMM.getObjectConnection().setRemoveContexts(context);
      }

      Part* createPart(mico::rdf::model::URI extractorID);

      Part* getPart(mico::rdf::model::URI uri) {
        //EuMicoPlatformAnno4jModelPartMMM = persistenceService.getAnno4j().findByID(PartMMM.class, uri);
        //return new PartAnno4cpp(partMMM, this, persistenceService);
      }

      mico::rdf::model::URI getURI() {
        //return new URIImpl(itemMMM.getResourceAsString());
      }

      jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> getRDFObject() {
        return m_itemMMM;
      }

      //Iterable<? extends Part> ItemAnno4cpp::getParts();

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

      Asset* getAsset();

      bool hasAsset() {
        jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset();
        throw std::runtime_error("ItemAnno4cpp::hasAsset(): Not yet implemented!");
      }

      std::string getSerializedAt() {
        return m_itemMMM->getSerializedAt()->std_str();
      }
    };
  }
}
#endif
