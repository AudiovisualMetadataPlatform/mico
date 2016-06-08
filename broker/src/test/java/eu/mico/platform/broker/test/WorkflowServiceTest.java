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
import com.rabbitmq.client.QueueingConsumer;

import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.broker.impl.MICOBrokerImpl.ExtractorStatus;
import eu.mico.platform.broker.impl.MICOBrokerImpl.RouteStatus;
import eu.mico.platform.broker.test.BaseBrokerTest.MockService;
import eu.mico.platform.broker.webservices.WorkflowManagementService;
import eu.mico.platform.camel.MicoCamelContext;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

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
	
	private static Logger log = LoggerFactory.getLogger(CamelBrokerTest.class);
    private static MicoCamelContext context = new MicoCamelContext();
    private static WorkflowManagementService service = null;
    private static boolean isRegistrationServiceAvailable = false;
    
    @BeforeClass
    public static void prepare() throws ClientProtocolException, IOException {
    	
    	HttpGet httpGetInfo = new HttpGet(((MICOBrokerImpl)broker).getRegistrationBaseUri() + "/info");
    	
    	CloseableHttpClient httpclient = HttpClients.createDefault();    	
    	CloseableHttpResponse response = null;
    	try{
    		response = httpclient.execute(httpGetInfo);
    		int status = response.getStatusLine().getStatusCode();
        log.info("looking for registration service at {}",httpGetInfo.toString());
        	if(status == 200){
        		isRegistrationServiceAvailable = true;
        	}
    	}
    	catch(Exception e){;}
    	finally{
    		if(response!= null) response.close();
    	}
    	
    	
        context.init();
        service = new WorkflowManagementService(broker,context);
    }

    
    // ------------------------Tests below this line -------------------- //
    
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
    
    private static void assertRouteStatus(RouteStatus expected,  String retrieved){
    	String errorMessage =  "Expected route status was " + expected.toString() +
                               ", but is "+retrieved;
    	Assert.assertTrue(errorMessage,expected.toString().contentEquals(retrieved));
    }
    
    private static String createTestRoute( MockService s, String syntacticType, String mimeType){
		
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
						    "</pipeline>"+
						  "</route>";
		
		  
		return ROUTE_PREAMBLE+startingPoint+pipeline+ROUTE_END;
	}
    
    private void connectExtractor(MockService s) throws InterruptedException, IOException{
    	eventManager.registerService(s);
        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }
    }
    
    private void disconnectExtractor(MockService s) throws InterruptedException, IOException{
    	eventManager.unregisterService(s);
        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }
    }
	
	private static void unregisterExtractor(MockService s) throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDelete httpDelete = new HttpDelete(((MICOBrokerImpl)broker).getRegistrationBaseUri()+"/delete/extractor/"+s.getExtractorID());
		httpDelete.setHeader("Accept", "application/json");
		httpDelete.setHeader("Content-type", "application/json");
	    httpclient.execute(httpDelete);
	}
	
	private static void registerExtractor(MockService s, String mimeType) throws ClientProtocolException, IOException{
		
		HttpEntity entity = MultipartEntityBuilder
			    .create()
			    .addBinaryBody("file",createTestRegistrationXml(s, mimeType).getBytes() , ContentType.create("application/octet-stream"), "filename")
			    .build();
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(((MICOBrokerImpl)broker).getRegistrationBaseUri()+"/add/extractor");
		httppost.setEntity(entity);

		//Execute and get the response.
		CloseableHttpResponse response = httpclient.execute(httppost);
		int status = response.getStatusLine().getStatusCode();
		response.close();
	    Assert.assertEquals(200, status);
	}
		
	
	private static String createTestRegistrationXml( MockService s, String mimeType){
		
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	    "<extractorSpecification>"+
		  "<name>"+s.getServiceID()+"</name>"+
		  "<version>"+s.getServiceID()+"</version>"+
		  "<id>"+s.getExtractorID()+"</id>"+
		    "<mode>"+
		      "<id>"+s.getExtractorModeID()+"</id>"+
		      "<description> desc </description>"+
		      "<input>"+
		        "<semanticType><name>TestInputName</name><description>TestDescription</description></semanticType>"+
		        "<dataType><mimeType>"+mimeType+"</mimeType><syntacticType>"+s.getRequires()+"</syntacticType></dataType>"+
		      "</input>"+
		      "<output>"+
		        "<semanticType><name>TestInputName</name><description>TestDescription</description></semanticType>"+
		        "<dataType><mimeType>"+mimeType+"</mimeType><syntacticType>"+s.getProvides()+"</syntacticType></dataType>"+
		        "<location>Test Location </location>"+
		      "</output>"+
		    "</mode>"+
		  "<isSingleton>false</isSingleton>"+
		"</extractorSpecification>";		  

	}
 
}
