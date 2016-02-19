/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.event.impl;

import com.google.common.base.Preconditions;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisEvent;
import eu.mico.platform.event.model.Event.AnalysisRequest.ParamEntry;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.event.model.Event.MessageType;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EventManagerImpl implements EventManager {

    private final int RPC_CONFIG_TIMEOUT = 5 * 1000;

    private static Logger log = LoggerFactory.getLogger(EventManagerImpl.class);

    private String amqpHost;
    private int amqpPort;
    private String amqpUser;
    private String amqpPassword;

    private java.net.URI marmottaBaseUri;
    private java.net.URI storageBaseUri;

    private PersistenceService persistenceService;

    private Connection connection;
    private Channel registryChannel;

    private Map<AnalysisService, AnalysisConsumer> services;

    private DiscoveryConsumer discovery;

    public EventManagerImpl(String amqpHost) throws IOException {
        this(amqpHost, "mico", "mico");
    }


    public EventManagerImpl(String amqpHost, String amqpUser, String amqpPassword) throws IOException {
        this(amqpHost, 5672, amqpUser, amqpPassword);
    }

    public EventManagerImpl(String amqpHost, int amqpPort, String amqpUser, String amqpPassword) throws IOException {
        this.amqpHost = amqpHost;
        this.amqpPort = amqpPort;
        this.amqpUser = amqpUser;
        this.amqpPassword = amqpPassword;

        services = new HashMap<>();
    }

    @Override
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    /**
     * Initialise the event manager, setting up any necessary channels and connections
     */
    @Override
    public void init() throws IOException, TimeoutException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(amqpHost);
        factory.setPort(amqpPort);
        factory.setUsername(amqpUser);
        factory.setPassword(amqpPassword);

        connection = factory.newConnection();

        getConfiguration();
        Preconditions.checkArgument(marmottaBaseUri.getPort() == 8080, "The marmotta port has to be 8080.");
        this.persistenceService = new PersistenceServiceAnno4j(marmottaBaseUri, storageBaseUri);

        registryChannel = connection.createChannel();

        // make sure the service registry and discovery channels exists
        registryChannel.exchangeDeclarePassive(EXCHANGE_SERVICE_REGISTRY);
        registryChannel.exchangeDeclarePassive(EXCHANGE_SERVICE_DISCOVERY);

        // register a listener queue for this event manager on the discovery exchange so we can react to discovery requests
        discovery = new DiscoveryConsumer();
    }

    private void getConfiguration() throws IOException, TimeoutException, java.net.URISyntaxException {
        Channel configChannel = connection.createChannel();
        RpcClient configClient = new RpcClient(configChannel, "", QUEUE_CONFIG_REQUEST, RPC_CONFIG_TIMEOUT);

        log.info("Retrieving Marmotta and storage configuration...");
        Event.ConfigurationEvent config;
        try {
            config = Event.ConfigurationEvent.parseFrom(configClient.primitiveCall(Event.ConfigurationDiscoverEvent.newBuilder().build().toByteArray()));
        } finally {
            configClient.close();
            configChannel.close();
        }

        log.info("Got Marmotta base URI: {}", config.getMarmottaBaseUri());
        this.marmottaBaseUri = new java.net.URI(config.getMarmottaBaseUri());
        log.info("Got storage base URI: {}", config.getStorageBaseUri());
        this.storageBaseUri = new java.net.URI(config.getStorageBaseUri());
    }

    /**
     * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
     */
    @Override
    public void shutdown() throws IOException {
        for (Map.Entry<AnalysisService, AnalysisConsumer> svc : services.entrySet()) {
            unregisterService(svc.getKey());
            svc.getValue().getChannel().close();
        }

        if (registryChannel.isOpen()) {
            registryChannel.close();
        }

        if (connection.isOpen()) {
            connection.close();
        }
    }

    /**
     * Register the given service with the event manager.
     *
     * @param service
     */
    @Override
    public void registerService(AnalysisService service) throws IOException {
        log.info("registering new service {} with message brokers ...", service.getServiceID());

        Channel chan = connection.createChannel();

        // first declare a new input queue for this service using the service queue name, and register a callback
        String queueName = service.getQueueName() != null ? service.getQueueName() : UUID.randomUUID().toString();

        // then create a new analysis consumer (auto-registered to its queue name)
        services.put(service, new AnalysisConsumer(service, queueName));


        // then send a registration message to the broker's "service_registry" exchange; all running brokers will
        // receive this message, assuming that they bound their queue to the registry exchange

        Event.RegistrationEvent registrationEvent =
                Event.RegistrationEvent.newBuilder()
                        .setType(Event.RegistrationType.REGISTER)
                        .setServiceId(service.getServiceID().stringValue())
                        .setQueueName(queueName)
                        .setProvides(service.getProvides())
                        .setRequires(service.getRequires()).build();

        chan.basicPublish(EXCHANGE_SERVICE_REGISTRY, "", null, registrationEvent.toByteArray());

        chan.close();
    }

    /**
     * Register the given service with the event manager.
     *
     * @param service
     */
    @Override
    public void unregisterService(AnalysisService service) throws IOException {
        log.info("unregistering new service {} with message brokers ...", service.getServiceID());

        Channel chan = connection.createChannel();

        // first declare a new input queue for this service using the service queue name, and register a callback
        String queueName = service.getQueueName() != null ? service.getQueueName() : UUID.randomUUID().toString();

        // then create a new analysis consumer (auto-registered to its queue name)
        services.put(service, new AnalysisConsumer(service, queueName));


        // then send a registration message to the broker's "service_registry" exchange; all running brokers will
        // receive this message, assuming that they bound their queue to the registry exchange

        Event.RegistrationEvent registrationEvent =
                Event.RegistrationEvent.newBuilder()
                        .setType(Event.RegistrationType.UNREGISTER)
                        .setServiceId(service.getServiceID().stringValue())
                        .setQueueName(queueName)
                        .setProvides(service.getProvides())
                        .setRequires(service.getRequires()).build();

        chan.basicPublish(EXCHANGE_SERVICE_REGISTRY, "", null, registrationEvent.toByteArray());

        chan.close();
    }


    /**
     * Trigger analysis of the given content item.
     *
     * @param item  the item to analyse
     * @throws java.io.IOException
     */
    @Override
    public void injectItem(Item item) throws IOException {
        Channel chan = connection.createChannel();
        chan.basicPublish("", EventManager.QUEUE_CONTENT_INPUT, null, Event.ItemEvent.newBuilder().setItemUri(item.getURI().stringValue()).build().toByteArray());
        chan.close();
    }

    /**
     * A consumer reacting to service discovery requests. Upon initialisation, it creates its own queue and binds it to
     * the service discovery exchange. Upon a discovery event, it simply sends back its list of services to the replyTo
     * queue provided in the discovery request.
     */
    private class DiscoveryConsumer extends DefaultConsumer {
        public DiscoveryConsumer() throws IOException {
            super(registryChannel);

            String queueName = getChannel().queueDeclare().getQueue();
            getChannel().queueBind(queueName, EXCHANGE_SERVICE_DISCOVERY, "");
            getChannel().basicConsume(queueName, false, this);
        }

        /**
         * Called when a discovery event has been received on the discovery exchange. In this case, we send back our local
         * list of analysis services via the replyTo queue provided in the request. The request payload is currently ignored.
         *
         * @param consumerTag
         * @param envelope
         * @param properties
         * @param body
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            log.info("received service discovery request for reply queue {} ...", properties.getReplyTo());

            // construct reply properties, use the same correlation ID as in the request
            final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();


            for (Map.Entry<AnalysisService, AnalysisConsumer> svc : services.entrySet()) {
                log.info("- discover service {} ...", svc.getKey().getServiceID());
                Event.RegistrationEvent registrationEvent =
                        Event.RegistrationEvent.newBuilder()
                                .setServiceId(svc.getKey().getServiceID().stringValue())
                                .setQueueName(svc.getValue().getQueueName())
                                .setProvides(svc.getKey().getProvides())
                                .setRequires(svc.getKey().getRequires()).build();


                getChannel().basicPublish("", properties.getReplyTo(), replyProps, registrationEvent.toByteArray());
            }

            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }
    }


    /**
     * This is the client (extractor) side of event api.
     */
    private class AnalysisConsumer extends DefaultConsumer {

        private final class AnalysisResponseImpl implements AnalysisResponse {
            private final BasicProperties properties;
            private final BasicProperties replyProps;
            private long progressSentMS = 0;
            private final long progressDeltaMS = 1000;

            private AnalysisResponseImpl(BasicProperties properties,
                                         BasicProperties replyProps) {
                this.properties = properties;
                this.replyProps = replyProps;
            }

            @Override
            public void sendFinish(Item item) throws IOException {
                AnalysisEvent.Finish finishMsg = AnalysisEvent.Finish.newBuilder()
                        .setServiceId(service.getServiceID().stringValue())
                        .setItemUri(item.getURI().stringValue())
                        .build();

                AnalysisEvent analysisEvent = AnalysisEvent.newBuilder()
                        .setFinish(finishMsg)
                        .setType(MessageType.FINISH)
                        .build();

                getChannel().basicPublish("", properties.getReplyTo(), replyProps, analysisEvent.toByteArray());
            }

            @Override
            public void sendProgress(Item item, URI part, float progress) throws IOException {
                long current = System.currentTimeMillis();
                if (current < progressDeltaMS + progressSentMS) {
                    // prevent broker from progress flooding and wait at least deltaMS millis
                    log.trace("suppress sending progress: {} for object: {}", progress, part);
                    return;
                }
                log.trace("sending progress of: {} for object: {}", progress, part);
                if (progress > 1.0001f) {
                    log.warn("progress should not be grater then 1.0 but is: {}", progress);
                }
                AnalysisEvent.Progress progressMsg = AnalysisEvent.Progress.newBuilder()
                        .setItemUri(item.getURI().stringValue())
                        .setPartUri(part.stringValue())
                        .setServiceId(service.getServiceID().stringValue())
                        .setProgress(progress).build();

                AnalysisEvent analysisEvent = AnalysisEvent.newBuilder()
                        .setProgress(progressMsg)
                        .setType(MessageType.PROGRESS)
                        .build();

                getChannel().basicPublish("", properties.getReplyTo(), replyProps, analysisEvent.toByteArray());
                progressSentMS = current;
            }

            @Override
            public void sendNew(Item item, URI part) throws IOException {
                AnalysisEvent.NewPart newPartMsg = AnalysisEvent.NewPart.newBuilder()
                        .setItemUri(item.getURI().stringValue())
                        .setPartUri(part.stringValue())
                        .setServiceId(service.getServiceID().stringValue())
                        .build();

                AnalysisEvent analysisEvent = AnalysisEvent.newBuilder()
                        .setNew(newPartMsg)
                        .setType(MessageType.NEW_PART)
                        .build();

                getChannel().basicPublish("", properties.getReplyTo(), replyProps, analysisEvent.toByteArray());
            }

            @Override
            public void sendError(Item item, ErrorCodes code, String msg, String desc) throws IOException {
                AnalysisEvent.Error responseEvent = AnalysisEvent.Error.newBuilder()
                        .setItemUri(item.getURI().stringValue())
                        .setServiceId(service.getServiceID().stringValue())
                        .setMessage(msg)
                        .setDescription(desc).build();

                getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseEvent.toByteArray());
            }
        }

        private AnalysisService service;
        private String queueName;

        public AnalysisConsumer(AnalysisService service, String queueName) throws IOException {
            super(connection.createChannel());

            this.service = service;
            this.queueName = queueName;

            getChannel().queueDeclare(queueName, true, false, true, null);
            getChannel().basicConsume(queueName, false, this);
        }

        public String getQueueName() {
            return queueName;
        }

        /**
         * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
         *
         * @param consumerTag the <i>consumer tag</i> associated with the consumer
         * @param envelope    packaging data for the message
         * @param properties  content header data for the message
         * @param body        the message body (opaque, client-specific byte array)
         * @throws java.io.IOException if the consumer encounters an I/O error while processing the message
         * @see com.rabbitmq.client.Envelope
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, final AMQP.BasicProperties properties, byte[] body) throws IOException {
            // retrieve the event data from the byte array
            Event.AnalysisRequest analysisRequest = Event.AnalysisRequest.parseFrom(body);

            // construct reply properties, use the same correlation ID as in the request
            final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();

            final AnalysisResponse response = new AnalysisResponseImpl(properties, replyProps);

            try {

                final Item item = persistenceService.getItem(new URIImpl(analysisRequest.getItemUri()));

                final List<Resource> resourceList = parseResourceList(analysisRequest.getPartUriList(), item);
                final Map<String, String> params = new HashMap();
                for (ParamEntry entry : analysisRequest.getParamsList()) {
                    params.put(entry.getKey(), entry.getValue());
                }

                if (service instanceof AnalysisServiceAnno4j) {
                    ((AnalysisServiceAnno4j) service).setAnno4j(persistenceService.getAnno4j());
                }

                try {
                    service.call(response, item, resourceList, params);
                } catch (Throwable t) {
                    log.error("could not analyse item with URI {}, requeuing (message: {})", analysisRequest.getItemUri(), t.getMessage());
                    log.debug("Exception:", t);
                    response.sendError(item, ErrorCodes.UNEXPECTED_ERROR, t.getMessage(), "could not analyse item with URI " + item.getURI());
                }

                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } catch (RepositoryException e) {
                log.error("could not access content item with URI {}, requeuing (message: {})", analysisRequest.getItemUri(), e.getMessage());
                log.debug("Exception:", e);
                getChannel().basicNack(envelope.getDeliveryTag(), false, true);
            }
        }

        private List<Resource> parseResourceList(List<String> resourceUriList, Item item) throws RepositoryException {
            List<Resource> parts = new ArrayList<>();

            for (String resourceUri : resourceUriList) {
                if (item.getURI().toString().equals(resourceUri)) {
                    parts.add(item);
                } else {
                    parts.add(item.getPart(new URIImpl(resourceUri)));
                }
            }
            return parts;
        }
    }
}
