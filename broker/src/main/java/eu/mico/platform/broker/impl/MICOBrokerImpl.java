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
package eu.mico.platform.broker.impl;

import com.rabbitmq.client.*;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.exception.StateNotFoundException;
import eu.mico.platform.broker.model.*;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceImpl;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * MICO Message Broker, orchestrating the communication between analysis services and the analysis workflow for content
 * items using RabbitMQ.
 *
 * Maintains a dependency graph of registered services based on the input and output they provide. For each content item,
 * uses this dependency graph to execute the analysis workflow.
 *
 * Maintains the following RabbitMQ exchanges:
 * - service_registry: an exchange where the event api sends new service registration events
 * - service_discovery: an exchange where the message broker sends a discovery request on startup and event managers
 *   respond with their service lists
 *
 * Maintains the following RabbitMQ queues:
 * - content item input queue: the broker creates the queue if it does not exist and registers itself as a consumer for
 *                             newly injected content items
 * - content item replyto queue: the broker creates a new temporary queue for each content item it processes and sets it as
 *                             replyto queue for services analysing this content item; it registers itself as consumer
 *                             so it can forward results to the next analysers in the analysis graph
 * - registry queue bound to service_registry: the broker registers itself as consumer to get notified about newly
 *   registered services
 * - replyto queue for each service discovery event: temporarily created when a service discovery is in process; event
 *   managers will respond to this queue about their services; the broker registers itself as consumer to get notified;
 *   queue is cleaned up automatically when it is no longer used.
 *
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MICOBrokerImpl implements MICOBroker {

    private static Logger log = LoggerFactory.getLogger(MICOBrokerImpl.class);

    private String host;
    private String user;
    private String password;
    private int rabbitPort;
    private int marmottaPort;

    // a graph for representing the currently registered service dependencies
    private ServiceGraph dependencies;


    private Connection connection;

    private PersistenceService persistenceService;

    private Channel registryChannel, discoveryChannel, contentChannel;

    // map from content item URIs to processing states
    private Map<String,ContentItemState> states;

    // map from content item URIs to channels
    private Map<String,Channel> channels;


    public MICOBrokerImpl(String host) throws IOException, URISyntaxException {
        this(host, "mico", "mico");
    }


    public MICOBrokerImpl(String host, String user, String password) throws IOException, URISyntaxException {
        this(host, user, password,5672, 8080);
    }


    public MICOBrokerImpl(String host, String user, String password, int rabbitPort, int marmottaPort) throws IOException, URISyntaxException {
        this.host = host;
        this.user = user;
        this.password = password;
        this.rabbitPort = rabbitPort;
        this.marmottaPort = marmottaPort;

        dependencies = new ServiceGraph();
        states       = new HashMap<>();
        channels     = new HashMap<>();

        log.info("initialising persistence service ...");

        persistenceService = new PersistenceServiceImpl(host);

        log.info("initialising RabbitMQ connection ...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(rabbitPort);
        factory.setUsername(user);
        factory.setPassword(password);

        connection = factory.newConnection();

        initRegistryQueue();
        initDiscoveryQueue();
        initContentItemQueue();
    }


    @Override
    public ServiceGraph getDependencies() {
        return dependencies;
    }


    @Override
    public Map<String, ContentItemState> getStates() {
        return states;
    }

    @Override
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }



    private void initRegistryQueue() throws IOException {
        log.info("setting up service registration queue ...");
        registryChannel = connection.createChannel();

        // create the exchange in case it does not exist
        registryChannel.exchangeDeclare(EventManager.EXCHANGE_SERVICE_REGISTRY, "fanout", true);

        // create a temporary queue, bind it to the exchange, and register a service listener that updates our dependencies
        // graph whenever a new service is registered
        String queueName = registryChannel.queueDeclare().getQueue();
        registryChannel.queueBind(queueName, EventManager.EXCHANGE_SERVICE_REGISTRY, "");
        registryChannel.basicConsume(queueName, false, new RegistrationConsumer(registryChannel));
    }


    private void initDiscoveryQueue() throws IOException {
        log.info("setting up service discovery queue ...");
        discoveryChannel = connection.createChannel();
        discoveryChannel.confirmSelect();

        // create the exchange in case it does not exist
        discoveryChannel.exchangeDeclare(EventManager.EXCHANGE_SERVICE_DISCOVERY, "fanout", true);

        // send a discovery request to all event managers
        String queueName = discoveryChannel.queueDeclare().getQueue();

        log.info("- sending service discovery request with reply queue {}...", queueName);
        discoveryChannel.basicConsume(queueName, false, new RegistrationConsumer(discoveryChannel));

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().replyTo(queueName).correlationId(UUID.randomUUID().toString()).build();
        discoveryChannel.basicPublish(EventManager.EXCHANGE_SERVICE_DISCOVERY,"",props,Event.DiscoveryEvent.newBuilder().build().toByteArray());

/*
        try {
            discoveryChannel.waitForConfirms();
            log.info("service discovery finished!");
        } catch (InterruptedException e) {
            log.warn("service discovery was interrupted");
        }
        discoveryChannel.close();
*/
    }


    private void initContentItemQueue() throws IOException {
        log.info("setting up content injection queue ...");
        contentChannel = connection.createChannel();

        // create the input and output queue with a defined name
        contentChannel.queueDeclare(EventManager.QUEUE_CONTENT_INPUT,true,false,false,null);
        contentChannel.queueDeclare(EventManager.QUEUE_CONTENT_OUTPUT,true,false,false,null);

        // register a content item consumer with the queue
        contentChannel.basicConsume(EventManager.QUEUE_CONTENT_INPUT,false, new ContentItemConsumer(contentChannel));
    }


    /**
     * Handle service registration events.
     */
    private class RegistrationConsumer extends DefaultConsumer {
        public RegistrationConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Event.RegistrationEvent registrationEvent = Event.RegistrationEvent.parseFrom(body);

            if(registrationEvent.getType() == Event.RegistrationType.REGISTER) {
                log.info("service registration of service {} (in: {}, out: {}, queue: {})", registrationEvent.getServiceId(), registrationEvent.getRequires(), registrationEvent.getProvides(), registrationEvent.getQueueName());

                ServiceDescriptor svc = new ServiceDescriptor(registrationEvent);
                TypeDescriptor tin = new TypeDescriptor(registrationEvent.getRequires());
                TypeDescriptor tout = new TypeDescriptor(registrationEvent.getProvides());

                if (!dependencies.containsEdge(svc)) {
                    log.info("- adding service {} to dependency graph, as it does not exist yet", svc.getUri());
                    dependencies.addEdge(tin, tout, svc);
                } else {
                    log.info("- not adding service {} to dependency graph, it already exists", svc.getUri());
                }

            } else {
                log.info("service unregistration of service {} (in: {}, out: {}, queue: {})", registrationEvent.getServiceId(), registrationEvent.getRequires(), registrationEvent.getProvides(), registrationEvent.getQueueName());

                ServiceDescriptor svc = new ServiceDescriptor(registrationEvent);

                if (dependencies.containsEdge(svc)) {
                    log.info("- removing service {} from dependency graph", svc.getUri());
                    dependencies.removeEdge(svc);
                } else {
                    log.info("- not removing service {} from dependency graph, it does not exists", svc.getUri());
                }
            }
            getChannel().basicAck(envelope.getDeliveryTag(), false);

            // in case someone is waiting for a notification, send it now
            synchronized (MICOBrokerImpl.this) {
                MICOBrokerImpl.this.notifyAll();
            }
        }
    }


    /**
     * Handle the injection of new content items into the system by watching the QUEUE_CONTENT_INPUT
     */
    private class ContentItemConsumer extends DefaultConsumer {
        public ContentItemConsumer(Channel channel) {
            super(channel);
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
            Event.ContentEvent contentEvent = Event.ContentEvent.parseFrom(body);

            log.info("new content item injected (URI: {}), preparing for analysis!", contentEvent.getContentItemUri());
            try {
                ContentItem item = persistenceService.createContentItem(new URIImpl(contentEvent.getContentItemUri()));

                log.info("- adding initial content item state ...");
                ContentItemState state = new ContentItemState(dependencies,item);
                states.put(contentEvent.getContentItemUri(), state);

                log.info("- setting up messaging for content item analysis ...");

                // create a new channel for the content item so it runs isolated from the other items
                Channel channel = connection.createChannel();

                log.info("- triggering analysis process for initial states ...");
                ContentItemManager mgr = new ContentItemManager(item,state,channel);
                Thread t = new Thread(mgr);
                t.start();

                getChannel().basicAck(envelope.getDeliveryTag(), false);

            } catch (RepositoryException e) {
                log.error("could not load content item from persistence layer (message: {})", e.getMessage());
                log.debug("Exception:",e);
            }
        }
    }


    /**
     * The ContentItemManager coordinates the analysis of a content item. Starting from the initial state, it sends
     * analysis events to appropriate analysers as found in the dependency graph. A thread loop waits and only terminates
     * once all service requests are finished. In this case, the manager sends a content event response to the output queue
     */
    private class ContentItemManager extends DefaultConsumer implements Runnable {
        private ContentItem      item;
        private ContentItemState state;
        private String           queue;

        public ContentItemManager(ContentItem item, ContentItemState state, Channel channel) throws IOException {
            super(channel);
            this.item = item;
            this.state = state;

            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            getChannel().basicConsume(queue, false, this);
        }


        private void executeStateTransitions() throws IOException {
            log.info("looking for possible state transitions ...");
            if(state.isFinalState()) {
                synchronized (this) {
                    this.notifyAll();
                }

                // send finish event
                getChannel().basicPublish("", EventManager.QUEUE_CONTENT_OUTPUT, null, Event.ContentEvent.newBuilder().setContentItemUri(item.getURI().stringValue()).build().toByteArray());
            } else {
                for (Transition t : state.getPossibleTransitions()) {
                    log.debug("- transition: {}", t);

                    String correlationId = UUID.randomUUID().toString();
                    state.addProgress(correlationId, t);

                    AMQP.BasicProperties ciProps = new AMQP.BasicProperties.Builder()
                            .correlationId(correlationId)
                            .replyTo(queue)
                            .build();

                    Event.AnalysisEvent analysisEvent = Event.AnalysisEvent.newBuilder()
                            .setContentItemUri(item.getURI().stringValue())
                            .setObjectUri(t.getObject().stringValue())
                            .setServiceId(t.getService().getUri().stringValue()).build();

                    getChannel().basicPublish("", t.getService().getQueueName(), ciProps, analysisEvent.toByteArray());

                    // remove transition, as it is being processed
                    state.removeState(t.getObject());
                }
            }

        }

        /**
         * Handle response of a service with an analysis event in the replyto queue
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Event.AnalysisEvent analysisResponse = Event.AnalysisEvent.parseFrom(body);
            log.info("received processing result from service {} for content item {}: new object {}", analysisResponse.getServiceId(), analysisResponse.getContentItemUri(), analysisResponse.getObjectUri());

            try {
                state.addState(new URIImpl(analysisResponse.getObjectUri()), dependencies.getTargetState(new URIImpl(analysisResponse.getServiceId())));
                state.removeProgress(properties.getCorrelationId());

                executeStateTransitions();
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } catch (StateNotFoundException e) {
                log.warn("could not proceed analysing content item {}, part {}; next state unknown because service was not registered", analysisResponse.getContentItemUri(), analysisResponse.getObjectUri());
            }
        }

        @Override
        public void run() {
            try {
                getChannel().confirmSelect();
                executeStateTransitions();

                if(!state.isFinalState()) {
                    // wait until executeStateTransitions notifies us of being finished
                    synchronized (this) {
                        this.wait();
                    }
                }
            } catch (IOException ex) {
                log.error("could not start processing of content item {} (message: {})", item.getURI().stringValue(), ex.getMessage());
                log.debug("Exception:",ex);
            } catch (InterruptedException e) {
                log.error("analysis of content item {} was interrupted ...", item.getURI().stringValue());
            }
        }
    }
}
