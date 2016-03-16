#ifndef PARTANNO4CPP_HPP
#define PARTANNO4CPP_HPP 1

#include "Part.hpp"
#include "AssetAnno4cpp.hpp"

namespace mico {
  namespace persistence {

    class PartAnno4cpp: public Part
    {

    private:
      const PersistenceService& m_persistenceService;
      const Item& m_item;
      jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> m_partMMM;

    public:
      PartAnno4cpp(jnipp::LocalRef<EuMicoPlatformAnno4jModelPartMMM> partMMM, const Item& item, const PersistenceService& persistenceService)
        : m_persistenceService(persistenceService),
          m_item(item),
          m_partMMM(partMMM)
      {}

      const Item& getItem() {
        return m_item;
      }

      mico::rdf::model::URI getURI() {
        //return new URIImpl(m_partMMM.getResourceAsString());
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

      //std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > getTargets() {
      //  throw std::runtime_error("PartAnno4cpp::getTargets(): Not yet implemented!");
      //}

      void setTargets(std::list< jnipp::LocalRef<ComGithubAnno4jModelTarget> > targets) {
        throw std::runtime_error("PartAnno4cpp::setTargets(): Not yet implemented!");
      }

      void addTarget(const jnipp::LocalRef<ComGithubAnno4jModelTarget> &target) {
        m_partMMM->addTarget(target);
      }

      //std::list<Resource> getInputs();

      void setInputs(std::list<Resource> inputs);

      void addInput(Resource& input) {
        m_partMMM->addInput( input.getRDFObject() );
      }

       std::string getSerializedAt() {
        return static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(m_partMMM)->getSerializedAt()->std_str();
      }

      jnipp::LocalRef<ComGithubAnno4jModelAgent> getSerializedBy() {
        return static_cast< jnipp::LocalRef<ComGithubAnno4jModelAnnotation> >(m_partMMM)->getSerializedBy();
      }

      Asset* getAsset();

      bool hasAsset() {
        jnipp::LocalRef<EuMicoPlatformAnno4jModelAssetMMM> asset = static_cast< jnipp::LocalRef<EuMicoPlatformAnno4jModelResourceMMM> >(m_partMMM)->getAsset();
        throw std::runtime_error("PartAnno4cpp::hasAsset(): Not yet implemented!");
      }
    };
  }
}
#endif
