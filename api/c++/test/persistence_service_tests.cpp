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
#include <map>
#include <boost/algorithm/string.hpp>

#include "http_client.hpp"

#include "PersistenceService.hpp"
#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"

using namespace std;
using namespace mico::persistence;
using namespace mico::http;
using namespace mico::util;

extern std::string mico_host;
extern std::string mico_user;
extern std::string mico_pass;


class PersistenceServiceTest : public ::testing::Test {

protected:
  std::string base_url = "http://" + mico_host + ":8080/marmotta";
  std::string content_dir = "hdfs://" + mico_host;
  HTTPClient client;
  PersistenceService* svc;

  virtual void SetUp() {
    svc = new PersistenceService(base_url, content_dir);
  }


  virtual void TearDown() {
    // delete pre-loaded data
    Request req(DELETE,base_url+"/context?graph="+base_url);
    
    Response* resp = client.execute(req);
    delete resp;

    delete svc;
  }

  void assertAskM(std::string query) {
    ASSERT_TRUE(svc->getMetadata().ask(query));
  }
  void assertAskMN(std::string query) {
    ASSERT_FALSE(svc->getMetadata().ask(query));
  }


};

template<typename T> 
bool ptr_contains(T* ptr, T** arr, int len) {
  for(int i=0; i<len; i++) {
    if(*ptr == *arr[i]) {
      return true;
    }
  }
  return false;
}

TEST_F(PersistenceServiceTest,Metadata) {
  PersistenceMetadata& m = svc->getMetadata();
  
  m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
  assertAskM("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
}


TEST_F(PersistenceServiceTest,CreateDeleteContentItem) {
  
  ASSERT_EQ(svc->begin(), svc->end());

  Item* item = svc->createItem();

  ASSERT_NE(svc->begin(), svc->end());

//  map<string,string> params;
//  params["g"] = base_url;
//  params["ci"] = item->getURI().stringValue();

//  assertAskM(sparql_format_query("ASK { GRAPH <$(g)> { <$(g)> <http://www.w3.org/ns/ldp#contains> <$(ci)> } }",params));

//  svc->deleteContentItem(item->getURI());

//  ASSERT_EQ(svc->begin(), svc->end());
//  assertAskMN(sparql_format_query("ASK { GRAPH <$(g)> { <$(g)> <http://www.w3.org/ns/ldp#contains> <$(ci)> } }",params));

  delete item;
}


TEST_F(PersistenceServiceTest,ListContentItems) {
  
//  ASSERT_EQ(svc->begin(), svc->end());


//  ContentItem* items[5];
//  for(int i=0; i<5; i++) {
//    items[i] = svc->createContentItem();
//  }

//  ASSERT_NE(svc->begin(), svc->end());

//  for(ContentItem* item : *svc) {
//    ASSERT_TRUE(ptr_contains(item,items,5));
//  }


}
