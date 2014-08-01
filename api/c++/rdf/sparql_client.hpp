#ifndef HAVE_SPARQL_CLIENT_H
#define HAVE_SPARQL_CLIENT_H 1

#include <string>

#include "http_client.hpp"
#include "rdf_model.hpp"
#include "rdf_query.hpp"


namespace mico {
  namespace rdf {
    namespace query {


      // thrown when a SPARQL query fails
      class QueryFailedException {
      private:
	long status;
	std::string message;

      public:
	QueryFailedException(long status, std::string message) : status(status), message(message) {};

	long getStatus() const { return status; };

	const std::string& getMessage() const { return message; };
      };
      
      /**
       * A class allowing to run SPARQL requests against a certain endpoint.
       */
      class SPARQLClient {

      private:
	std::string base_url;
	
	mico::http::HTTPClient  http_client;
	
      public:

	SPARQLClient(std::string base_url) : base_url(base_url) {};

	
	/**
	 * Execute a SPARQL 1.1 ask query against the Marmotta server.
	 */
	const bool ask(std::string sparqlAsk);

	/**
	 * Execute a SPARQL 1.1 tuple query against the Marmotta server.
	 */
	const TupleResult* query(std::string sparqlSelect);
	

	/**
	 * Execute a SPARQL 1.1 Update against the Marmotta server
	 */
	const void update(std::string sparqlUpdate);


      };


    }
  }
}

#endif
