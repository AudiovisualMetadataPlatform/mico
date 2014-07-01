#include "rdf_model.hpp"

namespace org {
  namespace openrdf {
    namespace model {


	URI(const string& uri);
	URI(const char* uri);
	~URI();
	
	/**
	 * Gets the local name of this URI.
	 */
	string getLocalName() const;

	/**
	 * Gets the namespace of this URI.
	 */
	string getNamespace() const;	


	const string& stringValue() const;



    }
  }
}
