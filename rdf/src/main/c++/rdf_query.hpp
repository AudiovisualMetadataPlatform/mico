#ifndef HAVE_RDF_QUERY_H
#define HAVE_RDF_QUERY_H 1

#include <iostream>
#include <map>
#include <vector>
#include <string>

#include "rdf_model.hpp"

using namespace std;
using namespace org::openrdf::model;

namespace org {
  namespace openrdf {
    namespace query {

      // define a BindingSet simply as a map from binding name to value
      class BindingSet : public map<string,Value*> {
	
      };

      static void startElement(void *data, const char *el, const char **attr);


      // define a QueryResult simply as list (vector) of BindingSets
      class QueryResult : public vector<BindingSet> {

      private:
	friend istream& operator>>(istream& is, QueryResult& r);
	friend ostream& operator<<(ostream& is, QueryResult& r);
	friend void startElement(void *data, const char *el, const char **attr);

	vector<string> bindingNames;

      public:
	
	const vector<string>& getBindingNames() const { return bindingNames; };

      };


      /**
       * Load query result data represented in the XML SPARQL protocol syntax into the query result
       * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
       */
      istream& operator>>(istream& is, QueryResult& r);


      /**
       * Serialize query result data from the given argument into XML SPARQL protocol syntax.
       */
      ostream& operator<<(ostream& is, QueryResult& r);


    }
  }
}


#endif
