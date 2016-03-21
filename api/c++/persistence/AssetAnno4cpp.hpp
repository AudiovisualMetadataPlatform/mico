#ifndef ASSETANNO4CPP_HPP
#define ASSETANNO4CPP_HPP 1

#include "Asset.hpp"
#include "PersistenceService.hpp"
#include "URLStream.hpp"

namespace mico {
  namespace persistence {

    class AssetAnno4cpp: public Asset
    {
    private:
      jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> m_assetMMM;

    public:
      AssetAnno4cpp(jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> assetMMM)
        : m_assetMMM(assetMMM)
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
        std::string id = this->getLocation().stringValue().substr(/* baseUrl.length() + */ 1);
        return new mico::io::url_ostream(/* contentDirectory  + */ "/" + id + ".bin");
      }

      std::istream* getInputStream() {
        std::string id = this->getLocation().stringValue().substr(/* baseUrl.length() + */ 1);
        return new mico::io::url_istream(/* contentDirectory  + */ "/" + id + ".bin");
      }
    };
  }
}
#endif
