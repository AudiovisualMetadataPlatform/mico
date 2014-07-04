#include <iostream>
#include <sstream>
#include "gtest.h"
#include "../../main/c++/rdf_model.hpp"
#include "../../main/c++/rdf_query.hpp"
#include "../../main/c++/sparql_client.hpp"


using namespace mico::rdf::query;
using namespace mico::rdf::model;

TEST(SPARQLClient,TestSelect) {
  SPARQLClient c("http://localhost:8080/sparql");

  const TupleResult *r = c.query("SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 10");
  
  ASSERT_TRUE(r->size() > 0);
}


TEST(SPARQLClient,TestAsk) {
  SPARQLClient c("http://localhost:8080/sparql");

  bool r = c.query("ASK {?s ?p ?o}");
  
  ASSERT_TRUE(r);
}
