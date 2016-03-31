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
package eu.mico.platform.event.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hdfs.protocol.proto.DatanodeProtocolProtos.ErrorReportRequestProto.ErrorCode;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.sparql.webservices.SparqlWebService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.anno4j.model.namespaces.OADM;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisEvent.Error;
import eu.mico.platform.event.model.Event.AnalysisEvent.Finish;
import eu.mico.platform.event.model.Event.AnalysisEvent.NewPart;
import eu.mico.platform.event.model.Event.AnalysisRequest;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;
import eu.mico.platform.storage.api.StorageService;

/**
 * Tests the {@link EventManagerImpl}. 
 * <p>
 * Mocks an Broker to validate that
 * the {@link EventManager} correctly consumes and sends events. Runs an
 * embedded Marmotta instance and a File Storage provide on a tmp directory
 * to provide a fully functional {@link PersistenceService}.
 * 
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Rupert Westenthaler (rwesten@apache.org)
 */
public class EventManagerTest extends BaseCommunicationTest {

    private static final Logger log = LoggerFactory.getLogger(EventManagerTest.class);
    
    private static Connection connection;
    private static MockBrokerRegisterEvents brokerRegister;
    private static MockBrokerConfigEvents brokerConfig;
    private static Channel registrationChannel;
    private static Channel configChannel;
    
    private static JettyMarmotta marmotta;
    
    private static Path storageDir;
    private static StorageService storage;
    
    protected static String marmottaBaseUrl;
    protected static PersistenceService persistenceService;
    
    protected static String storageBaseUri;
    private EventManager eventMgr;
    private AnalysisServiceMock service;

    private Item item;

    @BeforeClass
    public static void setupLocal() throws IOException, RepositoryConfigException, RepositoryException {
        //we need a Marmotta instance for testing the EventManagerImpl
        marmotta = new JettyMarmotta("/marmotta", 8088, SparqlWebService.class);
        marmottaBaseUrl = "http://localhost:8088/marmotta";
        
        storageDir = Files.createTempDirectory("mico-event-manager-test");
        storageBaseUri = storageDir.toUri().toString();
        
        //and a PersistenceService for creating test items
        persistenceService = new PersistenceServiceAnno4j(java.net.URI.create(marmottaBaseUrl), 
                java.net.URI.create(storageBaseUri));
        
        //Now initialize RabbitMQ
        ConnectionFactory testFactory = new ConnectionFactory();
        testFactory.setHost(amqpHost);
        testFactory.setUsername(amqpUsr);
        testFactory.setPassword(amqpPwd);

        connection = testFactory.newConnection();

        Channel initChannel = connection.createChannel();
        try {
            // create the exchange in case it does not exist
            initChannel.exchangeDeclare(EventManager.EXCHANGE_SERVICE_REGISTRY, "fanout", true);
            // create the exchange in case it does not exist
            initChannel.exchangeDeclare(EventManager.EXCHANGE_SERVICE_DISCOVERY, "fanout", true);
            // create the input and output queue with a defined name
            initChannel.queueDeclare(EventManager.QUEUE_CONTENT_INPUT, true, false, false, null);
            initChannel.queueDeclare(EventManager.QUEUE_PART_OUTPUT, true, false, false, null);
            // create the configuration queue with a defined name
            initChannel.queueDeclare(EventManager.QUEUE_CONFIG_REQUEST, false, true, false, null);
        } finally {
            initChannel.close();
        }
        
        registrationChannel = connection.createChannel();
        brokerRegister = new MockBrokerRegisterEvents(registrationChannel);
        configChannel = connection.createChannel();
        brokerConfig = new MockBrokerConfigEvents(configChannel);
    }



    @AfterClass
    public static void teardownLocal() throws IOException {
        if(registrationChannel != null){
            registrationChannel.close();
        }
        if(configChannel != null){
            configChannel.close();
        }
        if(connection != null){
            connection.close();
        }
        if(marmotta != null){
            marmotta.shutdown();
        }
        if(storageDir != null){
            FileUtils.deleteDirectory(storageDir.toFile());
        }
        
    }

    @Before
    public void createEventManager() throws IOException, TimeoutException, URISyntaxException{
        eventMgr = new EventManagerImpl(amqpHost, amqpUsr, amqpPwd, amqpVHost);
        eventMgr.init();
    }

    @After
    public void cleanTestData() throws IOException, RepositoryException {
        if(service != null){
            eventMgr.unregisterService(service);
            service = null;
        }
        if(item != null){
            persistenceService.deleteItem(item.getURI());
            item = null;
        }
        eventMgr.shutdown();
    }
    /**
     * This tests that the {@link AnalysisService} registration works as
     * expected. NOTE: this is also (implicitly) tested by all the other tests. 
     * In other words, if this fails also all the others fail as well.
     */
    @Test
    public void testRegisterService() throws IOException, InterruptedException, URISyntaxException, TimeoutException {
        registerAndAssertService(new AnalysisServiceMock("dummy","dummy","dummy"));
    }

    /**
     * This tests the correct handling of successful analysis requests.
     */
    @Test
    public void testSuccess() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException {
        String syntacticalType = "text/plain";
        service = registerAndAssertService(
                new AnalysisServiceMock("successTest", syntacticalType, "text/turtle"));
        
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        Assert.assertTrue(itemManager.isFinished());
        Assert.assertFalse(itemManager.isError());
    }
    
    /**
     * This tests that if a {@link AnalysisService} does not send an
     * FINISh event, but also does not encounter any error the {@link EventManager}
     * sends the FINISH instead
     */
    @Test
    public void testAutoFinalish() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException {
        String syntacticalType = "text/plain";
        service = registerAndAssertService(
                new AnalysisServiceMock("successTest", syntacticalType, "text/turtle"){
                    @Override
                    protected void internalCall(AnalysisResponse resp, Item ci,
                            List<Resource> object, Map<String, String> params)
                                    throws AnalysisException, IOException,
                                    RepositoryException {
                        //this lazy developer does not care about response events
                    }
                });
        
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        Assert.assertTrue(itemManager.isFinished());
        Assert.assertFalse(itemManager.isError());
    }
    
    /**
     * Tests that {@link AnalysisException}s are correctly handled by the {@link EventManager}.
     * This also ensures that {@link ErrorCodes} and messages attached to the
     * {@link AnalysisException} are correctly copied to the ERROR message
     */
    @Test
    public void testAnalysisException() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException {
        String syntacticalType = "text/plain";
        final String errorMsg = "Simulated Analysis Error";
        final ErrorCodes errorCode = ErrorCodes.UNSUPPORTED_CONTENT_VARIANT;
        service = registerAndAssertService(
                new AnalysisServiceMock("analysisExceptionTest", syntacticalType, "text/turtle"){
                    @Override
                    protected void internalCall(AnalysisResponse resp,Item ci, List<Resource> object, Map<String, String> params) throws AnalysisException, IOException, RepositoryException {
                        throw new AnalysisException(errorCode, errorMsg, null);
                    }
                });
        //create a Item with a text/plain content
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        Assert.assertTrue(itemManager.isError());
        Assert.assertFalse(itemManager.isFinished());
        //check if we preserve the original error codes
        Assert.assertEquals(errorCode, itemManager.getError().getErrorCode());
        Assert.assertEquals(errorMsg, itemManager.getError().getMessage());
    }
    
    /**
     * Tests that runtime exceptions are correctly handled by the {@link EventManager}
     */
    @Test
    public void testRuntimeException() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException {
        String syntacticalType = "text/plain";
        final String errorMsg = "Oh no an illegal state. Good this is only a test!";
        final ErrorCodes errorCode = ErrorCodes.UNEXPECTED_ERROR; //the code for unmapped errors
        service = registerAndAssertService(
                new AnalysisServiceMock("analysisExceptionTest", syntacticalType, "text/turtle"){
                    @Override
                    protected void internalCall(AnalysisResponse resp,Item ci, List<Resource> object, Map<String, String> params) throws AnalysisException, IOException, RepositoryException {
                        throw new IllegalStateException(errorMsg);
                    }
                });
        //create a Item with a text/plain content
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        Assert.assertTrue(itemManager.isError());
        Assert.assertFalse(itemManager.isFinished());
        //check if we preserve the original error codes
        Assert.assertEquals(errorCode, itemManager.getError().getErrorCode());
        Assert.assertTrue(itemManager.getError().getMessage().indexOf(errorMsg) >= 0);
    }
    
    /**
     * This tests that in case of a recoverable exception (in this case a
     * {@link RepositoryException}) the message is re-enqueued and sent a 
     * 2nd time by the messaging service. This tests completes successfully
     * on the 2nd try.
     * <p>
     * Note the recoverable exceptions are only re-enqueued if no NEW_ITEM,
     * FINISHED or ERROR was sent to the broker. This is tested by the
     * {@link #testExceptionAfterItemCreation()}.
     */
    @Test
    public void testRepositoryExceptionRecovery() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException {
        String syntacticalType = "text/plain";
        service = registerAndAssertService(
                new AnalysisServiceMock("analysisExceptionTest", syntacticalType, "text/turtle"){
                    @Override
                    protected void internalCall(AnalysisResponse resp,Item ci, List<Resource> object, Map<String, String> params) throws AnalysisException, IOException, RepositoryException {
                        if(getCallCount() == 1){ //on the first call send a repository exception
                            throw new RepositoryException("First try RepositoryException");
                        } else { //on the expected re-try we succeed
                            super.internalCall(resp, ci, object, params);
                        }
                    }
                });
        //create a Item with a text/plain content
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        Assert.assertEquals(service.getCallCount(), 2); //first unsuccessful, second succeed
        
        Assert.assertTrue(itemManager.isFinished());
        Assert.assertFalse(itemManager.isError());
    }

    /**
     * This tests the required behavior that the EventManager MUST NOT re-enqueue
     * an message after an NEW_ITEM message was sent to the broker.
     * @see #testRepositoryExceptionRecovery() this test asserts that messages
     * of recoverable exceptions are re-enqueued if no message was yet sent to
     * the broker!
     */
    @Test
    public void testExceptionAfterItemCreation() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException, RDFHandlerException {
        String syntacticalType = "text/plain";
        final String semanticPartType = "ExceptionAfterItemCreationTest";
        final String syntacticalPartType = "text/turtle";
        service = registerAndAssertService(
                new AnalysisServiceMock("analysisExceptionTest", syntacticalType, syntacticalPartType){
                    @Override
                    protected void internalCall(AnalysisResponse resp, Item item, List<Resource> object, Map<String, String> params) throws AnalysisException, IOException, RepositoryException {
                        Part part = item.createPart(getServiceID());
                        part.setSemanticType(semanticPartType);
                        part.setSyntacticalType(syntacticalPartType);
                        resp.sendNew(item, part.getURI());
//                        try {
//                            debugRDF(item);
//                        } catch (RDFHandlerException e) {log.warn(e.getMessage(),e);}
                        throw new RepositoryException("Recoverable Exception after Part createion");
                    }
                });
        //create a Item with a text/plain content
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        //Assert this is an error
        Assert.assertFalse(itemManager.isFinished());
        Assert.assertTrue(itemManager.isError());
        
        //but we should have a new part
        Assert.assertEquals(1, itemManager.getNewParts().size());
        URI newPartUri = itemManager.getNewParts().get(0);
        //debugRDF(item);
        //get the part created before the exception
        Part part = item.getPart(newPartUri);
        //check it is present and the expected syntactic and semantic type
        Assert.assertNotNull(part); 
        Assert.assertEquals(syntacticalPartType, part.getSyntacticalType());
        Assert.assertEquals(semanticPartType, part.getSemanticType());
    }
    
    /**
     * This tests that RDF statements written by an {@link AnalysisService} are
     * not written to the repository in case of an Error
     */
    @Test
    public void testRollback() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException, RDFHandlerException {
        String syntacticalType = "text/plain";
        final String semanticPartType = "ExceptionAfterItemCreationTest";
        final String syntacticalPartType = "text/turtle";
        service = registerAndAssertService(
                new AnalysisServiceMock("analysisExceptionTest", syntacticalType, syntacticalPartType){
                    @Override
                    protected void internalCall(AnalysisResponse resp, Item item, List<Resource> object, Map<String, String> params) throws AnalysisException, IOException, RepositoryException {
                        Part part = item.createPart(getServiceID());
                        part.setSemanticType(semanticPartType);
                        part.setSyntacticalType(syntacticalPartType);
                        throw new AnalysisException(ErrorCodes.UNEXPECTED_ERROR, "Exception to test rollback", null);
                    }
                });
        //create a Item with a text/plain content
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(5000); //5sec
        
        //Assert this is an error
        Assert.assertFalse(itemManager.isFinished());
        Assert.assertTrue(itemManager.isError());
        
        //Assert that this Item does not have any parts!
        Iterable<? extends Part> parts = item.getParts();
        Assert.assertFalse(parts.iterator().hasNext());
    }
    
    /**
     * This tests that RDF statements written by an {@link AnalysisService} are
     * not written to the repository in case of an Error
     */
    @Test
    public void testProgress() throws IOException, InterruptedException, URISyntaxException, TimeoutException, RepositoryException, RDFHandlerException {
        String syntacticalType = "text/plain";
        final String syntacticalPartType = "text/turtle";
        final float[] progressSteps = new float[]{0.1f,0.34f,0.51f,0.78f,0.98f,0.99f,1f};
        final boolean[] longSteps = new boolean[]{true,false,true, true, false,false};
        Assert.assertEquals(progressSteps.length, longSteps.length+1); //wrong test configuration?
        service = registerAndAssertService(
                new AnalysisServiceMock("testProgress", syntacticalType, syntacticalPartType){
                    @Override
                    protected void internalCall(AnalysisResponse resp, Item item, List<Resource> object, Map<String, String> params) throws AnalysisException, IOException, RepositoryException {
                        for(int i=0; i < progressSteps.length; i++){
                            resp.sendProgress(item,item.getURI(), progressSteps[i]);
                            boolean longStep = i < longSteps.length && longSteps[i];
                            if(longStep){
                                synchronized(this){ //wait a bit to have a real progress
                                    try {
                                        //we need to wait for more as 1000ms as shorter
                                        //steps will be suppressed
                                        this.wait(1042); 
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();  // set interrupt flag
                                    }
                                }
                            }
                        }
                        //super will send a finish event
                        super.internalCall(resp, item, object, params);
                    }
                });
        //create a Item with a text/plain content
        item = persistenceService.createItem();
        Asset asset = item.getAsset();
        item.setSyntacticalType(syntacticalType);
        asset.setFormat(syntacticalType);
        OutputStream out = asset.getOutputStream();
        IOUtils.write("This is just some dummy text", out, "UTF-8");
        IOUtils.closeQuietly(out);
        
        BasicItemManager itemManager = new BasicItemManager(service, item);
        itemManager.processAndWait(10000); //10sec ... because we test a longer progress
        
        //Assert this is an success
        Assert.assertTrue(itemManager.isFinished());
        Assert.assertFalse(itemManager.isError());
        
        //Assert the progress events
        Assert.assertFalse(itemManager.getProgresses().isEmpty());
        
        Iterator<Float> receivedSteps = itemManager.getProgresses().iterator();
        for(int i=0;i < progressSteps.length;i++){
            if(i== 0 || longSteps[i-1]){
                Assert.assertTrue(receivedSteps.hasNext());
                Float receivedStep = receivedSteps.next();
                Assert.assertEquals(progressSteps[i], receivedStep.floatValue(), 0.001f);
            }
        }
    }
    
    private AnalysisServiceMock registerAndAssertService(AnalysisServiceMock mock)
            throws IOException, InterruptedException {
        eventMgr.registerService(mock);
        // give the queue some time and then test for registration success
        synchronized (brokerRegister) {
            brokerRegister.wait(500);
        }
        Assert.assertEquals(mock.getServiceID().stringValue(), brokerRegister.lastService);
        return mock;
    }
    
    private void debugRDF(Item item) throws RepositoryException, RDFHandlerException {
        if(!log.isDebugEnabled()){
            return;
        }
        //we copy all statements to a TreeModel as this one sorts them by SPO
        //what results in a much nicer TURTLE serialization
        final Model model = new TreeModel();
        //we also set commonly used namespaces
        model.setNamespace(OADM.PREFIX, OADM.NS);
        model.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
        model.setNamespace(RDFS.PREFIX, RDF.NAMESPACE);
        model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNamespace(MMM.PREFIX, MMM.NS);
        model.setNamespace("test", "http://localhost/mem/");
        model.setNamespace("services", "http://www.mico-project.eu/services/");
        RepositoryConnection con = item.getObjectConnection();
        con.exportStatements(null, null, null, true, new RDFHandlerBase(){
            @Override
            public void handleStatement(Statement st) {
                //log.debug("{},{},{},{}",st.getSubject(),st.getPredicate(),st.getObject(),st.getContext());
                model.add(st);
            }
        });
        
        StringWriter writer = new StringWriter();
        writer.append("--- START generated RDF ---\n");
        PrintWriter out = new PrintWriter(writer);
        Rio.write(model, out, RDFFormat.TURTLE);
        writer.append("\n--- END generated RDF ---");
        log.debug(writer.toString());
    }
    /**
     * Basic ItemManager that send an {@link AnalysisRequest} event to the
     * input queue of the parsed {@link AnalysisService} and listens for
     * response Events. Received responses are asserted and logged.
     * <p>
     * In an real system this functionality is expected to be implemented by
     * the Broker
     * 
     * @author Rupert Westenthaler
     *
     */
    private class BasicItemManager extends DefaultConsumer{

        private final Logger log = LoggerFactory.getLogger(getClass());
        
        private final AnalysisService service;
        private final Item item;
        private final String queue;
        private final String queueTag;
        private final String correlationId;
        
        private boolean isFinished;
        private Error error;
        private final List<Float> progresses = new LinkedList<>();
        private final List<URI> newParts = new LinkedList<>();


        public BasicItemManager(AnalysisService service, Item item) throws IOException {
            super(connection.createChannel());
            this.service = service;
            this.item = item;
            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            queueTag = getChannel().basicConsume(queue, false, this);
            correlationId = UUID.randomUUID().toString();
        }
        
        public void processAndWait(int maxTime) throws InterruptedException, IOException {
            log.debug("> process Item {} with {}", item.getURI(), service);
            AMQP.BasicProperties ciProps = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(queue)
                    .build();
            
            Event.AnalysisRequest analysisEvent = Event.AnalysisRequest.newBuilder()
                    .setItemUri(item.getURI().toString())
                    .addPartUri(item.getURI().stringValue())
                    .setServiceId(service.getServiceID().toString()).build();
            getChannel().basicPublish("", service.getQueueName(), ciProps, analysisEvent.toByteArray());
            log.trace(" - sent analysis request for Item {} to {}", item.getURI(), service);
            long start = System.currentTimeMillis();
            synchronized (this) {
                if(maxTime <= 0){
                    log.trace(" - waiting for responses", maxTime);
                    this.wait();
                } else {
                    log.trace(" - waiting max {}ms for responses", maxTime);
                    this.wait(maxTime);
                }
            }
            Assert.assertTrue("No ERROR or FINISHED response for Service "
                    + service.getServiceID() + " and Item "+ item.getURI() 
                    + "within "+maxTime+"ms!", isError() || isFinished());
            log.debug(" - analysis request for Item {} to {} finished after {}ms", 
                    item.getURI(), service, System.currentTimeMillis() - start);
            //wait some time before we return to give ACK messages time to
            //arrive and the EventManager time to complete finally code before
            //the finishing test is shutting down the JVM
            synchronized (this) {
                //maxTime <= o MUST ONLY be used when a Human want to check things
                //in the Debugger
                if(maxTime <= 0){ 
                    this.wait();
                } else {
                    this.wait(maxTime/10);
                }
            }
        }
        
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                BasicProperties properties, byte[] body) throws IOException {
            if(log.isTraceEnabled()){
                log.trace(" - received Msg Bytes: {}", DatatypeConverter.printHexBinary(body));
            }
            Event.AnalysisEvent analysisResponse = Event.AnalysisEvent.parseFrom(body);
            //ack that we received this message
            getChannel().basicAck(envelope.getDeliveryTag(), false);
            //assert the message
            log.debug(" - received {} for Item {}", analysisResponse.getType(), item.getURI());
            Assert.assertEquals("Worng correlationId", correlationId, properties.getCorrelationId());
            Assert.assertFalse("Unexpected Message after FINISHED was receivend", isFinished());
            Assert.assertFalse("Unexpected Message after ERROR was receivend", isError());
            try {
                switch (analysisResponse.getType()) {
                    case ERROR:
                        error = analysisResponse.getError();
                        Assert.assertEquals("Expected ERROR response for Item "
                                + item.getURI() +" but received one for " + error.getItemUri() + "!",
                                item.getURI().stringValue(), error.getItemUri());
                        break;
                    case NEW_PART:
                        NewPart part = analysisResponse.getNew();
                        Assert.assertEquals("Expected NEW_PART response for Item "
                                + item.getURI() +" but received one for " + part.getItemUri() + "!",
                                item.getURI().stringValue(), part.getItemUri());
                        Assert.assertNotNull(part.getPartUri());
                        log.debug(" - part URI: {}", part.getPartUri());
                        newParts.add(new URIImpl(part.getPartUri()));
                        break;
                    case FINISH:
                        isFinished = true;
                        Finish finish = analysisResponse.getFinish();
                        Assert.assertEquals("Expected FINISH response for Item "
                                + item.getURI() +" but received one for " + finish.getItemUri() + "!",
                                item.getURI().stringValue(), finish.getItemUri());
                        break;
                    case PROGRESS:
                        Event.AnalysisEvent.Progress progress = analysisResponse.getProgress();
                        Assert.assertEquals("Expected PROGRESS response for Item "
                                + item.getURI() +" but received one for " + progress.getItemUri() + "!",
                                item.getURI().stringValue(), progress.getItemUri());
                        Assert.assertTrue("Reported Provress " + progress.getProgress() 
                                + " is out of expected range [0..1]",
                                progress.getProgress() >= 0f && progress.getProgress() <= 1f);
                        log.debug(" - progress: {}", progress.getProgress());
                        this.progresses.add(progress.getProgress());
                        break;
                     default:
                        Assert.fail("Unkown AnalysisResponse Type '" + analysisResponse.getType() + "'!");
                }
            } finally {
                if(isFinished() || isError()){
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
        }
        
        public boolean isError() {
            return error != null;
        }
        
        public Error getError(){
            return error;
        }
        
        public boolean isFinished() {
            return isFinished;
        }
        public List<URI> getNewParts() {
            return newParts;
        }
        public List<Float> getProgresses() {
            return progresses;
        }
        
    }
    

    private static class AnalysisServiceMock implements AnalysisService {

        private final String queueName;
        private final URI serviceId;
        private final String provides;
        private final String requires;
        private int called = 0;

        public AnalysisServiceMock(String queueName, String requires, String provides) {
            this.queueName = queueName;
            this.provides = provides;
            this.requires = requires;
            this.serviceId =  new URIImpl("http://example.org/services/"+queueName);
        }

        @Override
        public final URI getServiceID() {
            return serviceId;
        }

        @Override
        public final String getProvides() {
            return provides;
        }

        @Override
        public final String getRequires() {
            return requires;
        }

        @Override
        public final String getQueueName() {
            return queueName;
        }

        @Override
        public final void call(AnalysisResponse resp, Item ci, List<Resource> object, Map<String,String> params) throws AnalysisException, IOException, RepositoryException {
            called++;
            Assert.assertNotNull(resp);
            Assert.assertNotNull(ci);
            Assert.assertNotNull(object);
            Assert.assertFalse(object.isEmpty());
            Assert.assertNotNull(params);
            internalCall(resp, ci, object, params);
        }
        /**
         * internal method {@link #call(AnalysisResponse, Item, List, Map)} requests
         * are forwarded after counting and parameter validation. The default
         * implementation will call {@link AnalysisResponse#sendFinish(Item)}.
         * Can be overridden to mock analysis services with different behaviors.
         * @param resp
         * @param ci
         * @param object
         * @param params
         * @throws AnalysisException
         * @throws IOException
         * @throws RepositoryException
         */
        protected void internalCall(AnalysisResponse resp, Item ci, List<Resource> object, Map<String,String> params) throws AnalysisException, IOException, RepositoryException {
            resp.sendFinish(ci);
        }
        
        public boolean wasCalled(){
            return called > 0;
        }
        
        public int getCallCount(){
            return called;
        }
        
        @Override
        public String toString() {
            return "AnalysisService[id="+serviceId+" | queue="+queueName+"]";
        }
        
    }

    // a mock message broker just recording service registry events to test if they worked
    private static class MockBrokerRegisterEvents extends DefaultConsumer {

        private String lastService;

        public MockBrokerRegisterEvents(Channel channel) throws IOException {
            super(channel);

            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EventManager.EXCHANGE_SERVICE_REGISTRY, "");
            channel.basicConsume(queueName, true, this);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Event.RegistrationEvent registrationEvent = Event.RegistrationEvent.parseFrom(body);

            lastService = registrationEvent.getServiceId();

            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    // a mock config broker waiting for config requests and sending back fake responses
    private static class MockBrokerConfigEvents extends DefaultConsumer {
        public MockBrokerConfigEvents(Channel channel) throws IOException {
            super(channel);

            channel.queueDeclare(EventManager.QUEUE_CONFIG_REQUEST, false, true, false, null);
            channel.basicConsume(EventManager.QUEUE_CONFIG_REQUEST, false, this);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Event.ConfigurationDiscoverEvent configDiscover = Event.ConfigurationDiscoverEvent.parseFrom(body);

            // construct reply properties, use the same correlation ID as in the request
            final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();

            // construct configuration event
            Event.ConfigurationEvent config = Event.ConfigurationEvent.newBuilder()
                    .setMarmottaBaseUri(marmottaBaseUrl)
                    .setStorageBaseUri(storageBaseUri)
                    .build();

            // send configuration
            getChannel().basicPublish("", properties.getReplyTo(), replyProps, config.toByteArray());

            // ack request
            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }
    }
}
