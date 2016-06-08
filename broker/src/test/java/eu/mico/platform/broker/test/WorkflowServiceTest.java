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


import eu.mico.platform.broker.impl.MICOBrokerImpl.RouteStatus;
import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.webservices.WorkflowManagementService;
import eu.mico.platform.camel.MicoCamelContext;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
 public class WorkflowServiceTest extends BaseBrokerTest {
	
	private static final String ROUTE_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	                                             "<routes xmlns=\"http://camel.apache.org/schema/spring\">";
	private static final String ROUTE_END = "</routes>";
	private static final String USER="mico";
	
	private static Logger log = LoggerFactory.getLogger(WorkflowServiceTest.class);
    private static MicoCamelContext context = new MicoCamelContext();
    private static Map<Integer,MICOCamelRoute> routes = new HashMap<Integer,MICOCamelRoute>();
    private static WorkflowManagementService service = null;
    
    
    @BeforeClass
    public static void prepare() throws ClientProtocolException, IOException {
    	
    	context.init();
        service = new WorkflowManagementService(broker,context,routes);
    }

    
    // ------------------------Tests below this line -------------------- //
    
    @SuppressWarnings("deprecation")
	@Test
    public void testGetWorkflowStatus() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
    	//assert that no workflows are present
    	List<String> ids = service.listWorkflows(USER);
    	Assert.assertTrue(ids.isEmpty());
    	
    	
    	MockService abService = new MockService("A", "B");
    	String abWorkflow=createTestRoute(abService, "A", "mico/test");    	
        int newId = service.addWorkflow(USER, "tw-1",abWorkflow , "[]","[]");
        ids = service.listWorkflows(USER);
        
        Assert.assertEquals(ids.size(),1);
        Assert.assertEquals(Integer.parseInt(ids.get(0)),newId);
        
        int nonExistingId = -1;
        Assert.assertTrue(service.listWorkflows("NON_EXISTING_USER").isEmpty());        
        assertRouteStatus(RouteStatus.BROKEN,service.getStatus(USER, nonExistingId));
        assertRouteStatus(RouteStatus.BROKEN,service.getStatus(USER, newId));
        
        registerExtractor(abService,"mico/test");
        assertRouteStatus(RouteStatus.UNAVAILABLE,service.getStatus(USER, newId));
        
        connectExtractor(abService);        
        assertRouteStatus(RouteStatus.ONLINE,service.getStatus(USER, newId));
        
        disconnectExtractor(abService);
        assertRouteStatus(RouteStatus.UNAVAILABLE,service.getStatus(USER, newId));
        
        unregisterExtractor(abService);
        assertRouteStatus(RouteStatus.BROKEN,service.getStatus(USER, newId));
        
        service.deleteWorkflow(newId);
        Assert.assertTrue(service.listWorkflows(USER).isEmpty());
    }
    
    @SuppressWarnings("deprecation")
	@Test
    public void testAddRemoveWorkflows() throws RepositoryException, IOException{
    	
    	//assert that no workflows are present
    	List<String> ids = service.listWorkflows(USER);
    	Assert.assertTrue(ids.isEmpty());
    	
    	final String GUEST = "GUEST";
    	List<String> ids2 = service.listWorkflows(GUEST);
    	Assert.assertTrue(ids2.isEmpty());
    	
    	MockService abService = new MockService("A", "B");
    	
    	//add 100 workflows for user mico
    	for(int i=0; i<100; i++){
    		service.addWorkflow(USER, "tw-"+1,createTestRoute(abService,"mico:Test","test/mico") , "[]","[]");
    	}
    	
    	ids = service.listWorkflows(USER);
    	Assert.assertEquals(100,ids.size());
    	
    	
    	//add 50 workflows for user guest
    	for(int i=0; i<50; i++){
    		service.addWorkflow(GUEST, "tw-"+1,createTestRoute(abService,"mico:Test","test/mico") , "[]","[]");
    	}
    	
    	ids2 = service.listWorkflows(GUEST);
    	Assert.assertEquals(50,ids2.size());
    	
    	
    	Collections.shuffle(ids, new Random(0));
    	Collections.shuffle(ids2, new Random(0));
    	
    	try{
    	  	for(String id : ids){
        		service.getCamelRoute(Integer.parseInt(id));
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while retrieving an existing workflow for user "+USER);
    	}
    	try{
    	  	for(String id : ids2){
        		service.getCamelRoute(Integer.parseInt(id));
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while retrieving an existing workflow for user "+GUEST);
    	}
    	
    	try{
    	  	for(String id : ids){
        		service.deleteWorkflow(Integer.parseInt(id));
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while deleting an existing workflow for user "+USER);
    	}
    	
    	Assert.assertTrue(service.listWorkflows(USER).isEmpty());
    	Assert.assertEquals(ids2.size(),service.listWorkflows(GUEST).size());
    	
    	try{
    	  	for(String id : ids2){
        		service.deleteWorkflow(Integer.parseInt(id));
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while deleting an existing workflow for user "+GUEST);
    	}
    	
    	Assert.assertTrue(service.listWorkflows(GUEST).isEmpty());
    	
    		
    }
    
    
    // ------------------------ HELPER UTILITIES -------------------- //
    
    public static void assertRouteStatus(RouteStatus expected,  String retrieved){
    	String errorMessage =  "Expected route status was " + expected.toString() +
                               ", but is "+retrieved;
    	Assert.assertTrue(errorMessage,expected.toString().contentEquals(retrieved));
    }
    
    public static String createTestRoute( MockService s, String syntacticType, String mimeType){
		
		String startingPoint = "<route id='workflow-WORKFLOW_ID-starting-point-for-pipeline-0-mimeType="+mimeType+",syntacticType="+syntacticType+"'>" + "\n" +
		                       	 "<from uri='direct:workflow-WORKFLOW_ID,mimeType="+mimeType+",syntacticType="+syntacticType+"'/>" +  "\n" +
		                       	 "<to uri='direct:workflow-WORKFLOW_ID-pipeline-0'/>" + 
		                       "</route>";
		
		String pipeline = "<route id='workflow-WORKFLOW_ID-pipeline-0'>" +
					        "<from uri='direct:workflow-WORKFLOW_ID-pipeline-0'/>" + 
					        "<pipeline>" +
						      "<to uri='mico-comp:vbox1?serviceId="+s.getServiceID().stringValue()+
						                               "&amp;extractorId="+s.getExtractorID()+
						                               "&amp;extractorVersion="+s.getExtractorVersion()+
						                               "&amp;modeId="+s.getExtractorModeID()+"'/>"+ "\n" +
						      "<to uri='mock:auto-test-route-"+s.getRequires()+"-"+s.getProvides()+"-"+mimeType+"'/>"+
						    "</pipeline>"+
						  "</route>";
		
		  
		return ROUTE_PREAMBLE+startingPoint+pipeline+ROUTE_END;
	}
 
}
