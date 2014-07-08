#ifndef HAVE_METADATA_H
#define HAVE_METADATA_H 1

#include <string>
#include <iostream>
#include <sstream>

#include "../../../../rdf/src/main/c++/rdf_model.hpp"
#include "../../../../rdf/src/main/c++/rdf_query.hpp"
#include "../../../../rdf/src/main/c++/sparql_client.hpp"
#include "../../../../rdf/src/main/c++/http_client.hpp"


namespace mico {
  namespace persistence {

    using std::string;
    using namespace mico::rdf::model;
    using namespace mico::rdf::query;
    using namespace mico::http;

    /**
     * A class offering access to RDF metadata through SPARQL. The Metadata class implements a
     * wrapper around a SPARQL endpoint and can be used for any kind of RDF metadata
     * representation. Together with a contextual Marmotta server, it is also context aware,
     * i.e. when the ID of a named graph has been configured, all operations will only be executed
     * on this graph.
     */
    class Metadata {

    protected:
      string baseUrl;     //!< the base URL of the server
      string contextUrl;  //!< the URI of the context to use as named graph

      SPARQLClient sparqlClient; //!< an instance of a SPARQL client, will be initialised to baseUrl + "/sparql"
      HTTPClient   httpClient;   //!< an instance of an HTTP client, will be used for load/dump
				 //!< using the baseUrl and the Marmotta import/export endpoints

    public:

      /**
       * Create a new metadata object for the given server using the global SPARQL
       * endpoint. Optional context must be given explicitly in SPARQL queries.
       */
      Metadata(string baseUrl) : baseUrl(baseUrl), contextUrl(baseUrl), sparqlClient(baseUrl + "/sparql") {};

      /**
       * Create a new metadata object for the given server base URL and context using the contextual
       * SPARQL endpoint. All queries and updates will exclusively access this context.
       */
      Metadata(string baseUrl, string context) 
	: baseUrl(baseUrl), contextUrl(baseUrl + "/" + context), sparqlClient(baseUrl + "/" + context + "/sparql") {};


      virtual ~Metadata() {};

      /**
       * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
       *
       * @param in      InputStream to load the data from
       * @param format  data format the RDF data is using (e.g. text/turtle or application/rdf+xml;
       *                defined shortcuts: "turtle" and "rdfxml")
       */
      void load(std::istream& in, const string format);


      /**
       * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
       * format. Can be used for exporting the metadata.
       *
       * @param out    OutputStream to export the data to
       * @param format data format the RDF data is using (e.g. (e.g. text/turtle or application/rdf+xml;
       *                defined shortcuts: "turtle" and "rdfxml")
       */
      void dump(std::ostream& out, const string format);



      /**
       * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
       * used for any kind of modification of the data.
       *
       * @param sparqlUpdate
       */
      void update(const string sparqlUpdate);


      /**
       * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
       * any kind of data access.
       *
       * @param sparqlQuery
       * @return
       */
      const TupleResult* query(const string sparqlQuery);



      /**
       * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
       * any kind of data access.
       *
       * @param sparqlQuery
       * @return
       */
      const bool ask(const string sparqlQuery);


      /**
       * Close the metadata connection and clean up any open resources.
       */
      void close() {};

    };
  }
}

#endif
