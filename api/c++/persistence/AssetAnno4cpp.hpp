#ifndef ASSETANNO4CPP_HPP
#define ASSETANNO4CPP_HPP 1

#include "Asset.hpp"
#include "PersistenceService.hpp"

namespace mico {
  namespace persistence {

    class AssetAnno4cpp: public Asset
    {
    private:
      const PersistenceService& m_persistenceService;
      jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> m_assetMMM;

    public:
      AssetAnno4cpp(jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> assetMMM, const PersistenceService& persistenceService)
        : m_persistenceService(persistenceService),
          m_assetMMM(assetMMM)
      {}

      mico::rdf::model::URI getLocation() {
        jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( m_assetMMM->getLocation() );
        return mico::rdf::model::URI( juri->stringValue()->std_str() );
      }

      std::string getFormat() {
        return m_assetMMM->getFormat()->std_str();
      }

      void setFormat(std::string format) {
        jnipp::LocalRef<JavaLangString> jformat = JavaLangString::create(format);
        m_assetMMM->setFormat(jformat);
      }

      std::ostream* getOutputStream() {
        //->in old Content.cpp
      }

      std::istream* getInputStream() {
        //->in old Content.cpp
      }
    };
  }
}
#endif
