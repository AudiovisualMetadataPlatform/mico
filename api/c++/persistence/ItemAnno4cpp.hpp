#ifndef ITEMANNO4CPP_HPP
#define ITEMANNO4CPP_HPP 1

#include "rdf_model.hpp"
#include "Item.hpp"
#include "PartAnno4cpp.hpp"
#include "ResourceAnno4cpp.hpp"
#include "JnippExcpetionHandling.hpp"

namespace mico {
  namespace persistence {
    class ItemAnno4cpp: public Item, public ResourceAnno4cpp
    {
    protected:
      //PersistenceService& m_persistenceService;
      jnipp::GlobalRef<EuMicoPlatformAnno4jModelItemMMM> m_itemMMM;

    public:
      ItemAnno4cpp(jnipp::Ref<EuMicoPlatformAnno4jModelItemMMM> itemMMM, PersistenceService& persistenceService)
        : ResourceAnno4cpp(itemMMM, persistenceService),
          //m_persistenceService(persistenceService),
          m_itemMMM(itemMMM)

      {
      }

      std::shared_ptr<Part> createPart(const rdf::model::URI& extractorID);

      std::shared_ptr<Part> getPart(const rdf::model::URI& uri);

      //mico::rdf::model::URI getURI();


      std::list< std::shared_ptr<Part> > getParts();


      std::shared_ptr<Asset> getAsset();

      bool hasAsset() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_itemMMM)->getAsset();
        return (jobject)asset != nullptr;
      }

      std::string getSerializedAt() {
        jnipp::Env::Scope scope(PersistenceService::m_sJvm);
        std::string timestamp =  m_itemMMM->getSerializedAt()->std_str();
        checkJavaExcpetionNoThrow(m_jnippErrorMessage);
        return timestamp;
      }



    };
  }
}
#endif
