/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "gtest.h"

#include <iostream>
#include <sstream>
#include <fstream>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>

#include "http_client.hpp"
#include "Metadata.hpp"
#include "rdf_model.hpp"
#include "rdf_query.hpp"

using namespace boost::uuids;
using namespace mico::persistence;
using namespace mico::http;
using namespace mico::rdf::query;
using namespace mico::rdf::model;

extern std::string mico_host;
extern std::string mico_user;
extern std::string mico_pass;

class MetadataTest : public ::testing::Test {

protected:
  std::string base_url = "http://" + mico_host +":8080/marmotta";
  uuid base_ctx;
  random_generator gen;
  HTTPClient client;

  virtual void SetUp() {
    base_ctx = gen();

    // pre-load data using http client and import endpoint
    std::ifstream t("../../java/persistence/src/test/resources/demo-data.foaf");
    std::stringstream buffer;
    buffer << t.rdbuf();

    Request req(POST,base_url+"/import/upload?context="+base_url+"/"+boost::uuids::to_string(base_ctx));
    req.setBody(buffer.str(),"application/rdf+xml");
  
    Response* resp = client.execute(req);
    delete resp;
  }

  virtual void TearDown() {
    // delete pre-loaded data
    Request req(DELETE,base_url+"/context?graph="+base_url+"/"+boost::uuids::to_string(base_ctx));
    
    Response* resp = client.execute(req);
    delete resp;
  }
};


TEST_F(MetadataTest,Select) {
  Metadata m(base_url, boost::uuids::to_string(base_ctx));

  const TupleResult *r = m.query("SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 10");
  
  ASSERT_TRUE(r->size() > 0);
}

TEST_F(MetadataTest,Ask) {
  Metadata m(base_url, boost::uuids::to_string(base_ctx));

  ASSERT_TRUE(m.ask("ASK { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.software_engineering> }"));
  ASSERT_FALSE(m.ask("ASK { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.design> }"));
}

TEST_F(MetadataTest,Update) {
  Metadata m(base_url, boost::uuids::to_string(base_ctx));

  EXPECT_FALSE(m.ask("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }"));

  m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
  
  ASSERT_TRUE(m.ask("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }"));
}


TEST_F(MetadataTest,Load) {
  Metadata m(base_url, boost::uuids::to_string(base_ctx));

  std::ifstream is("../../java/persistence/src/test/resources/version-base.rdf");


  EXPECT_FALSE(m.ask("ASK { <http://marmotta.apache.org/testing/ns1/R1> <http://marmotta.apache.org/testing/ns1/P1> \"property 1 value 1\" }"));

  m.load(is, "application/rdf+xml");
  
  EXPECT_TRUE(m.ask("ASK { <http://marmotta.apache.org/testing/ns1/R1> <http://marmotta.apache.org/testing/ns1/P1> \"property 1 value 1\" }"));
}


TEST_F(MetadataTest,Dump) {
  Metadata m(base_url, boost::uuids::to_string(base_ctx));

  std::stringstream buffer;
  
  m.dump(buffer, "text/turtle");
  
  ASSERT_TRUE(buffer.str() != "");
}
