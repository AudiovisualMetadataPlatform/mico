#include "gtest.h"

#include <iostream>
#include <sstream>
#include <fstream>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/algorithm/string.hpp>

#include "http_client.hpp"

#include "../../main/c++/ContentItem.hpp"

using namespace boost::uuids;
using namespace mico::persistence;
using namespace mico::http;

class ContentItemTest : public ::testing::Test {

protected:
  std::string base_url = "http://localhost:8080/marmotta";
  uuid base_ctx;
  random_generator rnd_gen;
  HTTPClient client;
  ContentItem* item;

  virtual void SetUp() {
    base_ctx = rnd_gen();
    item = new ContentItem(base_url, base_ctx);
  }


  virtual void TearDown() {
    // delete pre-loaded data
    Request req(DELETE,base_url+"/context?graph="+base_url+"/"+boost::uuids::to_string(base_ctx));
    
    Response* resp = client.execute(req);
    delete resp;

    delete item;
  }

  void assertAskM(std::string query) {
    ASSERT_TRUE(item->getMetadata().ask(query));
  }

  void assertAskE(std::string query) {
    ASSERT_TRUE(item->getExecution().ask(query));
  }

  void assertAskR(std::string query) {
    ASSERT_TRUE(item->getResult().ask(query));
  }
};


TEST_F(ContentItemTest,ContentItemMetadata) {
  ContentItemMetadata& m = item->getMetadata();
  
  m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
  assertAskM("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}

TEST_F(ContentItemTest,ExecutionMetadata) {
  ExecutionMetadata& m = item->getExecution();
  
  m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
  assertAskE("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}

TEST_F(ContentItemTest,ResultMetadata) {
  ResultMetadata& m = item->getResult();
  
  m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
  assertAskR("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}


TEST_F(ContentItemTest,CreateDeleteContentPart) {
  
  ASSERT_EQ(item->begin(), item->end());

  Content* part = item->createContentPart();


}
