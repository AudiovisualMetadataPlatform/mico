package de.fraunhofer.idmt.camel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import de.fraunhofer.idmt.mico.DummyExtractor;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;

public class MicoCamel {
    private static Logger log = LoggerFactory.getLogger(MicoCamel.class);

    protected static String testHost;

    protected EventManager eventManager;
    protected Connection   connection;
    protected Channel      channel;
    
	protected static AnalysisService extr_1 = new DummyExtractor("A", "B");
	protected static AnalysisService extr_2 = new DummyExtractor("B", "text/plain");

    
	public void init() throws IOException, TimeoutException, URISyntaxException{
		String         testHost = System.getenv("test.host");
        if(testHost == null) {
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
	        channel    = connection.createChannel();
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("x-expires", 180000);
	        channel.queueDeclare("myqueue", false, false, false, args);
	        eventManager.registerService(extr_1);
	        eventManager.registerService(extr_2);
	        
	}
	
	
    public void testSimpleAnalyse() throws IOException, InterruptedException, RepositoryException {

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(EventManager.QUEUE_CONTENT_OUTPUT, true, consumer);
        log.info("channel number: " + channel.getChannelNumber() + " consumerTag: " + consumer.getConsumerTag());

        // create a content item with a single part of type "A"; it should walk through the registered mock services and
        // eventually finish analysis; we simply wait until we receive an event on the output queue.
        PersistenceService svc = eventManager.getPersistenceService();
        ContentItem item = svc.createContentItem();
        try {
            Content partA = item.createContentPart();
            partA.setType("A");
            partA.getOutputStream().write("Das ist Text A".getBytes());

            log.info("sending item ...   {}", System.currentTimeMillis());
//            eventManager.injectContentItem(item);
            log.info("sending item done  {}", System.currentTimeMillis());

            // wait for result notification and verify it contains what we expect
            QueueingConsumer.Delivery delivery;
            while((delivery = consumer.nextDelivery(10000)) != null){
			

	            Event.ContentEvent event = Event.ContentEvent.parseFrom(delivery.getBody());
	
	            log.info("the created item {} should be same as event item {}",item.getURI().stringValue(), event.getContentItemUri());
	
	            // each service should have added a part, so there are now four different parts
//	            Set<Content> parts = ImmutableSet.copyOf(item.listContentParts());
//	            log.info("parts.size() should be 3 and is: {}", parts.size());
//	            StringBuilder msg = new StringBuilder("parts in content: [");
//	            for (Content co : parts){
//	            	msg.append(co.getType()).append(", ");
//	            }
//	            int len = msg.length();
//	            msg.replace(len-2, len, "]");
//	            log.info(msg.toString());
	            Thread.sleep(3000);
	            
	            // keep running service in the background, and wait for user command "q" on the frontent to terminate
	            // service (other approaches might be more sensible for a service, e.g. commons-daemon)
	            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	            char c = ' ';
	            while(Character.toLowerCase(c) != 'q') {
	                System.out.print("enter 'q' to quit: ");
	                System.out.flush();

	                c = in.readLine().charAt(0);
	            }
            }
        } finally {
            //svc.deleteContentItem(item.getURI());
            channel.clearConfirmListeners();
            channel.clearFlowListeners();
            channel.clearReturnListeners();
            channel.close();
            connection.clearBlockedListeners();
            connection.close();
            eventManager.shutdown();
        }
    }
}
