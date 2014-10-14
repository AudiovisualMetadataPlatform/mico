#include <iostream>
#include <sstream>
#include <fstream>
#include "gtest.h"
#include "rdf_model.hpp"
#include "rdf_query.hpp"
#include "sparql_client.hpp"
#include "http_client.hpp"

using namespace mico::rdf::query;
using namespace mico::rdf::model;
using namespace mico::http;

extern std::string mico_host;
extern std::string mico_user;
extern std::string mico_pass;

class SPARQLClientTest : public ::testing::Test {

protected:
  std::string base_url = "http://" + mico_host + ":8080/marmotta";

  HTTPClient   httpClient;
  SPARQLClient* sparqlClient;

  virtual void SetUp() {
    sparqlClient = new SPARQLClient(base_url + "/sparql");

    // pre-load data using http client and import endpoint
    std::ifstream t("../../java/persistence/src/test/resources/demo-data.foaf");
    std::stringstream buffer;
    buffer << t.rdbuf();

    Request req(POST,base_url+"/import/upload?context="+base_url+"/cpp_sparql_client_test");
    req.setBody(buffer.str(),"application/rdf+xml");
  
    Response* resp = httpClient.execute(req);
    delete resp;
  }

  virtual void TearDown() {
    // delete pre-loaded data
    Request req(DELETE,base_url+"/context?graph="+base_url+"/cpp_sparql_client_test");
    
    Response* resp = httpClient.execute(req);
    delete resp;
  }
};



TEST_F(SPARQLClientTest,Select) {
  const TupleResult *r = sparqlClient->query("SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 10");
  
  ASSERT_TRUE(r->size() > 0);
}


TEST_F(SPARQLClientTest,Ask) {
  bool r = sparqlClient->query("ASK {?s ?p ?o}");
  
  ASSERT_TRUE(r);
}
