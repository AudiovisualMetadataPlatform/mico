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
#include "Uri.hpp"
#include "Part.hpp"
#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"
#include <anno4cpp.h>
#include <jnipp.h>


using namespace std;
using namespace mico::persistence;
//using namespace mico::http;
//using namespace mico::util;

extern std::string mico_host;
extern std::string mico_user;
extern std::string mico_pass;


class PersistenceServiceTest /*: public ::testing::Test */{

//protected:
public:
  std::string base_url = "http://" + mico_host + ":8080/marmotta";
  std::string content_dir = "hdfs://" + mico_host;
  //HTTPClient client;
  PersistenceService* svc;

  virtual void SetUp() {
    svc = new PersistenceService(base_url, content_dir);
  }


  virtual void TearDown() {
    // delete pre-loaded data
//    Request req(DELETE,base_url+"/context?graph="+base_url);
    
//    Response* resp = client.execute(req);
//    delete resp;

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

        size_t numTestItems = 1;

        std::vector<std::string> itemURIS;
        itemURIS.reserve(numTestItems);

        std::cout << "++++++++++++++++++++++++++++++++++++ TESTING ITEM CREATION ++++++++++++++++++++++++++++++" << std::endl;
        for (size_t i=0; i < numTestItems; ++i) {

            // create check

            std::shared_ptr<mico::persistence::model::Item> currItem = persistenceServiceTest.svc->createItem();
            std::shared_ptr<mico::persistence::model::Resource> currItemResource =
                std::dynamic_pointer_cast<mico::persistence::model::Resource>(currItem);
            assert(currItem != 0);

            mico::persistence::model::URI uri = currItemResource->getURI();

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

            currItemResource->setSyntacticalType(ss_syn_type.str().c_str());
            auto synType = currItemResource->getSyntacticalType();
            assert( synType.compare(ss_syn_type.str().c_str()) == 0 );
        }


        std::cout << "++++++++++++++++++++++++++++++++++++ TESTING ITEM RETRIEVAL ++++++++++++++++++++++++++++++" << std::endl;
        // check item retrieval
        for (auto itemURI : itemURIS) {
            mico::persistence::model::URI asURI(itemURI);

            std::shared_ptr<mico::persistence::model::Item> retrievedItem  =
                        persistenceServiceTest.svc->getItem(asURI);


            std::shared_ptr<mico::persistence::model::Resource> retrievedItemResource =
                std::dynamic_pointer_cast<mico::persistence::model::Resource>(retrievedItem);

            assert(retrievedItemResource);

            auto uri = retrievedItemResource->getURI();

            assert(retrievedItemResource->getURI().stringValue() == itemURI);
        }

        // check item retrieval
        //std::vector< std::shared_ptr<model::Item> > resultItems = persistenceServiceTest.svc->getItems();
        // @TODO comment back in when done
//        assert(resultItems.size() == itemURIS.size());

//        for (auto item : resultItems) {
//          std::string sItemURI = std::dynamic_pointer_cast<mico::persistence::model::Resource>(item)->getURI().stringValue();
//          assert(std::find(itemURIS.begin(),itemURIS.end(), sItemURI) != itemURIS.end());
//        }


        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // +++++++++++++++++++++ Asset creation for item++++++++++++++++++++
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        std::shared_ptr<mico::persistence::model::Resource> itemWithAssetResource =
            std::dynamic_pointer_cast<mico::persistence::model::Resource>(persistenceServiceTest.svc->createItem());

        std::shared_ptr<mico::persistence::model::Asset> newAsset = itemWithAssetResource->getAsset();
        std::shared_ptr<mico::persistence::model::Asset> existingAsset = itemWithAssetResource->getAsset();
        assert(newAsset);
        assert(existingAsset);
        assert(newAsset->getLocation().stringValue().compare(existingAsset->getLocation().stringValue()) == 0);

        newAsset->setFormat("video/mp4");

        assert(existingAsset->getFormat().compare("video/mp4") == 0);

        assert(newAsset->getURI().stringValue().length() > 0);
        assert(existingAsset->getURI().stringValue().length() > 0);

        std::shared_ptr<mico::persistence::model::Asset> existingAsset2 = itemWithAssetResource->getAssetWithLocation(newAsset->getLocation());
        assert(existingAsset2);
        assert(existingAsset2->getURI().stringValue().length() > 0);
        assert(existingAsset2->getFormat().compare("video/mp4") == 0);
        assert(newAsset->getLocation().stringValue().compare(existingAsset2->getLocation().stringValue()) == 0);

        persistenceServiceTest.svc->deleteItem(itemWithAssetResource->getURI());

        //now with custom location
        itemWithAssetResource =
        		std::dynamic_pointer_cast<mico::persistence::model::Resource>(persistenceServiceTest.svc->createItem());
        newAsset = itemWithAssetResource->getAssetWithLocation(mico::persistence::model::URI(""));
        assert(newAsset == nullptr );

        newAsset = itemWithAssetResource->getAssetWithLocation(
        		mico::persistence::model::URI(persistenceServiceTest.svc->getStoragePrefix()+"malf/or_med/url"));
        assert(newAsset == nullptr );

        std::string test_location=persistenceServiceTest.svc->getStoragePrefix()+"new/custom-location";
        newAsset = itemWithAssetResource->getAssetWithLocation(mico::persistence::model::URI(test_location));
        existingAsset = itemWithAssetResource->getAsset();
        existingAsset2 = itemWithAssetResource->getAssetWithLocation(mico::persistence::model::URI(test_location));

        assert(newAsset);
        assert(existingAsset);
        assert(existingAsset2);

        assert(newAsset->getURI().stringValue().length() > 0);
        assert(existingAsset->getURI().stringValue().length() > 0);
        assert(existingAsset2->getURI().stringValue().length() > 0);

        newAsset->setFormat("video/mp4");
        assert(existingAsset->getFormat().compare("video/mp4") == 0);
        assert(existingAsset2->getFormat().compare("video/mp4") == 0);

        assert(newAsset->getLocation().stringValue().compare(test_location) == 0);
        assert(newAsset->getLocation().stringValue().compare(existingAsset->getLocation().stringValue()) == 0);
        assert(newAsset->getLocation().stringValue().compare(existingAsset2->getLocation().stringValue()) == 0);

        newAsset = itemWithAssetResource->getAssetWithLocation(
                		mico::persistence::model::URI(persistenceServiceTest.svc->getStoragePrefix()+"wRong-But-v4lid/cust0m-loc4tion"));
        assert(newAsset == nullptr );


        persistenceServiceTest.svc->deleteItem(itemWithAssetResource->getURI());


        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        std::cout << "+++++++++++++++++++++++++++++ TESTING NON EXISTING ITEM RETRIEVAL +++++++++++++++++++++++" << std::endl;
        // check non existing item retrieval
        std::shared_ptr<mico::persistence::model::Item> notExistingItem  =
            persistenceServiceTest.svc->getItem(mico::persistence::model::URI("http://does_not_exist_at_all"));

        assert(!notExistingItem);

        std::cout << "+++++++++++++++++++++++++++++ TESTING PART CREATION +++++++++++++++++++++++" << std::endl;
        //part creation
        for (auto itemURI : itemURIS) {
            mico::persistence::model::URI asURI(itemURI);

            std::shared_ptr<mico::persistence::model::Item> retrievedItem  =
                        persistenceServiceTest.svc->getItem(asURI);

            assert(retrievedItem);

            std::stringstream ss_extractor_name;
            ss_extractor_name << "http://my_test_extractor___" << retrievedItem;


            std::shared_ptr<mico::persistence::model::Part> part1  = retrievedItem->createPart(mico::persistence::model::URI(ss_extractor_name.str()));
            std::shared_ptr<mico::persistence::model::Part> part2  = retrievedItem->createPart(mico::persistence::model::URI(ss_extractor_name.str()));

            assert(part1->getSerializedAt().length());
            assert(part2->getSerializedAt().length());

            std::string createdBy = part1->getSerializedBy()->toString()->std_str();

            assert(createdBy.compare( ss_extractor_name.str() ) == 0);

        }

        std::cout << "+++++++++++++++++++++++++++++ TESTING PART RETRIEVAL +++++++++++++++++++++++" << std::endl;
        //part retrieval and body creation - BIG ITEM LOOP!!
        for (auto itemURI : itemURIS) {
            mico::persistence::model::URI asURI(itemURI);

            std::shared_ptr<mico::persistence::model::Item> retrievedItem  =
                        persistenceServiceTest.svc->getItem(asURI);

            std::shared_ptr<mico::persistence::model::Resource> retrievedItemResource =
                std::dynamic_pointer_cast<mico::persistence::model::Resource>(retrievedItem);

            assert(retrievedItem);

            std::list< std::shared_ptr<mico::persistence::model::Part> > itemParts = retrievedItem->getParts();

            assert(itemParts.size() == 2);

            // iterate parts, get uri retrieve part with URI and compare if same
            for (std::shared_ptr<mico::persistence::model::Part> part : itemParts) {

              // existing parts
              std::shared_ptr<mico::persistence::model::Resource> partResource =
                  std::dynamic_pointer_cast<mico::persistence::model::Resource>(part);
              std::shared_ptr<mico::persistence::model::Resource> p_retrieved_as_res =
                  std::dynamic_pointer_cast<mico::persistence::model::Resource>(retrievedItem->getPart(partResource->getURI()));
              assert(partResource->getURI().stringValue().compare(partResource->getURI().stringValue()) == 0);

              part->addInput(std::dynamic_pointer_cast<mico::persistence::model::Resource>(retrievedItem));

              std::list< std::shared_ptr<mico::persistence::model::Resource> > partInputResources =  part->getInputs();

              part->addInput(std::dynamic_pointer_cast<mico::persistence::model::Resource>(retrievedItem));

              assert(partInputResources.size() == 1);



              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              // ++++++++++++++ create a MICO Body and Target aved input http://micobox154:8080/marmotta/d83a4436-4831-469a-ba75-5c5b873a494d of type ItemMMM for part htnd add to part ++++++++++++++
              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

              std::cout << "+++++++++++++++++++++++++++++ TESTING BODY AND TARGET CREATION +++++++++++++++++++++++" << std::endl;

              // !!!! always set the current scope to the JVM !!!

              jnipp::Env::Scope scope(PersistenceService::m_sJvm);

              std::string exceptMsg;

              namespace ns_bodymmm        = jnipp::eu::mico::platform::anno4j::model::impl::bodymmm;
              namespace ns_targetmmm      = jnipp::eu::mico::platform::anno4j::model::impl::targetmmm;
              namespace ns_anno4jmodel    = jnipp::com::github::anno4j::model;
              namespace ns_anno4jselector = jnipp::com::github::anno4j::model::impl::selector;


              jnipp::LocalRef<ns_anno4jmodel::Body> fd_body=
                part->getItem()->createObject(ns_bodymmm::FaceDetectionBodyMMM::clazz());

              jnipp::LocalRef<ns_targetmmm::SpecificResourceMMM> fd_target=
                part->getItem()->createObject(ns_targetmmm::SpecificResourceMMM::clazz());

              jnipp::LocalRef<ns_anno4jselector::FragmentSelector> spatialFragmentSelector=
                part->getItem()->createObject(ns_anno4jselector::FragmentSelector::clazz());

              // !!!! always check Java exceptions through persistence service and returned Java objects for null!!!
              persistenceServiceTest.svc->checkJavaExceptionThrow();
              assert((jobject) fd_body);
              assert((jobject) fd_target);
              assert((jobject) spatialFragmentSelector);

              spatialFragmentSelector->setSpatialFragment(jnipp::java::lang::Integer::construct(100),
                                                          jnipp::java::lang::Integer::construct(100),
                                                          jnipp::java::lang::Integer::construct(400),
                                                          jnipp::java::lang::Integer::construct(300));

              fd_target->setSelector(spatialFragmentSelector);

              part->setBody(fd_body);
              part->addTarget(fd_target);

              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              // +++++++++++++++++++++ Asset creation for part++++++++++++++++++++
              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

              std::cout << "+++++++++++++++++++++++++++++ TESTING ASSET CREATION FOR PART +++++++++++++++++++++++" << std::endl;

              assert(!partResource->hasAsset());

              std::shared_ptr<mico::persistence::model::Asset> newPartAsset = partResource->getAsset();
              std::shared_ptr<mico::persistence::model::Asset> existingPartAsset = partResource->getAsset();

              assert(partResource->hasAsset());

              assert(newPartAsset);
              assert(existingPartAsset);
              assert(newPartAsset->getLocation().stringValue().compare(existingPartAsset->getLocation().stringValue()) == 0);

              newPartAsset->setFormat("image/png");

              assert(existingPartAsset->getFormat().compare("image/png") == 0);



              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
              // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

            }

            std::cout << "+++++++++++++++++++++++++++++ TESTING INPUT ASSIGNMENT +++++++++++++++++++++++" << std::endl;

            //try circular part inputs -> should that be possible at all?
            auto listIt1 = itemParts.begin();
            auto listIt2 = listIt1++;

            (*listIt1)->addInput(std::dynamic_pointer_cast<mico::persistence::model::Resource>(*listIt2));
            (*listIt2)->addInput(std::dynamic_pointer_cast<mico::persistence::model::Resource>(*listIt1));

            std::list< std::shared_ptr<mico::persistence::model::Resource> > partInputResources1 =  (*listIt1)->getInputs();
            std::list< std::shared_ptr<mico::persistence::model::Resource> > partInputResources2 =  (*listIt2)->getInputs();

            assert(partInputResources1.size() == 2);
            assert(partInputResources2.size() == 2);

            // non existing parts check
            std::shared_ptr<mico::persistence::model::Part> notExistingPart  =
                retrievedItem->getPart(mico::persistence::model::URI("http://does_not_exist_at_all"));

            assert(!notExistingPart);

        }

        //check item deletion
        for (auto itemURI : itemURIS) {
           persistenceServiceTest.svc->deleteItem(itemURI);
        }


    } else {
        std::cerr << "usage: <testcmd> <host> <user> <password>" << std::endl;
    }

}
