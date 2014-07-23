package eu.mico.platform.event.test;

import com.rabbitmq.client.*;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.model.ContentItem;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EventManagerTest extends BaseCommunicationTest {

    private static Connection connection;
    private static MockBroker broker;

    @BeforeClass
    public static void setupLocal() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(testHost);
        factory.setUsername("mico");
        factory.setPassword("mico");

        connection = factory.newConnection();

        broker = new MockBroker(connection.createChannel());
    }


    @AfterClass
    public static void teardownLocal() throws IOException {
        connection.close();
    }


    @Test
    public void testCreateEventManager() throws IOException {
        EventManager mgr = new EventManagerImpl(testHost);
        mgr.init();
        mgr.shutdown();
    }

    @Test
    public void testRegisterService() throws IOException, InterruptedException {
        AnalysisService mock = new MockService();

        EventManager mgr = new EventManagerImpl(testHost);
        mgr.init();

        mgr.registerService(mock);

        // give the queue some time and then test for registration success
        synchronized (broker) {
            broker.wait(500);
        }
        Assert.assertEquals(mock.getServiceID().stringValue(), broker.lastService);

        mgr.shutdown();

    }


    private static class MockService implements AnalysisService {

        private boolean called = false;

        @Override
        public URI getServiceID() {
            return new URIImpl("http://example.org/services/TestService");
        }

        @Override
        public String getProvides() {
            return "test-output";
        }

        @Override
        public String getRequires() {
            return "test-input";
        }

        @Override
        public String getQueueName() {
            return "test-queue";
        }

        @Override
        public void call(AnalysisResponse resp, ContentItem ci, URI object) throws AnalysisException, IOException {
            resp.sendMessage(ci,object);
            called = true;
        }

    }

    // a mock message broker just recording service registry events to test if they worked
    private static class MockBroker extends DefaultConsumer {

        private String lastService;


        public MockBroker(Channel channel) throws IOException {
            super(channel);

            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, "service_registry", "");
            channel.basicConsume(queueName, true, this);
        }


        /**
         * No-op implementation of {@link com.rabbitmq.client.Consumer#handleDelivery}.
         *
         * @param consumerTag
         * @param envelope
         * @param properties
         * @param body
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Event.RegistrationEvent registrationEvent = Event.RegistrationEvent.parseFrom(body);

            lastService = registrationEvent.getServiceId();

            synchronized (this) {
                this.notifyAll();
            }
        }
    }
}
