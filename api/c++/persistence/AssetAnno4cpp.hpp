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

          std::string m_jnippErrorMessage;

        public:
          AssetAnno4cpp(jnipp::Ref<jnipp::eu::mico::platform::anno4j::model::AssetMMM> assetMMM, PersistenceService& persistenceService)
            : m_persistenceService(persistenceService),
              m_assetMMM(assetMMM)
          {}

          mico::persistence::model::URI getLocation() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<jnipp::org::openrdf::model::impl::URIImpl> juri =
                    jnipp::org::openrdf::model::impl::URIImpl::construct( m_assetMMM->getLocation() );

            return mico::persistence::model::URI( juri->stringValue()->std_str() );
          }

          mico::persistence::model::URI getURI() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);

            jnipp::LocalRef<jnipp::org::openrdf::model::URI> jAssetURI =
                ((jnipp::Ref<jnipp::org::openrdf::repository::object::RDFObject>)m_assetMMM)->getResource();

            m_persistenceService.checkJavaExceptionNoThrow(m_jnippErrorMessage);
            assert((jobject)jAssetURI);

            return mico::persistence::model::URI(jAssetURI->toString()->std_str());
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
            return new mico::io::url_ostream( m_persistenceService.unmaskContentLocation(this->getLocation().stringValue()) + ".bin");
          }

          std::istream* getInputStream() {
            jnipp::Env::Scope scope(PersistenceService::m_sJvm);
            LOG_DEBUG("new input stream connection to %s.bin", this->getLocation().stringValue().c_str());
            return new mico::io::url_istream( m_persistenceService.unmaskContentLocation(this->getLocation().stringValue()) + ".bin");
          }
        };
    }
  }
}
#endif
