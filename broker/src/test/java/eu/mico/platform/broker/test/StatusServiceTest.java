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

import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.model.MICOJob;
import eu.mico.platform.broker.model.MICOJobStatus;
import eu.mico.platform.broker.test.InjectionServiceTest.MockFailingServiceInjTest;
import eu.mico.platform.broker.test.InjectionServiceTest.MockServiceInjTest;
import eu.mico.platform.broker.webservices.InjectionWebService;
import eu.mico.platform.broker.webservices.StatusWebService;
import eu.mico.platform.broker.webservices.WorkflowManagementService;
import eu.mico.platform.camel.MicoCamelContext;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.NotFoundException;


/**
 * Minimal test. Inject items and see if their statuses are correctly retrieven
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StatusServiceTest extends BaseBrokerTest {
	
	private static Logger log = LoggerFactory.getLogger(StatusServiceTest.class);
    private static MicoCamelContext context = new MicoCamelContext();
    private static Map<String,MICOCamelRoute> routes = new HashMap<String,MICOCamelRoute>();
    
    private static WorkflowManagementService wManager = null;
    private static InjectionWebService injService = null;
	private static StatusWebService statusService = null;
    private static final String USER = "SERVICE-TEST-USER-"+UUID.randomUUID().toString();
    
    private MockServiceInjTest        abFastService  = new MockServiceInjTest("A", "B");
    private MockSlowServiceInjTest    cdSlowService  = new MockSlowServiceInjTest("C", "D");
    private MockFailingServiceInjTest efWrongService = new MockFailingServiceInjTest("E","F");
    
	@BeforeClass 
	public static void init() throws IOException, TimeoutException, URISyntaxException{
		
		eventManager = new EventManagerImpl(amqpHost, amqpUsr, amqpPwd, amqpVHost);
	    eventManager.init();
		
    	context.init(broker.getPersistenceService());
    	wManager = new WorkflowManagementService(broker, context, routes);
    	injService = new InjectionWebService(broker, eventManager, context, routes);
    	statusService = new StatusWebService(broker);
    
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
    public void testStatusWithNewdInjection() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
       
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
        //1. connect extractors
    	connectExtractor(abFastService);
    	connectExtractor(cdSlowService);
    	connectExtractor(efWrongService);
    	
    	//2. register the extractors
    	registerExtractor(abFastService, "mico/fast");
    	registerExtractor(cdSlowService, "mico/slow");
    	registerExtractor(efWrongService,"mico/error");
    	
    	//publish the routes
    	String abFastRoute =WorkflowServiceTest.createTestRoute(abFastService, "mico:A-B", "mico/fast");
    	String cdSlowRoute =WorkflowServiceTest.createTestRoute(cdSlowService, "mico:C-D", "mico/slow");
    	String efWrongRoute=WorkflowServiceTest.createTestRoute(efWrongService,"mico:E-F", "mico/error");
    	
    	
    	Map<String,String> routeIds = new HashMap<String,String>();
    	routeIds.put(abFastRoute, wManager.addWorkflow(USER,  abFastRoute ));
    	routeIds.put(cdSlowRoute, wManager.addWorkflow(USER,  cdSlowRoute ));
    	routeIds.put(efWrongRoute, wManager.addWorkflow(USER, efWrongRoute ));
    	

    	List<Item> items = new ArrayList<Item>();
    	PersistenceService ps = broker.getPersistenceService();

    	//call A-B (the fast one)
    	items.add(triggerRouteWithSimpleItem("mico:A-B", "mico/fast",  routeIds.get(abFastRoute)));
    	
    	//check that its status is available and equal to DONE
    	try{
    		Thread.sleep(800);
    		Map<String,Object> proprs = statusService.getItems(items.get(0).getURI().stringValue(),false,0,0).get(0);
            
            boolean finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            boolean hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	
    	//call E-F (the failing one )
    	items.add(triggerRouteWithSimpleItem("mico:E-F", "mico/error", routeIds.get(efWrongRoute)));
    	
    	
    	
    	//check that its status is available and equal to DONE
    	try{
    		Thread.sleep(800);
    		Map<String,Object> proprs = statusService.getItems(items.get(0).getURI().stringValue(),false,0,0).get(0);
            
            boolean finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            boolean hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertTrue("The item should have one error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	
    	//call C-D (the slow one )
    	items.add(triggerRouteWithSimpleItem("mico:C-D", "mico/slow",  routeIds.get(cdSlowRoute)));
    	
    	//check that its status is available and equal to DONE
    	try{
    		Thread.sleep(800);
    		Map<String,Object> proprs = statusService.getItems(items.get(0).getURI().stringValue(),false,0,0).get(0);
            
            boolean finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            boolean hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertFalse("The item should still be in progress finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
            Thread.sleep(2000); // same as the mock 
    		proprs = statusService.getItems(items.get(0).getURI().stringValue(),false,0,0).get(0);
            
            finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	disconnectExtractor(abFastService);
    	disconnectExtractor(cdSlowService);
    	disconnectExtractor(efWrongService);
        

    }
    
    @Test
    public void testStatusBackendWithNewdInjection() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
       
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
        //1. connect extractors
    	connectExtractor(abFastService);
    	connectExtractor(cdSlowService);
    	connectExtractor(efWrongService);
    	
    	//2. register the extractors
    	registerExtractor(abFastService, "mico/fast");
    	registerExtractor(cdSlowService, "mico/slow");
    	registerExtractor(efWrongService,"mico/error");
    	
    	//publish the routes
    	String abFastRoute =WorkflowServiceTest.createTestRoute(abFastService, "mico:A-B", "mico/fast");
    	String cdSlowRoute =WorkflowServiceTest.createTestRoute(cdSlowService, "mico:C-D", "mico/slow");
    	String efWrongRoute=WorkflowServiceTest.createTestRoute(efWrongService,"mico:E-F", "mico/error");
    	
    	
    	Map<String,String> routeIds = new HashMap<String,String>();
    	routeIds.put(abFastRoute, wManager.addWorkflow(USER,  abFastRoute ));
    	routeIds.put(cdSlowRoute, wManager.addWorkflow(USER,  cdSlowRoute ));
    	routeIds.put(efWrongRoute, wManager.addWorkflow(USER, efWrongRoute ));
    	

    	List<Item> items = new ArrayList<Item>();
    	PersistenceService ps = broker.getPersistenceService();

    	//call A-B (the fast one)
    	items.add(triggerRouteWithSimpleItem("mico:A-B", "mico/fast",  routeIds.get(abFastRoute)));
    	
    	//check that its status is available and equal to DONE
    	try{
    		Thread.sleep(200);
    		MICOJobStatus state = broker.getMICOCamelJobStatus(new MICOJob(routeIds.get(abFastRoute), items.get(0).getURI().stringValue()));
            boolean finished = state.isFinished();
            boolean hasError = state.hasError();
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	
    	//call E-F (the failing one )
    	items.add(triggerRouteWithSimpleItem("mico:E-F", "mico/error", routeIds.get(efWrongRoute)));
    	
    	
    	
    	//check that its status is available and equal to DONE
    	try{
    		Thread.sleep(800);
    		MICOJobStatus state = broker.getMICOCamelJobStatus(new MICOJob(routeIds.get(efWrongRoute), items.get(0).getURI().stringValue()));
            
            boolean finished = state.isFinished();
            boolean hasError = state.hasError();
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertTrue("The item should have one error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	
    	//call C-D (the slow one )
    	items.add(triggerRouteWithSimpleItem("mico:C-D", "mico/slow",  routeIds.get(cdSlowRoute)));
    	
    	//check that its status is available and equal to DONE
    	try{
    		Thread.sleep(800);
    		MICOJobStatus state = broker.getMICOCamelJobStatus(new MICOJob(routeIds.get(cdSlowRoute), items.get(0).getURI().stringValue()));
            
            boolean finished = state.isFinished();
            boolean hasError = state.hasError();
            
            Assert.assertFalse("The item should still be in progress finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
            Thread.sleep(2000); // same as the mock 
            
            state = broker.getMICOCamelJobStatus(new MICOJob(routeIds.get(cdSlowRoute), items.get(0).getURI().stringValue()));
            
            finished = state.isFinished();
            hasError = state.hasError();
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	disconnectExtractor(abFastService);
    	disconnectExtractor(cdSlowService);
    	disconnectExtractor(efWrongService);
        

    }
    
    @Test
    public void testStatusWithOldInjection() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
       
    	
        //1. connect extractors
    	connectExtractor(abFastService);
    	connectExtractor(cdSlowService);
    	connectExtractor(efWrongService);
    	
    	//prepare the items w
    	List<Item> items = new ArrayList<Item>();
    	
    	//call A-B (the fast one)
    	PersistenceService ps = broker.getPersistenceService();
    	items.add(broadcastSimpleItem("A"));
    	
    	//check that its status is available and equal to DONE
    	try{
            Map<String, Object> proprs = getStatus(items);
            boolean finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            boolean hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	
    	//call E-F (the failing one )
    	items.add(broadcastSimpleItem("E"));
    	
    	//check that its status is available and equal to DONE
    	try{
            Map<String, Object> proprs = getStatus(items);
            
            boolean finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            boolean hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertTrue("The item should have one error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	
    	//call C-D (the slow one )
    	items.add(broadcastSimpleItem("C"));
    	
    	//check that its status is available and equal to DONE
    	try{
            Map<String, Object> proprs = getStatus(items);
            
            boolean finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            boolean hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertFalse("The item should still be in progress",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
            Thread.sleep(2000); // same as the mock 
    		proprs = statusService.getItems(items.get(0).getURI().stringValue(),false,0,0).get(0);
            
            finished = Boolean.parseBoolean(((String)proprs.get("finished")));
            hasError = Boolean.parseBoolean(((String)proprs.get("hasError")));
            
            Assert.assertTrue("The item should have finished",finished);
            Assert.assertFalse("The item should have no error reported",hasError);
            
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Assert.fail();
    	}
    	finally{
    		ps.deleteItem(items.get(0).getURI());
    		items.remove(0);
    	}
    	 
        

    }

    private Map<String, Object> getStatus(List<Item> items)
            throws InterruptedException, RepositoryException {
        Map<String,Object> proprs = null;
        for( int i =0;i<5;i++){
            Thread.sleep(i*300);
            try{
            List<Map<String, Object>> itemStats = statusService.getItems(
                    items.get(0).getURI().stringValue(), false, 0, 0);
            if (itemStats == null || itemStats.size() == 0) {
                log.debug("wait for item processing ....");
                continue;
            }
            proprs = itemStats.get(0);
            }catch(NotFoundException e){
                if(i==4){
                    e.printStackTrace();
                    Assert.fail("item injection failed");
                }else{
                    log.debug("wait for item inject to finish ....");
                }
                continue;
            }
        }
        return proprs;
    }
    
    public static class MockSlowServiceInjTest extends MockServiceInjTest{

    	public MockSlowServiceInjTest(String source, String target) {
			super(source, target);
		}
		
		public MockSlowServiceInjTest(String source, String target, boolean createAsset) {
			super(source, target,createAsset);
		}
		
		@Override
		public void call(AnalysisResponse resp,
                Item item,
                java.util.List<eu.mico.platform.persistence.model.Resource> resourceList,
                java.util.Map<String, String> params) throws AnalysisException,
                IOException {
            log.info("mock analysis SLOW request for [{}] on queue {}, sleeping for two seconds",
                    resourceList.get(0).getURI(), getQueueName());
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new AnalysisException();				
			}
            super.call(resp,item,resourceList,params);			
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

             injService.submitItem(item.getURI().stringValue(), null, null);             
             Thread.sleep(200);
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
    
    private Item triggerRouteWithSimpleItem(String syntacticType,String mimeType,String routeId) throws RepositoryException, IOException, InterruptedException
    {
    	 PersistenceService ps = broker.getPersistenceService();
         Item item = null;
         try {
             item = ps.createItem();
             item.setSemanticType(syntacticType+" over "+mimeType);
             item.setSyntacticalType(syntacticType);
             item.getAsset().setFormat(mimeType);

             injService.submitItem(item.getURI().stringValue(), routeId, null);             
             Thread.sleep(500);
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
    
}
