package de.fraunhofer.idmt.camel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.TupleQueryResult;
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
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;

public class MicoCamel {
    private static Logger log = LoggerFactory.getLogger(MicoCamel.class);

    protected static String testHost;

    protected EventManager eventManager;
    protected Connection connection;
    protected Channel channel;

    protected static AnalysisService extr_1 = new DummyExtractor("A", "B");
    protected static AnalysisService extr_2 = new DummyExtractor("B", "text/plain");

    public void init() throws IOException, TimeoutException, URISyntaxException {
        String testHost = System.getenv("test.host");
        if (testHost == null) {
            log.warn("'test.host' environment variable not defined, using default of mico-platform");
            testHost = "mico-platform";
        }
        eventManager = new EventManagerImpl(testHost);
        eventManager.init();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(testHost);
        factory.setUsername("mico");
        factory.setPassword("mico");

        connection = factory.newConnection();
        channel = connection.createChannel();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 180000);
        channel.queueDeclare("myqueue", false, false, false, args);
        eventManager.registerService(extr_1);
        eventManager.registerService(extr_2);

    }

    public ContentItem createItem() throws RepositoryException {
        if (eventManager == null) {
            log.warn("Init mico camel befor calling: 'createItem(..)'");
            return null;
        }
        PersistenceService svc = eventManager.getPersistenceService();
        ContentItem item = svc.createContentItem();
        return item;
    }

    public void deleteContentItem(String item) {
        PersistenceService svc = eventManager.getPersistenceService();
        try {
            svc.deleteContentItem(new URIImpl(item));
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
    public Content addPart(byte[] content, String type, ContentItem item)
            throws RepositoryException, IOException {
        Content partA = item.createContentPart();
        partA.setType(type);
        OutputStream outputStream = partA.getOutputStream();
        outputStream.write(content);
        outputStream.flush();
        outputStream.close();
        return partA;
    }

    /**
     * close all connections to platform
     * 
     * @throws IOException
     */
    public void shutdown() throws IOException {
        channel.clearConfirmListeners();
        channel.clearFlowListeners();
        channel.clearReturnListeners();
        channel.close();
        connection.clearBlockedListeners();
        connection.close();
        eventManager.shutdown();
    }
}
