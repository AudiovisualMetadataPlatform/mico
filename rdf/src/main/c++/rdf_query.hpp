#ifndef HAVE_RDF_QUERY_H
#define HAVE_RDF_QUERY_H 1

#include <iostream>
#include <map>
#include <vector>
#include <string>

#include "rdf_model.hpp"

using namespace std;
using namespace mico::rdf::model;

namespace mico {
  namespace rdf {
    namespace query {

      // define a BindingSet simply as a map from binding name to value
      class BindingSet : public map<string,Value*> {
	
      };

      static void startElement(void *data, const char *el, const char **attr);
      static void characterData(void *data, const char *chars, int len);

      class QueryResult {
      public:

	/**
	 * Load query result data represented in the XML SPARQL protocol syntax into the query result
	 * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
	 */
	virtual void loadFrom(istream& is) = 0;


	/**
	 * Load query result data represented in the XML SPARQL protocol syntax into the query result
	 * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
	 */
	virtual void loadFrom(const char* ptr, size_t len) = 0;
	
      };


      class BooleanResult : public QueryResult {

	friend void characterData(void *data, const char *chars, int len);

      private:
	bool value;

      public:
	BooleanResult() : value(false) {};
	BooleanResult(bool value) : value(value) {};

	/**
	 * Load query result data represented in the XML SPARQL protocol syntax into the query result
	 * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
	 */
	void loadFrom(istream& is);


	/**
	 * Load query result data represented in the XML SPARQL protocol syntax into the query result
	 * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
	 */
	void loadFrom(const char* ptr, size_t len);


	// can be used as boolean value in boolean expressions...
	inline operator bool() const { return value; };
	
      };



      // define a QueryResult simply as list (vector) of BindingSets
      class TupleResult : public vector<BindingSet>, public QueryResult {

      private:
	friend istream& operator>>(istream& is, TupleResult& r);
	friend ostream& operator<<(ostream& is, TupleResult& r);
	friend void startElement(void *data, const char *el, const char **attr);

	vector<string> bindingNames;

      public:
	
	const vector<string>& getBindingNames() const { return bindingNames; };


	/**
	 * Load query result data represented in the XML SPARQL protocol syntax into the query result
	 * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
	 */
	void loadFrom(istream& is);


	/**
	 * Load query result data represented in the XML SPARQL protocol syntax into the query result
	 * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
	 */
	void loadFrom(const char* ptr, size_t len);

      };




      /**
       * Load query result data represented in the XML SPARQL protocol syntax into the query result
       * given as argument. http://www.w3.org/TR/rdf-sparql-XMLres/
       */
      istream& operator>>(istream& is, TupleResult& r);
      istream& operator>>(istream& is, BooleanResult& r);


      /**
       * Serialize query result data from the given argument into XML SPARQL protocol syntax.
       */
      ostream& operator<<(ostream& is, TupleResult& r);
      ostream& operator<<(ostream& is, BooleanResult& r);


    }
  }
}


#endif
