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

import com.rabbitmq.client.*;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EventManagerTest extends BaseCommunicationTest {

    private static Connection connection;
    private static MockBrokerRegisterEvents brokerRegister;
    private static MockBrokerConfigEvents brokerConfig;
    private static Channel registrationChannel;
    private static Channel configChannel;

    @BeforeClass
    public static void setupLocal() throws IOException {
        ConnectionFactory testFactory = new ConnectionFactory();
        testFactory.setHost(testHost);
        testFactory.setUsername(testUsr);
        testFactory.setPassword(testPwd);

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
        registrationChannel.close();
        configChannel.close();
        connection.close();
    }


    @Test
    public void testCreateEventManager() throws IOException, URISyntaxException, TimeoutException {
        EventManager mgr = new EventManagerImpl(testHost, testUsr, testPwd, testVHost);
        mgr.init();
        mgr.shutdown();
    }

    @Test
    public void testRegisterService() throws IOException, InterruptedException, URISyntaxException, TimeoutException {
        AnalysisService mock = new MockService();

        EventManager mgr = new EventManagerImpl(testHost, testUsr, testPwd, testVHost);
        mgr.init();

        mgr.registerService(mock);
        // give the queue some time and then test for registration success
        synchronized (brokerRegister) {
            brokerRegister.wait(500);
        }
        Assert.assertEquals(mock.getServiceID().stringValue(), brokerRegister.lastService);

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
        public void call(AnalysisResponse resp, Item ci, List<Resource> object, Map<String,String> params) throws AnalysisException, IOException {
            called = true;
            try {
                resp.sendFinish(ci);
            } catch (RepositoryException e) {
                throw new IOException(e);
            }
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

            channel.queueDeclare("config_request", false, true, false, null);
            channel.basicConsume("config_request", false, this);
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
                    .setMarmottaBaseUri("http://" + testHost + ":8080/marmotta")
                    .setStorageBaseUri("ftp://" + testHost + "/")
                    .build();

            // send configuration
            getChannel().basicPublish("", properties.getReplyTo(), replyProps, config.toByteArray());

            // ack request
            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }
    }
}
