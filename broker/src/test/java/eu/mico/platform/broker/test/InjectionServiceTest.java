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
package eu.mico.platform.broker.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.webservices.InjectionWebService;
import eu.mico.platform.broker.webservices.WorkflowManagementService;
import eu.mico.platform.camel.MicoCamelContext;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;

import org.apache.camel.Endpoint;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */


public class InjectionServiceTest extends BaseBrokerTest {

	private static final String UNKNOWN_ITEM_URI = "https://unknown.item/uri";
	
	private static Logger log = LoggerFactory.getLogger(InjectionServiceTest.class);
    private static MicoCamelContext context = new MicoCamelContext();
    private static Map<String,MICOCamelRoute> routes = new HashMap<String,MICOCamelRoute>();
    
    private static WorkflowManagementService wManager = null;
    private static InjectionWebService injService = null;
    private static final String USER = "INJECTION-TEST-USER-"+UUID.randomUUID().toString();
	
	@BeforeClass 
	public static void init() throws IOException, TimeoutException, URISyntaxException{
		
		 eventManager = new EventManagerImpl(amqpHost, amqpUsr, amqpPwd, amqpVHost);
	     eventManager.init();
		
    	context.init(broker.getPersistenceService());
    	wManager = new WorkflowManagementService(broker, context, routes);
    	injService = new InjectionWebService(broker, eventManager, context, routes);
	}
	
	@After
	@Before
	public void cleanupAMQPChannel() throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException{
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(EventManager.QUEUE_PART_OUTPUT, true, consumer);
	
	    //consume pending requests, if any are present
	    QueueingConsumer.Delivery delivery = consumer.nextDelivery(10);
	    while(delivery != null){
	     	delivery = consumer.nextDelivery(10);
	    }
	}
	
	@Test
    public void testOldInjectionWithSimpleItem() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	//setup extractors
    	MockService abService = new MockServiceInjTest("A", "B");
    	MockService abService1= new MockServiceInjTest("A", "B");
    	MockService abService2= new MockServiceInjTest("A", "B");
    	MockService acService = new MockServiceInjTest("A", "C");
    	MockService bcService = new MockServiceInjTest("B", "C");
    	
    	
    	//connect A-B extractors 

    	connectExtractor(abService);	//produces 1 'B' part
    	connectExtractor(abService1);	//produces 1 'B' part
    	connectExtractor(abService2);	//produces 1 'B' part
    	connectExtractor(acService);	//produces 1 'C' part
    	connectExtractor(bcService);	//produces 1 'B' part
    	
    	
    	//broadcast a simple item with mimeType 'A'
    	
    	Item processedItem = broadcastSimpleItem("A");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 3 'b' (A-B) + 1 'c' (A->C) + 3x1 'c' (B-C)
    	Assert.assertEquals(7, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	List<Part> parts = ImmutableList.copyOf(processedItem.getParts());
    	int numAParts=0;
    	int numBParts=0;
    	int numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(3,numBParts);
    	Assert.assertEquals(4,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    	
    	//broadcast a simple item with mimeType 'B'
    	processedItem = broadcastSimpleItem("B");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1 'c' (B-C)
    	Assert.assertEquals(1, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	parts = ImmutableList.copyOf(processedItem.getParts());
    	numAParts=0;
    	numBParts=0;
    	numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(0,numBParts);
    	Assert.assertEquals(1,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    	
    	//broadcast a simple item with mimeType 'C'
    	processedItem = broadcastSimpleItem("C");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 0
    	Assert.assertEquals(0, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	parts = ImmutableList.copyOf(processedItem.getParts());
    	numAParts=0;
    	numBParts=0;
    	numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(0,numBParts);
    	Assert.assertEquals(0,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    }
	
	@Test
    public void testOldInjectionWithComplexItem() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	//setup extractors
    	MockService abService = new MockServiceInjTest("A", "B");
    	MockService abService1= new MockServiceInjTest("A", "B");
    	MockService abService2= new MockServiceInjTest("A", "B");
    	MockService acService = new MockServiceInjTest("A", "C");
    	MockService bcService = new MockServiceInjTest("B", "C");
    	
    	
    	//connect A-B extractors 

    	connectExtractor(abService);	//produces 1 'B' part
    	connectExtractor(abService1);	//produces 1 'B' part
    	connectExtractor(abService2);	//produces 1 'B' part
    	connectExtractor(acService);	//produces 1 'C' part
    	connectExtractor(bcService);	//produces 1 'B' part
    	
    	
    	//broadcast a simple item with mimeType 'A'
    	
    	Item processedItem = broker.getPersistenceService().getItem(broadcastComplexItem("A").getURI());
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1 'a' + 3 'b' (A-B) + 1 'c' (A->C) + 3x1 'c' (B-C)
    	Assert.assertEquals(8, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	List<Part> parts = ImmutableList.copyOf(processedItem.getParts());
    	int numAParts=0;
    	int numBParts=0;
    	int numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(1,numAParts);
    	Assert.assertEquals(3,numBParts);
    	Assert.assertEquals(4,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    	
    	//broadcast a simple item with mimeType 'B'
    	processedItem = broadcastComplexItem("B");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1 'b' + 1 'c' (B-C)
    	Assert.assertEquals(2, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	parts = ImmutableList.copyOf(processedItem.getParts());
    	numAParts=0;
    	numBParts=0;
    	numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(1,numBParts);
    	Assert.assertEquals(1,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    	
    	//broadcast a simple item with mimeType 'C'
    	processedItem = broadcastComplexItem("C");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1 'c'
    	Assert.assertEquals(1, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	parts = ImmutableList.copyOf(processedItem.getParts());
    	numAParts=0;
    	numBParts=0;
    	numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(0,numBParts);
    	Assert.assertEquals(1,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    }
	
    public void testOldInjectionWithNastyItem() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	//setup extractors
    	MockService abService = new MockServiceInjTest("A", "B");
    	MockService abService1= new MockServiceInjTest("A", "B");
    	MockService abService2= new MockServiceInjTest("A", "B");
    	MockService acService = new MockServiceInjTest("A", "C");
    	MockService bcService = new MockServiceInjTest("B", "C");
    	
    	
    	//connect A-B extractors 

    	connectExtractor(abService);	//produces 1 'B' part
    	connectExtractor(abService1);	//produces 1 'B' part
    	connectExtractor(abService2);	//produces 1 'B' part
    	connectExtractor(acService);	//produces 1 'C' part
    	connectExtractor(bcService);	//produces 1 'B' part
    	
    	
    	//broadcast a simple item with mimeType 'A'
    	
    	Item processedItem = broadcastNastyItem("A");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1 'a' + 3x2 'b' (A-B) + 1x2 'c' (A->C) + 3x1x2 'c' (B-C)
    	Assert.assertEquals(15, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	List<Part> parts = ImmutableList.copyOf(processedItem.getParts());
    	int numAParts=0;
    	int numBParts=0;
    	int numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(1,numAParts);
    	Assert.assertEquals(6,numBParts);
    	Assert.assertEquals(8,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    	
    	//broadcast a simple item with mimeType 'B'
    	processedItem = broadcastNastyItem("B");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1'b' + 1x2 'c' (B-C)
    	Assert.assertEquals(3, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	parts = ImmutableList.copyOf(processedItem.getParts());
    	numAParts=0;
    	numBParts=0;
    	numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(1,numBParts);
    	Assert.assertEquals(2,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    	
    	//broadcast a simple item with mimeType 'C'
    	processedItem = broadcastNastyItem("C");
    	
    	//and check that all extractors were triggered
    	Assert.assertNotNull("Unable to retrieve an item from the persistence service",processedItem); 
    	
    	//total parts are 1'c'
    	Assert.assertEquals(1, ImmutableSet.copyOf(processedItem.getParts()).size());
    	
    	parts = ImmutableList.copyOf(processedItem.getParts());
    	numAParts=0;
    	numBParts=0;
    	numCParts=0;
    	for(Part p : parts){
    		if(p.getSyntacticalType().contentEquals("A"))  numAParts++;
    		if(p.getSyntacticalType().contentEquals("B"))  numBParts++;
    		if(p.getSyntacticalType().contentEquals("C"))  numCParts++;
    	}
    	
    	Assert.assertEquals(0,numAParts);
    	Assert.assertEquals(0,numBParts);
    	Assert.assertEquals(1,numCParts);
    	broker.getPersistenceService().deleteItem(processedItem.getURI());
    	
    	
    }
	

    @Test
    public void testInjectionWithWrongArgs() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
        
        try{
            injService.submitItem("not-an-URI", null, null);
        }catch(IllegalArgumentException ex){
            Assert.assertEquals("Not a valid (absolute) URI: not-an-URI", ex.getLocalizedMessage());
        }
        
        Response r = null;
        Item item = null;
        PersistenceService ps = broker.getPersistenceService();
        String mimeType = "mico:test-item";
        try {
            item = ps.createItem();
            item.setSemanticType("Simple type "+mimeType);
            item.setSyntacticalType(mimeType);

            r = injService.submitItem(null, null, null);
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),r.getStatus());
            Assert.assertEquals("item parameter not set",r.getEntity());

            r = injService.submitItem("", null, null);
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),r.getStatus());
            Assert.assertEquals("item parameter not set",r.getEntity());

            r = injService.submitItem(UNKNOWN_ITEM_URI, null, null);
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),r.getStatus());
            Assert.assertEquals("No item found with uri: "+UNKNOWN_ITEM_URI,r.getEntity());
            
            r = injService.submitItem(UNKNOWN_ITEM_URI, "", null);
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),r.getStatus());
            Assert.assertEquals("route parameter not set",r.getEntity());
            
            r = injService.submitItem(item.getURI().toString(), "unknown route", null);
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),r.getStatus());
            Assert.assertEquals("No route found with id: unknown route",r.getEntity());
            
        }catch (Exception e){
            log.error("Unexpected exception: " + e.getMessage());
            if (item != null)
                ps.deleteItem(item.getURI());
            Assert.fail();
        }

    }

	@Test
    public void testNewInjectionWithSimpleItem() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
    	//setup extractors
    	MockService abService = new MockServiceInjTest("A", "B");
    	MockService abService1 =new MockServiceInjTest("A", "B");
    	MockService abService2 =new MockServiceInjTest("A", "B");
    	MockService acService = new MockServiceInjTest("A", "C");
    	MockService bcService = new MockServiceInjTest("B", "C");
    	
    	
    	//setup test routes
    	
    	String A_B_MICO_TEST =WorkflowServiceTest.createTestRoute(abService, "mico:A-B", "mico/test");
    	String A_B_MICO_TEST1=WorkflowServiceTest.createTestRoute(abService1,"mico:A-B", "mico/test1");
    	String A_B_MICO_TEST2=WorkflowServiceTest.createTestRoute(abService2,"mico:A-B", "mico/test2");    	
    	String A_C_MICO_TEST =WorkflowServiceTest.createTestRoute(acService, "mico:A-C", "mico/test");
    	String B_C_MICO_TEST =WorkflowServiceTest.createTestRoute(bcService, "mico:B-C", "mico/test");
    	
    	//and publish them
    	
    	Map<String,String> routeIds = new HashMap<String,String>();
    	
        routeIds.put(A_B_MICO_TEST, wManager.addWorkflow(USER, A_B_MICO_TEST));
    	routeIds.put(A_B_MICO_TEST1,wManager.addWorkflow(USER, A_B_MICO_TEST1));
    	routeIds.put(A_B_MICO_TEST2,wManager.addWorkflow(USER, A_B_MICO_TEST2));
    	routeIds.put(A_C_MICO_TEST, wManager.addWorkflow(USER, A_C_MICO_TEST ));
    	routeIds.put(B_C_MICO_TEST, wManager.addWorkflow(USER, B_C_MICO_TEST ));

    	Map<String,MockEndpoint> mocks = new HashMap<String,MockEndpoint>();    	

    	//setup the mock endpoints
    	mocks.put(A_B_MICO_TEST, getMockEndpoint("mock:auto-test-route-A-B-mico/test"));
    	mocks.put(A_B_MICO_TEST1,getMockEndpoint("mock:auto-test-route-A-B-mico/test1"));
    	mocks.put(A_B_MICO_TEST2,getMockEndpoint("mock:auto-test-route-A-B-mico/test2"));
    	mocks.put(A_C_MICO_TEST, getMockEndpoint("mock:auto-test-route-A-C-mico/test"));
    	mocks.put(B_C_MICO_TEST, getMockEndpoint("mock:auto-test-route-B-C-mico/test"));
    	
        
        //try triggering all routes correctly, and verify that no mock is activated
    	for(MockEndpoint m : mocks.values()){
    		m.setExpectedCount(0);
    	}
    	
    	//not-registered = broken = BAD_REQUEST
    	
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	//register the extractors, and repeat the same check
    	registerExtractor(abService, "mico/test");
    	registerExtractor(abService1, "mico/test1");
    	registerExtractor(abService2, "mico/test2");
    	registerExtractor(acService, "mico/test");
    	registerExtractor(bcService, "mico/test");
    	
    	
    	//registered but not connected = UNAVAILABLE
    	
    	Thread.sleep(500);
    	
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	
    	//connect the extractors, and check that the mocks are activated only ONCE EACH

    	connectExtractor(abService);
    	connectExtractor(abService1);
    	connectExtractor(abService2);
    	connectExtractor(acService);
    	connectExtractor(bcService);
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.setExpectedCount(1);
    	}
    	
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	 //try triggering all routes INCORRECTLY, and verify that no mock is activated
    	for(MockEndpoint m : mocks.values()){
    		m.reset();
    		m.setExpectedCount(0);
    	}
    	
    	//requests with correct syntactic type, but wrong mime type
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	
    	//request with correct mime type, but wrong syntactic type
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-B","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	
    	//request where both types are wrong
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:A-C","mico/test2s", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}

    	
    	//try triggering one routes correctly, but with a FAILING EXTRACTOR, and verify that the mock is not activated

    	MockService abFailingService = new MockFailingServiceInjTest("ERROR", "B");
    	registerExtractor(abFailingService, "mico/error");
    	connectExtractor(abFailingService);
    	
    	String ERROR_B_MICO_FAILING_TEST =WorkflowServiceTest.createTestRoute(abFailingService, "mico:ERROR-B", "mico/error");
    	routeIds.put(ERROR_B_MICO_FAILING_TEST, wManager.addWorkflow(USER, ERROR_B_MICO_FAILING_TEST ));
    	
    	mocks.put(ERROR_B_MICO_FAILING_TEST, getMockEndpoint("mock:auto-test-route-ERROR-B-mico/error"));
    	mocks.get(ERROR_B_MICO_FAILING_TEST).setExpectedCount(0);
    	

    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithSimpleItem("mico:ERROR-B","mico/error",routeIds.get(ERROR_B_MICO_FAILING_TEST)).getStatus());

    	mocks.get(ERROR_B_MICO_FAILING_TEST).assertIsSatisfied();
    	//TODO: find a way to assert that the received body holds an error state
    	
    	//cleanup
    	
    	unregisterExtractor(abService);
    	unregisterExtractor(abService1);
    	unregisterExtractor(abService2);
    	unregisterExtractor(acService);
    	unregisterExtractor(bcService);
    	unregisterExtractor(abFailingService);
    	
    	for(String id : routeIds.values()){
    		wManager.deleteWorkflow(id.toString());
    	}
    }
    

	@Test
    public void testNewInjectionWithComplexItem() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
    	//setup extractors
    	MockService abService = new MockServiceInjTest("A", "B");
    	MockService abService1 =new MockServiceInjTest("A", "B");
    	MockService abService2 =new MockServiceInjTest("A", "B");
    	MockService acService = new MockServiceInjTest("A", "C");
    	MockService bcService = new MockServiceInjTest("B", "C");
    	
    	
    	//setup test routes
    	
    	String A_B_MICO_TEST =WorkflowServiceTest.createTestRoute(abService, "mico:A-B", "mico/test");
    	String A_B_MICO_TEST1=WorkflowServiceTest.createTestRoute(abService1,"mico:A-B", "mico/test1");
    	String A_B_MICO_TEST2=WorkflowServiceTest.createTestRoute(abService2,"mico:A-B", "mico/test2");    	
    	String A_C_MICO_TEST =WorkflowServiceTest.createTestRoute(acService, "mico:A-C", "mico/test");
    	String B_C_MICO_TEST =WorkflowServiceTest.createTestRoute(bcService, "mico:B-C", "mico/test");
    	
    	//and publish them
    	
    	Map<String,String> routeIds = new HashMap<String,String>();
    	
        routeIds.put(A_B_MICO_TEST, wManager.addWorkflow(USER, A_B_MICO_TEST ));
    	routeIds.put(A_B_MICO_TEST1,wManager.addWorkflow(USER, A_B_MICO_TEST1));
    	routeIds.put(A_B_MICO_TEST2,wManager.addWorkflow(USER, A_B_MICO_TEST2));
    	routeIds.put(A_C_MICO_TEST, wManager.addWorkflow(USER, A_C_MICO_TEST ));
    	routeIds.put(B_C_MICO_TEST, wManager.addWorkflow(USER, B_C_MICO_TEST ));

    	Map<String,MockEndpoint> mocks = new HashMap<String,MockEndpoint>();    	

    	//setup the mock endpoints
    	mocks.put(A_B_MICO_TEST, getMockEndpoint("mock:auto-test-route-A-B-mico/test"));
    	mocks.put(A_B_MICO_TEST1,getMockEndpoint("mock:auto-test-route-A-B-mico/test1"));
    	mocks.put(A_B_MICO_TEST2,getMockEndpoint("mock:auto-test-route-A-B-mico/test2"));
    	mocks.put(A_C_MICO_TEST, getMockEndpoint("mock:auto-test-route-A-C-mico/test"));
    	mocks.put(B_C_MICO_TEST, getMockEndpoint("mock:auto-test-route-B-C-mico/test"));
    	
        
        //try triggering all routes correctly, and verify that no mock is activated
    	for(MockEndpoint m : mocks.values()){
    		m.setExpectedCount(0);
    	}
    	
    	//not-registered = broken = BAD_REQUEST
    	
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	//register the extractors, and repeat the same check
    	registerExtractor(abService, "mico/test");
    	registerExtractor(abService1, "mico/test1");
    	registerExtractor(abService2, "mico/test2");
    	registerExtractor(acService, "mico/test");
    	registerExtractor(bcService, "mico/test");
    	
    	
    	//registered but not connected = UNAVAILABLE
    	
    	Thread.sleep(500);
    	
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithComplexItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	
    	//connect the extractors, and check that the mocks are activated only ONCE EACH

    	connectExtractor(abService);
    	connectExtractor(abService1);
    	connectExtractor(abService2);
    	connectExtractor(acService);
    	connectExtractor(bcService);
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.setExpectedCount(1);
    	}
    	
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithComplexItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	 //try triggering all routes INCORRECTLY, and verify that no mock is activated
    	for(MockEndpoint m : mocks.values()){
    		m.reset();
    		m.setExpectedCount(0);
    	}
    	
    	//requests with correct syntactic type, but wrong mime type
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	
    	//request with correct mime type, but wrong syntactic type
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-B","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	
    	//request where both types are wrong
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithComplexItem("mico:A-C","mico/test2s", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}

    	
    	//try triggering one routes correctly, but with a FAILING EXTRACTOR, and verify that the mock is not activated

    	MockService abFailingService = new MockFailingServiceInjTest("ERROR", "B");
    	registerExtractor(abFailingService, "mico/error");
    	connectExtractor(abFailingService);
    	
    	String ERROR_B_MICO_FAILING_TEST =WorkflowServiceTest.createTestRoute(abFailingService, "mico:ERROR-B", "mico/error");
    	routeIds.put(ERROR_B_MICO_FAILING_TEST, wManager.addWorkflow(USER, ERROR_B_MICO_FAILING_TEST ));
    	
    	mocks.put(ERROR_B_MICO_FAILING_TEST, getMockEndpoint("mock:auto-test-route-ERROR-B-mico/error"));
    	mocks.get(ERROR_B_MICO_FAILING_TEST).setExpectedCount(0);
    	

    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithComplexItem("mico:ERROR-B","mico/error",routeIds.get(ERROR_B_MICO_FAILING_TEST)).getStatus());

    	mocks.get(ERROR_B_MICO_FAILING_TEST).assertIsSatisfied();
    	//TODO: find a way to assert that the received body holds an error state
    	
    	//cleanup
    	
    	unregisterExtractor(abService);
    	unregisterExtractor(abService1);
    	unregisterExtractor(abService2);
    	unregisterExtractor(acService);
    	unregisterExtractor(bcService);
    	unregisterExtractor(abFailingService);
    	
    	for(String id : routeIds.values()){
    		wManager.deleteWorkflow(id);
    	}    	
    }
    

	@Test
    public void testNewInjectionWithNastyItem() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
    	//setup extractors
    	MockService abService = new MockServiceInjTest("A", "B");
    	MockService abService1 =new MockServiceInjTest("A", "B");
    	MockService abService2 =new MockServiceInjTest("A", "B");
    	MockService acService = new MockServiceInjTest("A", "C");
    	MockService bcService = new MockServiceInjTest("B", "C");
    	
    	
    	//setup test routes
    	
    	String A_B_MICO_TEST =WorkflowServiceTest.createTestRoute(abService, "mico:A-B", "mico/test");
    	String A_B_MICO_TEST1=WorkflowServiceTest.createTestRoute(abService1,"mico:A-B", "mico/test1");
    	String A_B_MICO_TEST2=WorkflowServiceTest.createTestRoute(abService2,"mico:A-B", "mico/test2");    	
    	String A_C_MICO_TEST =WorkflowServiceTest.createTestRoute(acService, "mico:A-C", "mico/test");
    	String B_C_MICO_TEST =WorkflowServiceTest.createTestRoute(bcService, "mico:B-C", "mico/test");
    	
    	//and publish them
    	
    	Map<String,String> routeIds = new HashMap<String,String>();
    	
        routeIds.put(A_B_MICO_TEST, wManager.addWorkflow(USER, A_B_MICO_TEST ));
    	routeIds.put(A_B_MICO_TEST1,wManager.addWorkflow(USER, A_B_MICO_TEST1));
    	routeIds.put(A_B_MICO_TEST2,wManager.addWorkflow(USER, A_B_MICO_TEST2));
    	routeIds.put(A_C_MICO_TEST, wManager.addWorkflow(USER, A_C_MICO_TEST ));
    	routeIds.put(B_C_MICO_TEST, wManager.addWorkflow(USER, B_C_MICO_TEST ));

    	Map<String,MockEndpoint> mocks = new HashMap<String,MockEndpoint>();    	

    	//setup the mock endpoints
    	mocks.put(A_B_MICO_TEST, getMockEndpoint("mock:auto-test-route-A-B-mico/test"));
    	mocks.put(A_B_MICO_TEST1,getMockEndpoint("mock:auto-test-route-A-B-mico/test1"));
    	mocks.put(A_B_MICO_TEST2,getMockEndpoint("mock:auto-test-route-A-B-mico/test2"));
    	mocks.put(A_C_MICO_TEST, getMockEndpoint("mock:auto-test-route-A-C-mico/test"));
    	mocks.put(B_C_MICO_TEST, getMockEndpoint("mock:auto-test-route-B-C-mico/test"));
    	
        
        //try triggering all routes correctly, and verify that no mock is activated
    	for(MockEndpoint m : mocks.values()){
    		m.setExpectedCount(0);
    	}
    	
    	//not-registered = broken = BAD_REQUEST
    	
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	//register the extractors, and repeat the same check
    	registerExtractor(abService, "mico/test");
    	registerExtractor(abService1, "mico/test1");
    	registerExtractor(abService2, "mico/test2");
    	registerExtractor(acService, "mico/test");
    	registerExtractor(bcService, "mico/test");
    	
    	
    	//registered but not connected = UNAVAILABLE
    	
    	Thread.sleep(500);
    	
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
    			triggerRouteWithNastyItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	
    	//connect the extractors, and check that the mocks are activated only ONCE EACH

    	connectExtractor(abService);
    	connectExtractor(abService1);
    	connectExtractor(abService2);
    	connectExtractor(acService);
    	connectExtractor(bcService);
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.setExpectedCount(2);
    	}
    	
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-C","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithNastyItem("mico:B-C","mico/test", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}
    	
    	 //try triggering all routes INCORRECTLY, and verify that no mock is activated
    	for(MockEndpoint m : mocks.values()){
    		m.reset();
    		m.setExpectedCount(0);
    	}
    	
    	//requests with correct syntactic type, but wrong mime type
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test1",routeIds.get(A_B_MICO_TEST)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test2",routeIds.get(A_B_MICO_TEST1)).getStatus());
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test",routeIds.get(A_B_MICO_TEST2)).getStatus());
    	
    	//request with correct mime type, but wrong syntactic type
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-B","mico/test", routeIds.get(A_C_MICO_TEST)).getStatus());
    	
    	//request where both types are wrong
    	Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(),
    			triggerRouteWithNastyItem("mico:A-C","mico/test2s", routeIds.get(B_C_MICO_TEST)).getStatus());
    	
    	Thread.sleep(500);
    	
    	for(MockEndpoint m : mocks.values()){
    		m.assertIsSatisfied();
    	}

    	
    	//try triggering one routes correctly, but with a FAILING EXTRACTOR, and verify that the mock is not activated

    	MockService abFailingService = new MockFailingServiceInjTest("ERROR", "B");
    	registerExtractor(abFailingService, "mico/error");
    	connectExtractor(abFailingService);
    	
    	String ERROR_B_MICO_FAILING_TEST =WorkflowServiceTest.createTestRoute(abFailingService, "mico:ERROR-B", "mico/error");
    	routeIds.put(ERROR_B_MICO_FAILING_TEST, wManager.addWorkflow(USER, ERROR_B_MICO_FAILING_TEST ));
    	
    	mocks.put(ERROR_B_MICO_FAILING_TEST, getMockEndpoint("mock:auto-test-route-ERROR-B-mico/error"));
    	mocks.get(ERROR_B_MICO_FAILING_TEST).setExpectedCount(0);
    	

    	Assert.assertEquals(Status.OK.getStatusCode(),
    			triggerRouteWithNastyItem("mico:ERROR-B","mico/error",routeIds.get(ERROR_B_MICO_FAILING_TEST)).getStatus());

    	mocks.get(ERROR_B_MICO_FAILING_TEST).assertIsSatisfied();
    	//TODO: find a way to assert that the received body holds an error state
    	
    	//cleanup
    	
    	unregisterExtractor(abService);
    	unregisterExtractor(abService1);
    	unregisterExtractor(abService2);
    	unregisterExtractor(acService);
    	unregisterExtractor(bcService);
    	unregisterExtractor(abFailingService);
    	
    	for(String id : routeIds.values()){
    		wManager.deleteWorkflow(id);
    	}
    }
    
    private Item broadcastSimpleItem(String mimeType) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             item.setSemanticType("Simple type "+mimeType);
             item.setSyntacticalType(mimeType);
             item.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), null, null);
             
             Thread.sleep(1000);
             
             Assert.assertEquals("The response should have been [OK]", 
            		              Status.OK.getStatusCode(),r.getStatus());
             
             return item;
         }
         catch(Exception e){
        	 log.error("Unexpected exception: ");
        	 e.printStackTrace();
        	 if(item!=null) ps.deleteItem(item.getURI());
        	 Assert.fail();
        	 return null;
         }
    }
    
    private Item broadcastComplexItem(String mimeType) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             item.setSemanticType("Empty item");
             item.setSyntacticalType("mico:Item");
             
             Part part =item.createPart(new URIImpl("urn:org.example.injection"));
             part.setSemanticType("Simple type "+mimeType);
             part.setSyntacticalType(mimeType);
             part.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), null, null);

             Thread.sleep(1000);
             
             Assert.assertEquals("The response should have been [OK]", 
            		              Status.OK.getStatusCode(),r.getStatus());
             
             return item;
         }
         catch(Exception e){
        	 log.error("Unexpected exception: ");
        	 e.printStackTrace();
        	 if(item!=null) ps.deleteItem(item.getURI());
        	 Assert.fail();
        	 return null;
         }
    }
    
    private Item broadcastNastyItem(String mimeType) throws RepositoryException, IOException, InterruptedException
    {
    	PersistenceService ps = broker.getPersistenceService();
    	Item item = null;
    	try {
    		item = ps.createItem();
    		item.setSemanticType("Simple type "+mimeType);
    		item.setSyntacticalType(mimeType);
    		item.getAsset().setFormat(mimeType);

    		Part part =item.createPart(new URIImpl("urn:org.example.injection"));
    		part.setSemanticType("Simple type "+mimeType);
    		part.setSyntacticalType(mimeType);
    		part.getAsset().setFormat(mimeType);

    		Response r = injService.submitItem(item.getURI().stringValue(), null, null);

    		Thread.sleep(1000);

    		Assert.assertEquals("The response should have been [OK]", 
    				Status.OK.getStatusCode(),r.getStatus());

    		return item;
    	}
        catch(Exception e){
       	 log.error("Unexpected exception: ");
       	 e.printStackTrace();
       	 if(item!=null) ps.deleteItem(item.getURI());
       	 Assert.fail();
       	 return null;
        }
    }
    
    private Response triggerRouteWithSimpleItem(String syntacticType,String mimeType,String routeId) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             item.setSemanticType(syntacticType+" over "+mimeType);
             item.setSyntacticalType(syntacticType);
             item.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), routeId, null);
             
             Thread.sleep(500);
             
             if(r.getStatus()==Status.OK.getStatusCode() && !mimeType.contentEquals("mico/error")){
            	 String expectedNewType = String.valueOf(syntacticType.charAt(syntacticType.length()-1));
            	 
            	 
            	 Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            	 Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty(
            	 "syntacticalType", equalTo(expectedNewType))));
             }
             
             return r;
         }
         catch(Exception e){
        	 log.error("Unexpected exception: ");
        	 e.printStackTrace();
        	 Assert.fail();
        	 return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
         finally{
        	 if(item != null){
                 ps.deleteItem(item.getURI());
             }
        	 
        }
         
         
    }
    
    private Response triggerRouteWithComplexItem(String syntacticType,String mimeType,String routeId) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             
             Part part =item.createPart(new URIImpl("urn:org.example.injection"));
             part.setSemanticType(syntacticType+" over "+mimeType);
             part.setSyntacticalType(syntacticType);
             part.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), routeId, null);
             
             Thread.sleep(200);
             
             if(r.getStatus()==Status.OK.getStatusCode() && !mimeType.contentEquals("mico/error")){
            	 
            	 String expectedNewType = String.valueOf(syntacticType.charAt(syntacticType.length()-1));
            	 
            	 Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            	 Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty(
            	 "syntacticalType", equalTo(expectedNewType))));
            	 
            	 Assert.assertEquals(2, parts.size());
             }
             
             return r;
         }
         catch(Exception e){
        	 log.error("Unexpected exception: ");
        	 e.printStackTrace();
        	 Assert.fail();
        	 return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
         finally{
        	 if(item != null){
                 ps.deleteItem(item.getURI());
             }
        	 
        }
         
         
    }
    
    private Response triggerRouteWithNastyItem(String syntacticType,String mimeType,String routeId) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             item.setSemanticType(syntacticType+" over "+mimeType);
             item.setSyntacticalType(syntacticType);
             item.getAsset().setFormat(mimeType);
             
             Part part =item.createPart(new URIImpl("urn:org.example.injection"));
             part.setSemanticType(syntacticType+" over "+mimeType);
             part.setSyntacticalType(syntacticType);
             part.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), routeId, null);

             Thread.sleep(500);
             
             if(r.getStatus()==Status.OK.getStatusCode() && !mimeType.contentEquals("mico/error")){
            	 
            	 String expectedNewType = String.valueOf(syntacticType.charAt(syntacticType.length()-1));
            	 
            	 Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            	 Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty(
            	 "syntacticalType", equalTo(expectedNewType))));
            	 
            	 Assert.assertEquals(3, parts.size());
             }
             
             return r;
         }
         catch(Exception e){
        	 log.error("Unexpected exception: ");
        	 e.printStackTrace();
        	 Assert.fail();
        	 return Response.status(Status.INTERNAL_SERVER_ERROR).build();
         }
         finally{
        	 if(item != null){
                 ps.deleteItem(item.getURI());
             }
        	 
        }
         
         
    }
    

    /**
     * Resolves the {@link MockEndpoint} using a URI of the form
     * <code>mock:someName</code>, optionally creating it if it does not exist.
     *
     * @param uri
     *            the URI which typically starts with "mock:" and has some name
     * @return the mock endpoint or an {@link NoSuchEndpointException} is thrown
     *         if it could not be resolved
     * @throws NoSuchEndpointException
     *             is the mock endpoint does not exists
     */
    protected MockEndpoint getMockEndpoint(String uri)
            throws NoSuchEndpointException {
        Endpoint endpoint = context.hasEndpoint(uri);
        if (endpoint instanceof MockEndpoint) {
            return (MockEndpoint) endpoint;
        }
        throw new NoSuchEndpointException(String.format(
                "MockEndpoint %s does not exist.", uri));
    }

    /**
     * Asserts that all the expectations of the Mock endpoints are valid
     * @param mock 
     */
    protected void assertMockEndpointsSatisfied(MockEndpoint... mock) throws InterruptedException {
        MockEndpoint.assertIsSatisfied(mock);
    }

    /**
     * Asserts that all the expectations of the Mock endpoints are valid
     */
    protected void assertMockEndpointsSatisfied(long timeout, TimeUnit unit, MockEndpoint... mock) throws InterruptedException {
        MockEndpoint.assertIsSatisfied(timeout, unit,mock);
    }
    
    protected static class MockServiceInjTest extends MockService{

    	private String extractorId;
    	
		public MockServiceInjTest(String source, String target) {
			super(source, target);
			this.extractorId=UUID.randomUUID().toString();
		}
		
		public MockServiceInjTest(String source, String target, boolean createAsset) {
			super(source, target,createAsset);
			this.extractorId=UUID.randomUUID().toString();
		}
		
		@Override
		public String getExtractorID() {
			return "urn:org.example.services-"+extractorId;
		}
    	
    }
    
    protected static class MockFailingServiceInjTest extends MockServiceInjTest{

    	public MockFailingServiceInjTest(String source, String target) {
			super(source, target);
		}
		
		public MockFailingServiceInjTest(String source, String target, boolean createAsset) {
			super(source, target,createAsset);
		}
		
		@Override
		public void call(AnalysisResponse resp,
                Item item,
                java.util.List<eu.mico.platform.persistence.model.Resource> resourceList,
                java.util.Map<String, String> params) throws AnalysisException,
                IOException {
            log.info("mock analysis FAILING request for [{}] on queue {}",
                    resourceList.get(0).getURI(), getQueueName());
            throw new AnalysisException("exception from mock service");
			
		}
    	
    }
}
