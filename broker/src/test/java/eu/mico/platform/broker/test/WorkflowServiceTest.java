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


import eu.mico.platform.broker.api.MICOBroker.WorkflowStatus;
import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.webservices.WorkflowManagementService;
import eu.mico.platform.camel.MicoCamelContext;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;



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
	
    private static MicoCamelContext context = new MicoCamelContext();
    private static Map<String,MICOCamelRoute> routes = new HashMap<String,MICOCamelRoute>();
    private static WorkflowManagementService service = null;
    
    
    @BeforeClass
    public static void prepare() throws ClientProtocolException, IOException {
    	
    	context.init(broker.getPersistenceService());
        service = new WorkflowManagementService(broker,context,routes);
    }

    
    // ------------------------Tests below this line -------------------- //
    

	@Test
    public void testGetWorkflowStatus() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
    	
    	Assume.assumeTrue(isRegistrationServiceAvailable);
    	
    	MockService abService = new MockService("A", "B");
    	String abWorkflow=createTestRoute(abService, "A", "mico/test");    	
        String newId = service.addWorkflow(USER, abWorkflow );
        String nonExistingId = "notExistingId";
        
        assertRouteStatus(WorkflowStatus.BROKEN,service.getStatus(USER, nonExistingId));
        assertRouteStatus(WorkflowStatus.BROKEN,service.getStatus(USER, newId));
        
        registerExtractor(abService,"mico/test");
        assertRouteStatus(WorkflowStatus.UNAVAILABLE,service.getStatus(USER, newId));
        
        connectExtractor(abService);        
        assertRouteStatus(WorkflowStatus.ONLINE,service.getStatus(USER, newId));
        
        disconnectExtractor(abService);
        assertRouteStatus(WorkflowStatus.UNAVAILABLE,service.getStatus(USER, newId));
        
        unregisterExtractor(abService);
        assertRouteStatus(WorkflowStatus.BROKEN,service.getStatus(USER, newId));
        
        service.deleteWorkflow(newId);
        assertRouteStatus(WorkflowStatus.BROKEN,service.getStatus(USER, nonExistingId));
    }
    
	@Test
	public void testReplaceExistingWorkflow() throws RepositoryException, IOException{
		
		MockService abService = new MockService("A", "B");
    	MockService bcService = new MockService("B", "C");
    	
    	String routeAB = createTestRouteWithoutId(abService,  "A", "mico/test");
    	String routeBC = createTestRouteWithoutId(bcService,  "B", "mico/test");
    	
    	service.addWorkflow(USER, routeAB);    	
    	Assert.assertTrue(service.getCamelRoute("WORKFLOW_ID").contentEquals(routeAB));
    	
    	//check that we can replace routeAB with itself without the service throwing
    	service.addWorkflow(USER, routeAB);
    	Assert.assertTrue(service.getCamelRoute("WORKFLOW_ID").contentEquals(routeAB));
    	
    	//check that we can replace routeAB with routeBC without the service throwing
    	service.addWorkflow(USER, routeBC);
    	Assert.assertFalse(service.getCamelRoute("WORKFLOW_ID").contentEquals(routeAB));
    	Assert.assertTrue(service.getCamelRoute("WORKFLOW_ID").contentEquals(routeBC));
    	
    	//check that if we replace routeBC with something not valid the service throws
    	boolean serviceHasThrown = false;
    	try{
    		service.addWorkflow(USER, ROUTE_PREAMBLE+ROUTE_END);
    	}
    	catch(IllegalArgumentException e){
    		serviceHasThrown = true;
    	}
    	Assert.assertTrue("The workflow management service should have thrown",serviceHasThrown);
    	serviceHasThrown=false;
    	
    	//and that the content is unchanged
    	Assert.assertTrue(service.getCamelRoute("WORKFLOW_ID").contentEquals(routeBC));
    	
    	//check that we are able to delete the route correctly
    	Response r = service.deleteWorkflow("WORKFLOW_ID");
    	Assert.assertEquals(Status.OK.getStatusCode(),r.getStatus());
    	
    	//and that a NOT_FOUND code is raised in case the route does not exist
    	r = service.deleteWorkflow("WORKFLOW_ID");
    	Assert.assertEquals(Status.NOT_FOUND.getStatusCode(),r.getStatus());
    	
	}

	@Test
	public void testAddRemoveWorkflows() throws RepositoryException, IOException{
    	

    	final String GUEST = "GUEST";
    	Set<String> ids = new HashSet<String>();
    	Set<String> ids2 = new HashSet<String>();
    	Assert.assertTrue(ids.isEmpty());
    	Assert.assertTrue(ids2.isEmpty());
    	
    	MockService abService = new MockService("A", "B");
    	
    	//add 100 workflows for user mico
    	for(int i=0; i<100; i++){
    		ids.add(service.addWorkflow(USER, createTestRoute(abService,"mico:Test","test/mico") ));
    	}
    	Assert.assertEquals(100,ids.size());
    	
    	
    	//add 50 workflows for user guest
    	for(int i=0; i<50; i++){
    		ids2.add(service.addWorkflow(GUEST, createTestRoute(abService,"mico:Test","test/mico") ));
    	}
    	Assert.assertEquals(50,ids2.size());
    	
    	
    	try{
    	  	for(String id : ids){
        		service.getCamelRoute(id);
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while retrieving an existing workflow for user "+USER);
    	}
    	try{
    	  	for(String id : ids2){
        		service.getCamelRoute(id);
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while retrieving an existing workflow for user "+GUEST);
    	}
    	
    	try{
    	  	for(String id : ids){
        		service.deleteWorkflow(id);
        		assertRouteStatus(WorkflowStatus.BROKEN,service.getStatus(USER, id));
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while deleting an existing workflow for user "+USER);
    	}
    	
    	try{
    	  	for(String id : ids2){
        		service.deleteWorkflow(id);
        		assertRouteStatus(WorkflowStatus.BROKEN,service.getStatus(GUEST, id));
        	}
    	}catch(Exception e) {
    		Assert.fail("Unexpected exception while deleting an existing workflow for user "+GUEST);
    	}    	
    		
    }
    
    
    // ------------------------ HELPER UTILITIES -------------------- //
    
    public static void assertRouteStatus(WorkflowStatus expected,  String retrieved){
    	String errorMessage =  "Expected route status was " + expected.toString() +
                               ", but is "+retrieved;
    	Assert.assertTrue(errorMessage,expected.toString().contentEquals(retrieved));
    }
    
    private static Integer newID = 0;
    public static String createTestRoute( MockService s, String syntacticType, String mimeType){
		
		return createTestRouteWithoutId(s,syntacticType,mimeType).replace("WORKFLOW_ID", (newID++).toString());
	}
    
    public static String createTestRouteWithoutId( MockService s, String syntacticType, String mimeType){
    	String startingPoint = "<route id='workflow-WORKFLOW_ID-starting-point-for-pipeline-0-mimeType="+mimeType+",syntacticType="+syntacticType+"'>" + "\n" +
				              	 "<from uri='direct:workflow-WORKFLOW_ID,mimeType="+mimeType+",syntacticType="+syntacticType+"'/>" +  "\n" +
				              	 "<to uri='direct:workflow-WORKFLOW_ID-pipeline-0'/>" + 
				              "</route>";

		String pipeline = "<route id='workflow-WORKFLOW_ID-pipeline-0'>" +
					        "<from uri='direct:workflow-WORKFLOW_ID-pipeline-0'/>" + 
					        "<pipeline>" +
						      "<to uri='mico-comp:vbox1?host=localhost&amp;serviceId="+s.getServiceID().stringValue()+
						                               "&amp;extractorId="+s.getExtractorID()+
						                               "&amp;extractorVersion="+s.getExtractorVersion()+
						                               "&amp;modeId="+s.getExtractorModeID()+"'/>"+ "\n" +
						      "<to uri='mock:auto-test-route-"+s.getRequires()+"-"+s.getProvides()+"-"+mimeType+"'/>"+
						    "</pipeline>"+
						  "</route>";


		return (ROUTE_PREAMBLE+startingPoint+pipeline+ROUTE_END);
    }
 
}
