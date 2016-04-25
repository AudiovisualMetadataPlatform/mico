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
//#include "gtest.h"

#include <iostream>
#include <sstream>
#include <fstream>
#include <map>
#include <memory>
#include <boost/algorithm/string.hpp>

#include "http_client.hpp"

#include "PersistenceService.hpp"
#include "Item.hpp"
#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"

using namespace std;
using namespace mico::persistence;
using namespace mico::http;
using namespace mico::util;

extern std::string mico_host;
extern std::string mico_user;
extern std::string mico_pass;


class PersistenceServiceTest /*: public ::testing::Test */{

//protected:
public:
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
    //ASSERT_TRUE(svc->getMetadata().ask(query));
  }
  void assertAskMN(std::string query) {
    //ASSERT_FALSE(svc->getMetadata().ask(query));
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

//TEST_F(PersistenceServiceTest,Init) {
//  PersistenceMetadata& m = svc->getMetadata();
  
//  //m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
//  //assertAskM("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
//}

//TEST_F(PersistenceServiceTest,CreateItem) {
//  std::shared_ptr<Item> newItem = svc->createItem();

////  //m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
////  //assertAskM("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }");
//}


//TEST_F(PersistenceServiceTest,CreateDeleteContentItem) {
  
//  ASSERT_EQ(svc->begin(), svc->end());

//  Item* item = svc->createItem();

//  ASSERT_NE(svc->begin(), svc->end());

//  map<string,string> params;
//  params["g"] = base_url;
//  params["ci"] = item->getURI().stringValue();

//  assertAskM(sparql_format_query("ASK { GRAPH <$(g)> { <$(g)> <http://www.w3.org/ns/ldp#contains> <$(ci)> } }",params));

//  svc->deleteContentItem(item->getURI());

//  ASSERT_EQ(svc->begin(), svc->end());
//  assertAskMN(sparql_format_query("ASK { GRAPH <$(g)> { <$(g)> <http://www.w3.org/ns/ldp#contains> <$(ci)> } }",params));

//  delete item;
//}


//TEST_F(PersistenceServiceTest,ListContentItems) {
  
//  ASSERT_EQ(svc->begin(), svc->end());


//  ContentItem* items[5];
//  for(int i=0; i<5; i++) {
//    items[i] = svc->createContentItem();
//  }

//  ASSERT_NE(svc->begin(), svc->end());

//  for(ContentItem* item : *svc) {
//    ASSERT_TRUE(ptr_contains(item,items,5));
//  }


//}

std::string mico_host;
std::string mico_user;
std::string mico_pass;

int main(int argc, char **argv) {
    if(argc == 4) {
        mico_host = argv[1];
        mico_user = argv[2];
        mico_pass = argv[3];


        PersistenceServiceTest persistenceServiceTest;

        persistenceServiceTest.SetUp();

        jnipp::Env::Scope scope(PersistenceService::m_sJvm);

        size_t numTestItems = 10;

        std::vector<std::string> itemURIS;
        itemURIS.reserve(numTestItems);


        for (size_t i=0; i < numTestItems; ++i) {

            // create check

            std::shared_ptr<mico::persistence::model::Item> currItem = persistenceServiceTest.svc->createItem();
            std::shared_ptr<mico::persistence::model::Resource> currItemResource =
                std::dynamic_pointer_cast<mico::persistence::model::Resource>(currItem);
            assert(currItem != 0);

            mico::rdf::model::URI uri = currItemResource->getURI();

            itemURIS.push_back(uri.stringValue());

            auto itemMMM = currItemResource->getRDFObject();
            assert( (jobject)itemMMM != nullptr );

            auto stime = currItem->getSerializedAt();
            assert( stime.size() );


            // set/get type check
            std::stringstream ss_sem_type, ss_syn_type;
            ss_sem_type << "semantic_type_item_" << i;
            ss_syn_type << "syntactic_type_item_" << i;

            currItemResource->setSemanticType(ss_sem_type.str().c_str());
            auto semanticType = currItemResource->getSemanticType();
            assert( semanticType.compare(ss_sem_type.str().c_str()) == 0 );

            currItemResource->setSyntacticalType(ss_sem_type.str().c_str());
            auto synType = currItemResource->getSyntacticalType();
            assert( synType.compare(ss_sem_type.str().c_str()) == 0 );
        }


        // check item retrieval
        for (auto itemURI : itemURIS) {
            mico::rdf::model::URI asURI(itemURI);

            std::shared_ptr<mico::persistence::model::Item> retrievedItem  =
                        persistenceServiceTest.svc->getItem(asURI);


            std::shared_ptr<mico::persistence::model::Resource> retrievedItemResource =
                std::dynamic_pointer_cast<mico::persistence::model::Resource>(retrievedItem);

            assert(retrievedItemResource);

            auto uri = retrievedItemResource->getURI();

            assert(retrievedItemResource->getURI().stringValue() == itemURI);
        }

        // check item retrieval at once
        std::vector< std::shared_ptr<model::Item> > resultItems = persistenceServiceTest.svc->getItems();

        assert(resultItems.size() == itemURIS.size());

        for (auto item : resultItems) {
          std::string sItemURI = std::dynamic_pointer_cast<mico::persistence::model::Resource>(item)->getURI().stringValue();
          assert(std::find(itemURIS.begin(),itemURIS.end(), sItemURI) != itemURIS.end());
        }

        // check non existing item retrieval
        std::shared_ptr<mico::persistence::model::Item> notExistingItem  =
            persistenceServiceTest.svc->getItem(mico::rdf::model::URI("http://does_not_exist_at_all"));

        assert(!notExistingItem);

        //part creation
        for (auto itemURI : itemURIS) {
            mico::rdf::model::URI asURI(itemURI);

            std::shared_ptr<mico::persistence::model::Item> retrievedItem  =
                        persistenceServiceTest.svc->getItem(asURI);

            assert(retrievedItem);

            retrievedItem->createPart(mico::rdf::model::URI("http://my_test_extractor"));
        }

        // check item deletion
        for (auto itemURI : itemURIS) {
           persistenceServiceTest.svc->deleteItem(itemURI);
        }






        //std::shared_ptr<Asset> asset = currItem->getAsset();
        //std::shared_ptr<Part> part2 = currItem->getPart(uri);
        //std::list< std::shared_ptr<Part> > parts = currItem->getParts();


    } else {
        std::cerr << "usage: <testcmd> <host> <user> <password>" << std::endl;
    }

}
