package de.fraunhofer.idmt.camel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.directory.api.util.DummySSLSocketFactory;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.sparql.webservices.SparqlWebService;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import de.fraunhofer.idmt.mico.DummyExtractor;
import de.fraunhofer.idmt.mico.DummyFailingExtractor;
import de.fraunhofer.idmt.mico.DummyNoPartExtractor;
import de.fraunhofer.idmt.mico.DummyTwoPartsExtractor;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.test.mock.EventManagerMock;
import eu.mico.platform.camel.TestBase;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;
import eu.mico.platform.storage.api.StorageService;

public class MicoCamel {
    private static Logger log = LoggerFactory.getLogger(MicoCamel.class);

    private String testHost;

    protected EventManager eventManager;
    protected Connection connection;
    
    private static JettyMarmotta marmotta;
    
    private static Path storageDir;
    protected static String storageBaseUri;
    private static StorageService storage;
    
    protected static String marmottaBaseUrl;
    
    protected static PersistenceService persistenceService;
    
    private static MockBrokerRegisterEvents brokerRegister;
    private static MockBrokerConfigEvents brokerConfig;
    private static Channel registrationChannel;
    private static Channel configChannel;

    protected static AnalysisService extr1 = new DummyExtractor("A","B","mico-extractor-test","1.0.0","A-B-queue");
    protected static AnalysisService extr2 = new DummyExtractor("B","C","mico-extractor-test","1.0.0","B-C-queue");
    protected static AnalysisService extr_2 = new DummyExtractor("B", "text/plain");
    protected static AnalysisService extr_a = new DummyExtractor("A", "B");
    protected static AnalysisService extr_b = new DummyExtractor("B", "C");
    protected static AnalysisService extr_ab1 = new DummyExtractor("A", "B1","mico-extractor-test","1.0.0","A-B1");
    protected static AnalysisService extr_ab2 = new DummyExtractor("A", "B2","mico-extractor-test","1.0.0","A-B2");
    protected static AnalysisService extr_abc = new DummyExtractor("AB", "C","mico-extractor-test","1.0.0","AB-C-queue");
    protected static AnalysisService extr_c = new DummyExtractor("C1", "D","mico-extractor-test","1.0.0","C1-D-queue");
    protected static AnalysisService extr_d = new DummyExtractor("D", "E","mico-extractor-test","1.0.0","D-E-queue");
    protected static AnalysisService extr_e = new DummyExtractor("C2", "F","mico-extractor-test","1.0.0","C2-F-queue");
    protected static AnalysisService extr_error = new DummyFailingExtractor("ERROR", "ERROR","mico-extractor-test","1.0.0","ERROR-ERROR-queue");
    
    protected static AnalysisService extr_stop = new DummyNoPartExtractor("STOP", "STOP","mico-extractor-test","1.0.0","STOP-STOP-queue");
    protected static AnalysisService extr_f12 = new DummyExtractor("FINISH1", "FINISH2","mico-extractor-test","1.0.0","FINISH1-FINISH2-queue");
    protected static AnalysisService extr_f23 = new DummyExtractor("FINISH2", "FINISH3","mico-extractor-test","1.0.0","FINISH2-FINISH3-queue");
    protected static AnalysisService extr_f34 = new DummyExtractor("FINISH3", "FINISH4","mico-extractor-test","1.0.0","FINISH3-FINISH4-queue");
    protected static AnalysisService extr_aabb = new DummyTwoPartsExtractor("AA", "BB","mico-extractor-test","1.0.0","AA-BB-queue");
    protected static AnalysisService extr_bbcc = new DummyTwoPartsExtractor("BB", "CC","mico-extractor-test","1.0.0","BB-CC-queue");
    protected static AnalysisService extr_ccdd = new DummyTwoPartsExtractor("CC", "DD","mico-extractor-test","1.0.0","CC-DD-queue");
    
    public EventManager getEventManager(){
    	return eventManager;
    }

    /**
     * setup test environment including mico eventManager and some registered
     * dummy services
     * 
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    public void init() throws IOException, TimeoutException, URISyntaxException {
        testHost = System.getProperty("amqp.host");
        if (testHost == null) {
        	testHost="127.0.0.1";
            log.warn("'amqp.host' environment variable not defined, using default of {}",testHost);
        }
        String virtualHost = System.getProperty("amqp.vhost");
        if (virtualHost == null) {
        	virtualHost="/";
            log.warn("'amqp.vhost' environment variable not defined, using default of {}",virtualHost);
        }
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(testHost);
        factory.setVirtualHost(virtualHost);
        factory.setUsername("mico");
        factory.setPassword("mico");
        
        //we need a Marmotta instance for the EventManagerImpl
        if(marmotta == null) marmotta = new JettyMarmotta("/marmotta", 8088, SparqlWebService.class);
        marmottaBaseUrl = "http://localhost:8088/marmotta";
        
        storageDir = Files.createTempDirectory("mico-rabbit-component-test");
        storageBaseUri = storageDir.toUri().toString();
        
        //and a PersistenceService
        persistenceService = new PersistenceServiceAnno4j(java.net.URI.create(marmottaBaseUrl), 
                java.net.URI.create(storageBaseUri));

        connection = factory.newConnection();

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

        log.info("initialize event manager with host: {} ....", testHost);
        eventManager = new EventManagerImpl(testHost);
        eventManager.init();

        eventManager.registerService(extr1);
        eventManager.registerService(extr2);
        eventManager.registerService(extr_2);
        eventManager.registerService(extr_a);
        eventManager.registerService(extr_b);
        eventManager.registerService(extr_ab1);
        eventManager.registerService(extr_ab2);
        eventManager.registerService(extr_abc);
        eventManager.registerService(extr_c);
        eventManager.registerService(extr_d);
        eventManager.registerService(extr_e);
        eventManager.registerService(extr_error);
        
        eventManager.registerService(extr_stop);
        eventManager.registerService(extr_f12);
        eventManager.registerService(extr_f23);
        eventManager.registerService(extr_f34); 
        
        eventManager.registerService(extr_aabb);
        eventManager.registerService(extr_bbcc);
        eventManager.registerService(extr_ccdd);

        log.info("event manager initialized: {}", eventManager.getPersistenceService().getStoragePrefix());
    }

    public Item createItem() throws RepositoryException {
        if (eventManager == null) {
            log.warn("Init mico camel befor calling: 'createItem(..)'");
            return null;
        }
        PersistenceService svc = eventManager.getPersistenceService();
        Item item = svc.createItem();
        return item;
    }

    public void deleteContentItem(String item) {
        PersistenceService svc = eventManager.getPersistenceService();
        try {
            svc.deleteItem(new URIImpl(item));
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param content
     *            content of new part
     * @param type
     *            type of new part
     * @param item
     *            item to which the new part should be added
     * @throws RepositoryException
     * @throws IOException
     */
    public Part addPart(byte[] content, String type, Item item, URI extractorId)
            throws RepositoryException, IOException {
        Part part = item.createPart(extractorId);
        part.setSyntacticalType(type);
        addAsset(content, part, type);
        return part;
    }

    public void addAsset(byte[] content, Resource part, String format) throws IOException,
            RepositoryException {
        Asset asset = part.getAsset();
        asset.setFormat(format);
        OutputStream outputStream = asset.getOutputStream();
        outputStream.write(content);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * close all connections to platform
     * 
     * @throws IOException
     */
    public void shutdown() throws IOException {
        
        eventManager.shutdown();
        
        if(registrationChannel != null){
            registrationChannel.close();
            registrationChannel=null;
        }
        if(configChannel != null){
            configChannel.close();
            configChannel=null;
        }
        if(connection != null){
        	connection.clearBlockedListeners();
            connection.close();
            connection=null;
        }
        if(storageDir != null){
            FileUtils.deleteDirectory(storageDir.toFile());
        }
    }

    public void registerService(AnalysisService... extr) throws IOException {
        for(AnalysisService ex : extr){
            eventManager.registerService(ex);
        }
    }

    public void unregisterService(AnalysisService... extr) throws IOException {
        for(AnalysisService ex : extr){
            eventManager.unregisterService(ex);
        }
       }
    
    public String getTestHost() {
        return testHost;
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
            log.debug("returning data from MockBrokerConfigEvents");
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
