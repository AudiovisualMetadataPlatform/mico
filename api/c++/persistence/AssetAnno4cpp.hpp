#ifndef ASSETANNO4CPP_HPP
#define ASSETANNO4CPP_HPP 1

#include "Asset.hpp"
#include "PersistenceService.hpp"
#include "URLStream.hpp"
#include "Logging.hpp"

namespace mico {
  namespace persistence {
    namespace model {

        class AssetAnno4cpp: public Asset
        {
        private:
          PersistenceService& m_persistenceService;
          jnipp::GlobalRef<jnipp::eu::mico::platform::anno4j::model::AssetMMM> m_assetMMM;

        public:
          AssetAnno4cpp(jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::AssetMMM> assetMMM, PersistenceService& persistenceService)
            : m_persistenceService(persistenceService),
              m_assetMMM(assetMMM)
          {}

          mico::rdf::model::URI getLocation() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<jnipp::org::openrdf::model::impl::URIImpl> juri =
                    jnipp::org::openrdf::model::impl::URIImpl::construct( m_assetMMM->getLocation() );

            return mico::rdf::model::URI( juri->stringValue()->std_str() );
          }

          std::string getFormat() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);
            return m_assetMMM->getFormat()->std_str();
          }

          void setFormat(std::string format) {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);
            jnipp::LocalRef<JavaLangString> jformat = JavaLangString::create(format);
            m_assetMMM->setFormat(jformat);
          }

          std::ostream* getOutputStream() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);
            LOG_DEBUG("new output stream connection to %s.bin", this->getLocation().stringValue().c_str());
            return new mico::io::url_ostream( this->getLocation().stringValue() + ".bin");
          }

          std::istream* getInputStream() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);
            LOG_DEBUG("new input stream connection to %s.bin", this->getLocation().stringValue().c_str());
            return new mico::io::url_istream( this->getLocation().stringValue() + ".bin");
          }
        };
    }
  }
}
#endif
