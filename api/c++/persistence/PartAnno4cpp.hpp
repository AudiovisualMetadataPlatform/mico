#ifndef PARTANNO4CPP_HPP
#define PARTANNO4CPP_HPP 1

#include "Part.hpp"
#include "AssetAnno4cpp.hpp"

namespace mico {
  namespace persistence {
    namespace model {
      class PartAnno4cpp: public Part
      {

      private:
        PersistenceService& m_persistenceService;
        std::shared_ptr<Item> m_item;
        jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> m_partMMM;

      public:
        PartAnno4cpp(jnipp::Ref<EuMicoPlatformAnno4jModelPartMMM> partMMM, std::shared_ptr<Item> item, PersistenceService& persistenceService)
          : m_persistenceService(persistenceService),
            m_item(item),
            m_partMMM(partMMM)
        {}

        std::shared_ptr<Item> getItem() {
          return m_item;
        }

        mico::rdf::model::URI getURI() {
          jnipp::LocalRef<OrgOpenrdfModelImplURIImpl> juri = OrgOpenrdfModelImplURIImpl::construct( static_cast< jnipp::LocalRef<ComGithubAnno4jModelImplResourceObject> >(m_partMMM)->getResourceAsString() );
          return mico::rdf::model::URI( juri->stringValue()->std_str() );
        }

        jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> getRDFObject() {
          return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM);
        }

        std::string getSyntacticalType() {
          return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getSyntacticalType()->std_str();
        }

        void setSyntacticalType(std::string syntacticalType) {
          jnipp::LocalRef<JavaLangString> jsyntacticalType = JavaLangString::create(syntacticalType);
          static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->setSyntacticalType(jsyntacticalType);
        }

        std::string getSemanticType() {
          return static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getSemanticType()->std_str();
        }

        void setSemanticType(std::string semanticType) {
          jnipp::LocalRef<JavaLangString> jsemanticType = JavaLangString::create(semanticType);
          static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->setSemanticType(jsemanticType);
        }

        jnipp::LocalRef<ComGithubAnno4jModelBody> getBody() {
          return m_partMMM->getBody();
        }

        void setBody(const jnipp::LocalRef<ComGithubAnno4jModelBody> &body) {
          m_partMMM->setBody(body);
        }

        std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > getTargets();

        void setTargets(std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > targets);

        void addTarget(const jnipp::LocalRef<ComGithubAnno4jModelTarget> &target) {
          m_partMMM->addTarget(target);
        }

        std::list< std::shared_ptr<Resource> > getInputs();

        void setInputs(std::list<std::shared_ptr<Resource> > inputs);

        void addInput(Resource& input) {
          m_partMMM->addInput( input.getRDFObject() );
        }

         std::string getSerializedAt() {
          return static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(m_partMMM)->getSerializedAt()->std_str();
        }

        jnipp::LocalRef<ComGithubAnno4jModelAgent> getSerializedBy() {
          return static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(m_partMMM)->getSerializedBy();
        }

        std::shared_ptr<Asset> getAsset();

        bool hasAsset() {
          jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getAsset();
          return (jobject)asset != nullptr;
        }
      };
    }
  }
}
#endif
