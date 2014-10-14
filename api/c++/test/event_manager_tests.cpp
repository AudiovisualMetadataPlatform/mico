#include "gtest.h"

#include <unistd.h>
#include <iostream>
#include <sstream>
#include <fstream>

#include "EventManager.hpp"
#include "ContentItem.hpp"
#include "SPARQLUtil.hpp"


using namespace std;
using namespace mico::event;
using namespace mico::persistence;
using namespace mico::rdf::model;

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
		: AnalysisService("http://example.org/services/cpp/TestService-"+provides+"-"+requires, requires, provides, "queue-"+provides+"-"+requires) {};


    /**
     * Call this service for the given content item and object. This method is called by the event manager whenever
     * a new analysis event for this service has been received in its queue. The API takes care of automatically
     * resolving the content item in the persistence service.
     *
     * @param resp   a response object that can be used to send back notifications about new objects to the broker
     * @param ci     the content item to analyse
     * @param object the URI of the object to analyse in the content item (a content part or a metadata URI)
     */
    void call(std::function<void(const ContentItem& ci, const URI& object)> resp, ContentItem& ci, URI& object) {
		std::cout << "analysis callback of mock service " << serviceID.stringValue() << " called!" << std::endl;
		Content* c = ci.createContentPart();
		c->setType(getProvides());
		resp(ci,c->getURI());		
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
	
	eventManager->unregisterService(s_ab);
	eventManager->unregisterService(s_bc);
	eventManager->unregisterService(s_ac);
}

TEST_F(EventManagerTest, injectContentItem) {
	ContentItem* item = eventManager->getPersistenceService().createContentItem();
	eventManager->injectContentItem(*item);
	eventManager->getPersistenceService().deleteContentItem(item->getURI());
	delete item;
}


TEST_F(EventManagerTest, analyseContentItem) {
	MockAnalyser* s_ab = new MockAnalyser("A","B");
	MockAnalyser* s_bc = new MockAnalyser("B","C");
	MockAnalyser* s_ac = new MockAnalyser("A","C");
	
	eventManager->registerService(s_ab);
	eventManager->registerService(s_bc);
	eventManager->registerService(s_ac);

	ContentItem* item = eventManager->getPersistenceService().createContentItem();
	Content* part = item->createContentPart();
	part->setType("A");
	
	eventManager->injectContentItem(*item);

	// give analysis some time to finish
	sleep(10);

	eventManager->unregisterService(s_ab);
	eventManager->unregisterService(s_bc);
	eventManager->unregisterService(s_ac);

	int count = 0;
	for(auto cp : *item) {
		count++;
	}
	EXPECT_EQ(4,count);

	eventManager->getPersistenceService().deleteContentItem(item->getURI());
	delete item;
}
