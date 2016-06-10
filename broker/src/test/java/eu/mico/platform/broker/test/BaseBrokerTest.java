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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Marcel Sieland (marcel.sieland@fraunhofer.idmt.de)
 */
public abstract class BaseBrokerTest {

    private static Logger log = LoggerFactory.getLogger(BaseBrokerTest.class);

    protected static String testHost;

    protected static EventManager eventManager;
    protected static MICOBroker broker;
    private static Connection connection;
    protected Channel channel;

    protected static String amqpHost;
    protected static String amqpUsr;
    protected static String amqpPwd;
    protected static String amqpVHost;

    private static JettyMarmotta marmotta;
    private static String marmottaBaseUrl;
    private static Path storageDir;
    private static String storageBaseUri;
    private static int rabbitPort;

	private static String registrationBaseUrl;
	protected static boolean isRegistrationServiceAvailable = false;

    @BeforeClass
    public static void setupBase() throws URISyntaxException, IOException,
            RDFParseException, RepositoryException {
        testHost = System.getProperty("test.host");
        if (testHost == null) {
            testHost = "127.0.0.1";
            log.warn(
                    "test.host environment variable not defined, using default of {}",
                    testHost);
        } else {
            log.info("test.host environment variable set to: {}", testHost);
        }

        amqpHost = getConf("amqp.host", testHost);
        amqpVHost = getConf("amqp.vhost", "/");
        amqpUsr = getConf("amqp.usr", "mico");
        amqpPwd = getConf("amqp.pwd", "mico", false); // to not log the pwd
        rabbitPort = Integer.parseInt(getConf("rabbitPort", "5672"));


        // we need a Marmotta instance for testing the MicoBrokerImpl
        if (marmotta == null) {
            marmotta = new JettyMarmotta("/marmotta", 8088
                    );
            marmottaBaseUrl = "http://localhost:8088/marmotta";

            storageDir = Files.createTempDirectory("mico-event-manager-test");
            storageBaseUri = storageDir.toUri().toString();
            
            registrationBaseUrl = getConf("reg-api.base", "http://localhost:8030/");

            broker = new MICOBrokerImpl(testHost, amqpVHost, amqpUsr, amqpPwd,
                    rabbitPort, marmottaBaseUrl, storageBaseUri,registrationBaseUrl);
        }

        // Now initialize RabbitMQ
        ConnectionFactory testFactory = new ConnectionFactory();
        testFactory.setHost(amqpHost);
        testFactory.setVirtualHost(amqpVHost);
        testFactory.setUsername(amqpUsr);
        testFactory.setPassword(amqpPwd);

        connection = testFactory.newConnection();
        
        //retrieve the status of the registration service
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
    }

    @AfterClass
    public static void teardownBase() throws IOException {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    @Before
    public void setupTestBase() throws IOException, URISyntaxException,
            TimeoutException {
        eventManager = new EventManagerImpl(amqpHost, amqpUsr, amqpPwd,
                amqpVHost);
        eventManager.init();

        channel = connection.createChannel();
    }

    @After
    public void teardownTestBase() throws IOException {
        eventManager.shutdown();

        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
    
    // ------------------------ HELPER UTILITIES -------------------- //
    
    
    
    protected void connectExtractor(MockService s) throws InterruptedException, IOException{
    	eventManager.registerService(s);
        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }
    }
    
    protected void disconnectExtractor(MockService s) throws InterruptedException, IOException{
    	eventManager.unregisterService(s);
        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }
    }
	
	protected void setupMockAnalyser(String source, String target)
            throws IOException, InterruptedException {
        setupMockAnalyser(source, target,false);
    }
    
    protected void setupMockAnalyser(String source, String target, boolean createPart)
            throws IOException, InterruptedException {
        connectExtractor(new MockService(source, target, createPart));
    }
    
    protected void teardownMockAnalyser(MockService s)
            throws IOException, InterruptedException {
        disconnectExtractor(s);
        s=null;
    }

    protected static class MockService implements AnalysisService {

        private boolean createAsset = false;
        private String source, target;

        public MockService(String source, String target) {
            this(source,target,false);
        }

        public MockService(String source, String target, boolean createAsset) {
            this.source = source;
            this.target = target;
            this.createAsset = createAsset;
        }

        @Override
        public URI getServiceID() {
            return new URIImpl("urn:org.example.services-1.0.0-"
                    + StringUtils.capitalize(source)
                    + StringUtils.capitalize(target) + "Service");
        }

        @Override
        public String getProvides() {
            return target;
        }

        @Override
        public String getRequires() {
            return source;
        }

        @Override
        public String getQueueName() {
            return getExtractorID()+"-"+getExtractorVersion() + "-" + getExtractorModeID();
        }

        @Override
        public void call(
                AnalysisResponse resp,
                Item item,
                java.util.List<eu.mico.platform.persistence.model.Resource> resourceList,
                java.util.Map<String, String> params) throws AnalysisException,
                IOException {
            log.info("mock analysis request for [{}] on queue {}",
                    resourceList.get(0).getURI(), getQueueName());
            Part c = null;
            try {
                c = item.createPart(getServiceID());
                c.setSemanticType(getProvides());
                c.setSyntacticalType(getProvides());
                for (Resource r :resourceList){
                    c.addInput(r);
                }
                if (createAsset){
                    Asset asset = c.getAsset();
                    asset.setFormat("text/plain");
                    asset.getOutputStream().write(getServiceID().stringValue().getBytes("UTF-8"));
                }

                resp.sendNew(item, c.getURI());
            } catch (RepositoryException e) {
                throw new AnalysisException("could not access triple store");
            }

        }

		@Override
		public String getExtractorID() {
			return "urn:org.example.services";
		}

		@Override
		public String getExtractorModeID() {
			return source+target+"Service";
		}

		@Override
		public String getExtractorVersion() {
			return "1.0.0";
		}

    }

    private static String getConf(String var, String defVal) {
        return getConf(var, defVal, true);
    }

    private static String getConf(String var, String defVal, boolean logVal) {
        String val = System.getenv(var);
        if (val == null) {
            val = System.getProperty(var);
            if (val == null) {
                val = defVal;
                log.warn(
                        "{} variable not defined, falling back to default one: {}",
                        var, defVal);
            } else {
                log.info(" - {}: {} (from system property)", var, logVal ? val
                        : "< not logged >");
            }
        } else {
            log.info(" - {}: {} (from ENV param)", var, logVal ? val
                    : "< not logged >");
        }
        return val;
    }

    protected static void unregisterExtractor(MockService s) throws ClientProtocolException, IOException{
		Assume.assumeTrue(isRegistrationServiceAvailable);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDelete httpDelete = new HttpDelete(((MICOBrokerImpl)broker).getRegistrationBaseUri()+"/delete/extractor/"+s.getExtractorID());
		httpDelete.setHeader("Accept", "application/json");
		httpDelete.setHeader("Content-type", "application/json");
		CloseableHttpResponse response = httpclient.execute(httpDelete);
		response.close();
	}
	
    
    
    // ------------------------ HELPER UTILITIES FOR VERSION 3 -------------------- //
    
	protected static void registerExtractor(MockService s, String mimeType) throws ClientProtocolException, IOException{
		
		Assume.assumeTrue(isRegistrationServiceAvailable);
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
		  "<version>"+s.getExtractorVersion()+"</version>"+
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
