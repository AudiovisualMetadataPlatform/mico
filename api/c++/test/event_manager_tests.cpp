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

#include <unistd.h>
#include <iostream>
#include <sstream>
#include <fstream>
#include <chrono>
#include <thread>

#include "EventManager.hpp"
#include "SPARQLUtil.hpp"
#include "Uri.hpp"


using namespace std;
using namespace mico::event;
using namespace mico::persistence;

extern std::string mico_host;
extern std::string mico_user;
extern std::string mico_pass;



class EventManagerTest : public ::testing::Test {

protected:
  EventManager* eventManager;
 
  virtual void SetUp() {
    eventManager = new EventManager(mico_host);
  }


  virtual void TearDown() {
    delete eventManager;
  }


};


class MockAnalyser : public AnalysisService {
	
public:
	bool called;

	MockAnalyser(string requires, string provides) 
    : AnalysisService("TestService-"+provides+"-"+requires,"TestMode" , "1.0.0-TestVersion", requires, provides) {}


    /**
     * Call this service for the given content item and object. This method is called by the event manager whenever
     * a new analysis event for this service has been received in its queue. The API takes care of automatically
     * resolving the content item in the persistence service.
     *
     * @param resp   a response object that can be used to send back notifications about new objects to the broker
     * @param ci     the content item to analyse
     * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
     */
    void call(mico::event::AnalysisResponse& resp,
              std::shared_ptr< mico::persistence::model::Item > item,
              std::vector<std::shared_ptr<mico::persistence::model::Resource>> resources,
              std::map<std::string,std::string>& params) {
		std::cout << "analysis callback of mock service " << serviceID.stringValue() << " called!" << std::endl;
    std::shared_ptr<mico::persistence::model::Part> c = item->createPart(mico::persistence::model::URI("http://dont_know_what_to_write_here"));
    std::shared_ptr<mico::persistence::model::Resource> r = std::dynamic_pointer_cast<mico::persistence::model::Resource>(c);
    r->setSyntacticalType( getProvides() );
    resp.sendFinish(item);
		called = true;
	};
	
};

TEST_F(EventManagerTest,registerService) {
	MockAnalyser* s_ab = new MockAnalyser("A","B");
	MockAnalyser* s_bc = new MockAnalyser("B","C");
	MockAnalyser* s_ac = new MockAnalyser("A","C");
	
	eventManager->registerService(s_ab);
	eventManager->registerService(s_bc);
	eventManager->registerService(s_ac);
	
	std::this_thread::sleep_for(std::chrono::duration<double, std::milli>(5000));


	eventManager->unregisterService(s_ab);
	eventManager->unregisterService(s_bc);
	eventManager->unregisterService(s_ac);
}

TEST_F(EventManagerTest, injectContentItem) {
//	ContentItem* item = eventManager->getPersistenceService()->createContentItem();
//	eventManager->injectContentItem(*item);
//	eventManager->getPersistenceService()->deleteContentItem(item->getURI());
//	delete item;
}


TEST_F(EventManagerTest, analyseContentItem) {
//	MockAnalyser* s_ab = new MockAnalyser("A","B");
//	MockAnalyser* s_bc = new MockAnalyser("B","C");
//	MockAnalyser* s_ac = new MockAnalyser("A","C");
	
//	eventManager->registerService(s_ab);
//	eventManager->registerService(s_bc);
//	eventManager->registerService(s_ac);

//	ContentItem* item = eventManager->getPersistenceService()->createContentItem();
//	Content* part = item->createContentPart();
//	part->setType("A");
	
//	eventManager->injectContentItem(*item);

//	// give analysis some time to finish
//	sleep(10);

//	eventManager->unregisterService(s_ab);
//	eventManager->unregisterService(s_bc);
//	eventManager->unregisterService(s_ac);

//	int count = 0;
//	for(auto cp : *item) {
//		count++;
//	}
//	EXPECT_EQ(4,count);

//	eventManager->getPersistenceService()->deleteContentItem(item->getURI());
//	delete item;
}
