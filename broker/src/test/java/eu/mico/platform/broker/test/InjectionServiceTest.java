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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ByteString;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.test.BaseBrokerTest.MockService;
import eu.mico.platform.broker.webservices.InjectionWebService;
import eu.mico.platform.broker.webservices.WorkflowManagementService;
import eu.mico.platform.camel.MicoCamelContext;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.MessageType;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;

import org.apache.camel.Endpoint;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

	private static Logger log = LoggerFactory.getLogger(WorkflowServiceTest.class);
    private static MicoCamelContext context = new MicoCamelContext();
    private static Map<Integer,MICOCamelRoute> routes = new HashMap<Integer,MICOCamelRoute>();
    
    private static WorkflowManagementService wManager = null;
    private static InjectionWebService injService = null;
    private static final String USER = "INJECTION-TEST-USER-"+UUID.randomUUID().toString();
	
	@BeforeClass 
	public static void init() throws IOException, TimeoutException, URISyntaxException{
		
		 eventManager = new EventManagerImpl(amqpHost, amqpUsr, amqpPwd, amqpVHost);
	     eventManager.init();
		
    	context.init();
    	wManager = new WorkflowManagementService(broker, context, routes);
    	injService = new InjectionWebService(broker, eventManager, context, routes);
	}

    @SuppressWarnings("deprecation")
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
    	
    	Map<String,Integer> routeIds = new HashMap<String,Integer>();
    	
        routeIds.put(A_B_MICO_TEST, wManager.addWorkflow(USER, "A_B_MICO_TEST" ,A_B_MICO_TEST , "[]","[]"));
    	routeIds.put(A_B_MICO_TEST1,wManager.addWorkflow(USER, "A_B_MICO_TEST1",A_B_MICO_TEST1, "[]","[]"));
    	routeIds.put(A_B_MICO_TEST2,wManager.addWorkflow(USER, "A_B_MICO_TEST2",A_B_MICO_TEST2, "[]","[]"));
    	routeIds.put(A_C_MICO_TEST, wManager.addWorkflow(USER, "A_C_MICO_TEST" ,A_C_MICO_TEST , "[]","[]"));
    	routeIds.put(B_C_MICO_TEST, wManager.addWorkflow(USER, "B_C_MICO_TEST" ,B_C_MICO_TEST , "[]","[]"));

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
    	routeIds.put(ERROR_B_MICO_FAILING_TEST, wManager.addWorkflow(USER, "ERROR_B_MICO_FAILING_TEST" ,ERROR_B_MICO_FAILING_TEST , "[]","[]"));
    	
    	mocks.put(ERROR_B_MICO_FAILING_TEST, getMockEndpoint("mock:auto-test-route-ERROR-B-mico/error"));
    	mocks.get(ERROR_B_MICO_FAILING_TEST).setExpectedCount(1);
    	

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
    	
    	for(Integer id : routeIds.values()){
    		wManager.deleteWorkflow(id);
    	}
    	Thread.sleep(200);
    	
    	assert(wManager.listWorkflows(USER).isEmpty());
    }
    
    @SuppressWarnings("deprecation")
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
    	
    	Map<String,Integer> routeIds = new HashMap<String,Integer>();
    	
        routeIds.put(A_B_MICO_TEST, wManager.addWorkflow(USER, "A_B_MICO_TEST" ,A_B_MICO_TEST , "[]","[]"));
    	routeIds.put(A_B_MICO_TEST1,wManager.addWorkflow(USER, "A_B_MICO_TEST1",A_B_MICO_TEST1, "[]","[]"));
    	routeIds.put(A_B_MICO_TEST2,wManager.addWorkflow(USER, "A_B_MICO_TEST2",A_B_MICO_TEST2, "[]","[]"));
    	routeIds.put(A_C_MICO_TEST, wManager.addWorkflow(USER, "A_C_MICO_TEST" ,A_C_MICO_TEST , "[]","[]"));
    	routeIds.put(B_C_MICO_TEST, wManager.addWorkflow(USER, "B_C_MICO_TEST" ,B_C_MICO_TEST , "[]","[]"));

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
    	routeIds.put(ERROR_B_MICO_FAILING_TEST, wManager.addWorkflow(USER, "ERROR_B_MICO_FAILING_TEST" ,ERROR_B_MICO_FAILING_TEST , "[]","[]"));
    	
    	mocks.put(ERROR_B_MICO_FAILING_TEST, getMockEndpoint("mock:auto-test-route-ERROR-B-mico/error"));
    	mocks.get(ERROR_B_MICO_FAILING_TEST).setExpectedCount(1);
    	

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
    	
    	for(Integer id : routeIds.values()){
    		wManager.deleteWorkflow(id);
    	}
    	assert(wManager.listWorkflows(USER).isEmpty());
    	
    }
    
    private Response triggerRouteWithSimpleItem(String syntacticType,String mimeType,Integer routeId) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             item.setSemanticType(syntacticType+" over "+mimeType);
             item.setSyntacticalType(syntacticType);
             item.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), routeId);
             
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
    
    private Response triggerRouteWithComplexItem(String syntacticType,String mimeType,Integer routeId) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             
             Part part =item.createPart(new URIImpl("urn:org.example.injection"));
             part.setSemanticType(syntacticType+" over "+mimeType);
             part.setSyntacticalType(syntacticType);
             part.getAsset().setFormat(mimeType);

             Response r = injService.submitItem(item.getURI().stringValue(), routeId);
             
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
