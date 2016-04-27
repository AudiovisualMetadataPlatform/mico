package de.fraunhofer.idmt.camel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.fraunhofer.idmt.mico.DummyExtractor;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

public class MicoCamel {
    private static Logger log = LoggerFactory.getLogger(MicoCamel.class);

    protected static String testHost;

    protected EventManager eventManager;
    protected Connection connection;

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

    /**
     * setup test environment including mico eventManager and some registered
     * dummy services
     * 
     * @throws IOException
     * @throws TimeoutException
     * @throws URISyntaxException
     */
    public void init() throws IOException, TimeoutException, URISyntaxException {
        String testHost = System.getProperty("test.host");
        if (testHost == null) {
            log.warn("'test.host' environment variable not defined, using default of mico-box");
            testHost = "mico-box";
        }
        log.info("initialize event manager with host: {} ....", testHost);
        eventManager = new EventManagerImpl(testHost);
        eventManager.init();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(testHost);
        factory.setUsername("mico");
        factory.setPassword("mico");

        connection = factory.newConnection();

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
        connection.clearBlockedListeners();
        connection.close();
        eventManager.shutdown();
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
}
