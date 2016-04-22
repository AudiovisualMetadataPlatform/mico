
#include "ResourceAnno4cpp.hpp"

#include <jnipp.h>
#include <anno4cpp.h>
#include "JnippExcpetionHandling.hpp"
#include "Logging.hpp"


namespace mico {
  namespace persistence {
    mico::rdf::model::URI ResourceAnno4cpp::getURI() {
      LOG_DEBUG("ResourceAnno4cpp::getURI()");
      jnipp::Env::Scope scope(PersistenceService::m_sJvm);


      jnipp::LocalRef<OrgOpenrdfModelURI> jResourceURI =
          ((jnipp::Ref<OrgOpenrdfRepositoryObjectRDFObject>)m_resourceMMM)->getResource();

      LOG_DEBUG("ResourceAnno4cpp:: URI is [%s]", jResourceURI->toString()->std_str().c_str());

      checkJavaExcpetionNoThrow(m_jnippErrorMessage);
      assert((jobject)jResourceURI);

      return mico::rdf::model::URI(jResourceURI->toString()->std_str());
    }
  }
}
