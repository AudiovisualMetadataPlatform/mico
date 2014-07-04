#include "sparql_client.hpp"

using namespace mico::http;

namespace mico {
  namespace rdf {
    namespace query {

      
      /**
       * Execute a SPARQL 1.1 ask query against the Marmotta server.
       */
      const bool SPARQLClient::ask(std::string sparqlAsk) {
	Request req(POST,base_url+"/select");
	req.setHeader("Accept","application/sparql-results+xml");
	req.setBody(sparqlAsk, "application/sparql-query");

	Response *resp = http_client.execute(req);
	
	if(resp->getStatus() == 200 && resp->getBody() != NULL) {
	  BooleanResult *r = new BooleanResult();
	  r->loadFrom(resp->getBody()->getContent(), resp->getBody()->getContentLength());
	  return (bool)*r;
	} else {
	  throw QueryFailedException(resp->getStatus(), "HTTP response without body");
	}
	

	return false;
      }

      /**
       * Execute a SPARQL 1.1 tuple query against the Marmotta server.
       */
      const TupleResult* SPARQLClient::query(std::string sparqlSelect) {
	Request req(POST,base_url+"/select");
	req.setHeader("Accept","application/sparql-results+xml");
	req.setBody(sparqlSelect, "application/sparql-query");

	Response *resp = http_client.execute(req);
	
	if(resp->getStatus() == 200 && resp->getBody() != NULL) {
	  TupleResult *r = new TupleResult();
	  r->loadFrom(resp->getBody()->getContent(), resp->getBody()->getContentLength());
	  return r;
	} else {
	  throw QueryFailedException(resp->getStatus(), "HTTP response without body");
	}
      }
	

      /**
       * Execute a SPARQL 1.1 Update against the Marmotta server
       */
      const void SPARQLClient::update(std::string sparqlUpdate) {
	Request req(POST,base_url+"/update");
	req.setBody(sparqlUpdate, "application/sparql-query");

	Response *resp = http_client.execute(req);
	
	if(resp->getStatus() != 200) {
	  throw QueryFailedException(resp->getStatus(), "SPARQL update failed");
	}
      }

    }
  }
}
