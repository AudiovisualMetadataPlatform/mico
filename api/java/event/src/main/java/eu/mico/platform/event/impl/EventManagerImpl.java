package eu.mico.platform.event.impl;

import com.google.common.base.Preconditions;
import com.rabbitmq.client.*;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceImpl;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EventManagerImpl implements EventManager {

    /**
     * Name of service registry exchange where brokers bind their registration queues. The event manager will send
     * a registration event to this exchange every time a new service is registered.
     */
    public static final String EXCHANGE_SERVICE_REGISTRY  = "service_registry";

    /**
     * Name of service discovery exchange where brokers send discovery requests. The event manager binds its own
     * discovery queue to this exchange and reacts on any incoming discovery events by sending its service list to
     * the replyTo queue provided by the requester.
     */
    public static final String EXCHANGE_SERVICE_DISCOVERY = "service_discovery";

    private static Logger log = LoggerFactory.getLogger(EventManagerImpl.class);

    private String host;
    private int rabbitPort;
    private int marmottaPort;

    private String user;
    private String password;

    private PersistenceService persistenceService;

    private Connection connection;
    private Channel    registryChannel;

    private Map<AnalysisService, AnalysisConsumer> services;

    private DiscoveryConsumer discovery;

    public EventManagerImpl(String host) throws IOException {
        this(host, "mico", "mico");
    }


    public EventManagerImpl(String host, String user, String password) throws IOException {
        this(host,5672, 8080, user, password);
    }

    public EventManagerImpl(String host, int rabbitPort, int marmottaPort, String user, String password) throws IOException {
        this.host = host;
        this.rabbitPort = rabbitPort;
        this.marmottaPort = marmottaPort;
        this.user = user;
        this.password = password;

        Preconditions.checkArgument(marmottaPort == 8080, "changing the marmotta port is currently not supported");



        services = new HashMap<>();
    }

    /**
     * Initialise the event manager, setting up any necessary channels and connections
     */
    @Override
    public void init() throws IOException {

        persistenceService = new PersistenceServiceImpl(host,user,password);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(rabbitPort);
        factory.setUsername(user);
        factory.setPassword(password);

        connection = factory.newConnection();

        registryChannel = connection.createChannel();

        // make sure the service registry and discovery channels exists
        registryChannel.exchangeDeclarePassive(EXCHANGE_SERVICE_REGISTRY);
        registryChannel.exchangeDeclarePassive(EXCHANGE_SERVICE_DISCOVERY);

        // register a listener queue for this event manager on the discovery exchange so we can react to discovery requests
        discovery = new DiscoveryConsumer();
    }

    /**
     * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
     */
    @Override
    public void shutdown() throws IOException {
        for(Map.Entry<AnalysisService, AnalysisConsumer> svc : services.entrySet()) {
            svc.getValue().getChannel().close();
        }

        registryChannel.close();
        connection.close();
    }

    /**
     * Register the given service with the event manager.
     *
     * @param service
     */
    @Override
    public void registerService(AnalysisService service) throws IOException {
        Channel chan = connection.createChannel();

        // first declare a new input queue for this service using the service queue name, and register a callback
        String queueName = service.getQueueName() != null ? service.getQueueName() : UUID.randomUUID().toString();

        // then create a new analysis consumer (auto-registered to its queue name)
        services.put(service, new AnalysisConsumer(service, queueName));


        // then send a registration message to the broker's "service_registry" exchange; all running brokers will
        // receive this message, assuming that they bound their queue to the registry exchange

        Event.RegistrationEvent registrationEvent =
                Event.RegistrationEvent.newBuilder()
                        .setServiceId(service.getServiceID().stringValue())
                        .setQueueName(queueName)
                        .setProvides(service.getProvides())
                        .setRequires(service.getRequires()).build();

        chan.basicPublish(EXCHANGE_SERVICE_REGISTRY, "", null, registrationEvent.toByteArray());

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
            getChannel().basicConsume(queueName, true, this);
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

            // construct reply properties, use the same correlation ID as in the request
            final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();


            for(Map.Entry<AnalysisService, AnalysisConsumer> svc : services.entrySet()) {
                Event.RegistrationEvent registrationEvent =
                        Event.RegistrationEvent.newBuilder()
                                .setServiceId(svc.getKey().getServiceID().stringValue())
                                .setQueueName(svc.getValue().getQueueName())
                                .setProvides(svc.getKey().getProvides())
                                .setRequires(svc.getKey().getRequires()).build();


                getChannel().basicPublish("", properties.getReplyTo(), replyProps, registrationEvent.toByteArray());
            }

        }
    }


    private class AnalysisConsumer extends DefaultConsumer {

        private AnalysisService service;
        private String          queueName;

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
            Event.AnalysisEvent analysisEvent = Event.AnalysisEvent.parseFrom(body);

            // construct reply properties, use the same correlation ID as in the request
            final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();

            final AnalysisResponse response = new AnalysisResponse() {
                @Override
                public void sendMessage(ContentItem ci, URI object) throws IOException {
                    Event.AnalysisEvent responseEvent = Event.AnalysisEvent.newBuilder()
                            .setContentItemUri(ci.getURI().stringValue())
                            .setObjectUri(object.stringValue())
                            .setServiceId(service.getServiceID().stringValue()).build();

                    getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseEvent.toByteArray());
                }
            };

            try {
                final ContentItem ci = persistenceService.getContentItem(new URIImpl(analysisEvent.getContentItemUri()));

                service.call(response, ci, new URIImpl(analysisEvent.getObjectUri()));

                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } catch (RepositoryException e) {
                log.error("could not access content item with URI {}, requeuing (message: {})", analysisEvent.getContentItemUri(), e.getMessage());
                log.debug("Exception:",e);
                getChannel().basicNack(envelope.getDeliveryTag(), false, true);
            } catch (AnalysisException e) {
                log.error("could not analyse content item with URI {}, requeuing (message: {})", analysisEvent.getContentItemUri(), e.getMessage());
                log.debug("Exception:", e);
                getChannel().basicNack(envelope.getDeliveryTag(), false, true);
            }

        }
    }
}
