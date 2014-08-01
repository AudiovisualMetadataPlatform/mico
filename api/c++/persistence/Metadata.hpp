#ifndef HAVE_METADATA_H
#define HAVE_METADATA_H 1

#include <string>
#include <iostream>

#include "http_client.hpp"
#include "sparql_client.hpp"

namespace mico {
  // forward declarations
  namespace rdf {
    namespace query {
      class TupleResult;
    }
  }

  namespace persistence {


    /**
     * A class offering access to RDF metadata through SPARQL. The Metadata class implements a
     * wrapper around a SPARQL endpoint and can be used for any kind of RDF metadata
     * representation. Together with a contextual Marmotta server, it is also context aware,
     * i.e. when the ID of a named graph has been configured, all operations will only be executed
     * on this graph.
     */
    class Metadata {

    protected:
      std::string baseUrl;     //!< the base URL of the server
      std::string contextUrl;  //!< the URI of the context to use as named graph

      mico::rdf::query::
      SPARQLClient sparqlClient; //!< an instance of a SPARQL client, will be initialised to baseUrl + "/sparql"

      mico::http::
      HTTPClient   httpClient;   //!< an instance of an HTTP client, will be used for load/dump
				 //!< using the baseUrl and the Marmotta import/export endpoints

    public:

      /**
       * Create a new metadata object for the given server using the global SPARQL
       * endpoint. Optional context must be given explicitly in SPARQL queries.
       */
      Metadata(std::string baseUrl);

      /**
       * Create a new metadata object for the given server base URL and context using the contextual
       * SPARQL endpoint. All queries and updates will exclusively access this context.
       */
      Metadata(std::string baseUrl, std::string context);


      /**
       * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
       *
       * @param in      InputStream to load the data from
       * @param format  data format the RDF data is using (e.g. text/turtle or application/rdf+xml;
       *                defined shortcuts: "turtle" and "rdfxml")
       */
      void load(std::istream& in, const std::string format);


      /**
       * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
       * format. Can be used for exporting the metadata.
       *
       * @param out    OutputStream to export the data to
       * @param format data format the RDF data is using (e.g. (e.g. text/turtle or application/rdf+xml;
       *                defined shortcuts: "turtle" and "rdfxml")
       */
      void dump(std::ostream& out, const std::string format);



      /**
       * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
       * used for any kind of modification of the data.
       *
       * @param sparqlUpdate
       */
      void update(const std::string sparqlUpdate);


      /**
       * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
       * any kind of data access.
       *
       * @param sparqlQuery
       * @return
       */
      const mico::rdf::query::TupleResult* query(const std::string sparqlQuery);



      /**
       * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
       * any kind of data access.
       *
       * @param sparqlQuery
       * @return
       */
      const bool ask(const std::string sparqlQuery);


      /**
       * Close the metadata connection and clean up any open resources.
       */
      void close() {};

    };
  }
}

#endif
