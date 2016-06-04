/*
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

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.*;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.exception.StateNotFoundException;
import eu.mico.platform.broker.model.*;
import eu.mico.platform.broker.util.RabbitMQUtils;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * MICO Message Broker, orchestrating the communication between analysis services and the analysis workflow for content
 * items using RabbitMQ.
 * <p/>
 * Maintains a dependency graph of registered services based on the input and output they provide. For each content item,
 * uses this dependency graph to execute the analysis workflow.
 * <p/>
 * Maintains the following RabbitMQ exchanges:
 * - service_registry: an exchange where the event api sends new service registration events
 * - service_discovery: an exchange where the message broker sends a discovery request on startup and event managers
 * respond with their service lists
 * <p/>
 * Maintains the following RabbitMQ queues:
 * - content item input queue: the broker creates the queue if it does not exist and registers itself as a consumer for
 * newly injected content items
 * - content item replyto queue: the broker creates a new temporary queue for each content item it processes and sets it as
 * replyto queue for services analysing this content item; it registers itself as consumer
 * so it can forward results to the next analysers in the analysis graph
 * - registry queue bound to service_registry: the broker registers itself as consumer to get notified about newly
 * registered services
 * - replyto queue for each service discovery event: temporarily created when a service discovery is in process; event
 * managers will respond to this queue about their services; the broker registers itself as consumer to get notified;
 * queue is cleaned up automatically when it is no longer used.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MICOBrokerImpl implements MICOBroker {

    private static Logger log = LoggerFactory.getLogger(MICOBrokerImpl.class);

    private String host;
    private String vhost;
    private String user;
    private String password;
    private int rabbitPort;

    private String marmottaBaseUri;
    private String storageBaseUri;

    // a graph for representing the currently registered service dependencies
    private ServiceGraph dependencies;


    private Connection connection;

    private PersistenceService persistenceService;

    private Channel registryChannel, discoveryChannel, contentChannel, configChannel;

    // map from content item URIs to processing states
    private Map<String, ItemState> states;

    // map from content item URIs to channels
    private Map<String, Channel> channels;


    public MICOBrokerImpl(String host) throws IOException, URISyntaxException {
        this(host, "mico", "mico");
    }

    public MICOBrokerImpl(String host, String user, String password) throws IOException, URISyntaxException {
        this(host, user, password, 5672, "http://" + host + ":8080/marmotta", "hdfs://" + host + ("/"));
    }
    public MICOBrokerImpl(String host, String user, String password, int rabbitPort, String marmottaBaseUri, String storageBaseUri) throws IOException, URISyntaxException {
        this(host, "/", user, password, rabbitPort, marmottaBaseUri, storageBaseUri);
    }

    public MICOBrokerImpl(String host, String vhost, String user, String password, int rabbitPort, String marmottaBaseUri, String storageBaseUri) throws IOException, URISyntaxException {
        this.host = host;
        this.vhost = vhost;
        this.user = user;
        this.password = password;
        this.rabbitPort = rabbitPort;
        this.marmottaBaseUri = marmottaBaseUri;
        this.storageBaseUri = storageBaseUri;

        dependencies = new ServiceGraph();
        states = new ConcurrentHashMap<>();
        channels = new HashMap<>();

        log.info("initialising RabbitMQ connection ...");


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setVirtualHost(this.vhost);
        factory.setPort(this.rabbitPort);
        factory.setUsername(this.user);
        factory.setPassword(this.password);

        connection = factory.newConnection();

        initConfigQueue();
        initRegistryQueue();
        initDiscoveryQueue();
        initItemQueue();

        log.info("try initialising persistence service ...");
        try{
            persistenceService = new PersistenceServiceAnno4j(new java.net.URI(this.marmottaBaseUri), new java.net.URI(this.storageBaseUri));
            log.info("initialising persistence service ... finished: {}", persistenceService);
        }catch(Throwable e){
            log.error("unable to initialize persistenceService",e);
        }finally{
            if (persistenceService == null ){
                log.info("-------- persistenceService should be initialized ------- ");
            }
        }
    }


    @Override
    public ServiceGraph getDependencies() {
        return dependencies;
    }


    @Override
    public Map<String, ItemState> getStates() {
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
        discoveryChannel.basicPublish(EventManager.EXCHANGE_SERVICE_DISCOVERY, "", props, Event.DiscoveryEvent.newBuilder().build().toByteArray());

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


    private void initItemQueue() throws IOException {
        log.info("setting up content injection queue ...");
        contentChannel = connection.createChannel();

        // create the input and output queue with a defined name
        contentChannel.queueDeclare(EventManager.QUEUE_CONTENT_INPUT, true, false, false, null);
        contentChannel.queueDeclare(EventManager.QUEUE_PART_OUTPUT, true, false, false, null);

        // register a content item consumer with the queue
        contentChannel.basicConsume(EventManager.QUEUE_CONTENT_INPUT, false, new ItemConsumer(contentChannel));
    }

    private void initConfigQueue() throws IOException {
        log.info("setting up configuration request queue ...");
        configChannel = connection.createChannel();

        try {
            configChannel.queueDeclare(EventManager.QUEUE_CONFIG_REQUEST, false, true, false, null);
            configChannel.basicConsume(EventManager.QUEUE_CONFIG_REQUEST, false, new ConfigurationRequestConsumer(configChannel));
        } catch (IOException e) {
            if (RabbitMQUtils.isCausedByChannelCloseException(e, 405)) {
                log.info("There is already a broker running. Retrieving central configuration.");
                // The Channes has already been closed, so we need a new one.
                Channel channel = connection.createChannel();
                try {
                    try {
                        RpcClient configClient = new RpcClient(channel, "", EventManager.QUEUE_CONFIG_REQUEST, 5000);
                        try {
                            log.info("Retrieving Marmotta and storage configuration...");
                            Event.ConfigurationEvent config = Event.ConfigurationEvent.parseFrom(configClient.primitiveCall(Event.ConfigurationDiscoverEvent.newBuilder().build().toByteArray()));

                            boolean diff = false;
                            log.info("Got Marmotta base URI: {}", config.getMarmottaBaseUri());
                            if (StringUtils.equals(this.marmottaBaseUri, config.getMarmottaBaseUri())) {
                                log.info("All brokers agree on the marmottaBaseUri: <{}>", marmottaBaseUri);
                            } else {
                                log.warn("Other broker reports different marmottaBaseUri: <{}> != <{}> (mine), using the new value", config.getMarmottaBaseUri(), marmottaBaseUri);
                                this.marmottaBaseUri = config.getMarmottaBaseUri();
                                diff = true;
                            }
                            log.info("Got storage base URI: {}", config.getStorageBaseUri());
                            if (StringUtils.equals(this.storageBaseUri, config.getStorageBaseUri())) {
                                log.info("All brokers agree on the storageBaseUri: <{}>", storageBaseUri);
                            } else {
                                log.warn("Other broker reports different storageBaseUri: <{}> != <{}> (mine), using the new value", config.getStorageBaseUri(), storageBaseUri);
                                this.storageBaseUri = config.getStorageBaseUri();
                                diff = true;
                            }
                            if (diff) {
                                log.warn("!!! Local settings for marmottaBaseUri and/or storageUri overwritten !!!");
                                log.warn("!!! marmottaBaseUri: <{}>", marmottaBaseUri);
                                log.warn("!!! storageUri: <{}>", storageBaseUri);
                                log.warn("!!!");
                            }
                        } finally {
                            configClient.close();
                        }
                    } catch (TimeoutException e1) {
                        throw new IllegalStateException("ConfigQueue exists but no one ins answering there", e);
                    }
                } finally {
                    if (channel.isOpen()) channel.close();
                }
            } else {
                throw e;
            }
        }
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

            if (registrationEvent.getType() == Event.RegistrationType.REGISTER) {
                log.info("service registration of service {} (in: {}, out: {}, queue: {})", registrationEvent.getServiceId(), registrationEvent.getRequires(), registrationEvent.getProvides(), registrationEvent.getQueueName());

                ServiceDescriptor svc = new ServiceDescriptor(registrationEvent);
                TypeDescriptor tin = new TypeDescriptor(registrationEvent.getRequires());
                TypeDescriptor tout = new TypeDescriptor(registrationEvent.getProvides());

                if (!dependencies.containsEdge(svc) && dependencies.addEdge(tin, tout, svc)) {
                    log.info("- adding service {} to dependency graph, as it does not exist yet", svc.getUri());
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

    private class ConfigurationRequestConsumer extends DefaultConsumer {
        public ConfigurationRequestConsumer(Channel channel) {
            super(channel);
        }

        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            log.info("received config discovery event with reply queue {} ...", properties.getReplyTo());

            // parse config discovery request (as it doesn't contain any data for now we won't care about it further)
            Event.ConfigurationDiscoverEvent configDiscover = Event.ConfigurationDiscoverEvent.parseFrom(body);

            // construct reply properties, use the same correlation ID as in the request
            final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();

            // construct configuration event
            Event.ConfigurationEvent config = Event.ConfigurationEvent.newBuilder()
                    .setMarmottaBaseUri(marmottaBaseUri)
                    .setStorageBaseUri(storageBaseUri)
                    .build();

            // send configuration
            getChannel().basicPublish("", properties.getReplyTo(), replyProps, config.toByteArray());

            // ack request
            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }

    }


    /**
     * Handle the injection of new content items into the system by watching the QUEUE_CONTENT_INPUT
     */
    private class ItemConsumer extends DefaultConsumer {
        public ItemConsumer(Channel channel) {
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
            Event.ItemEvent partEvent = Event.ItemEvent.parseFrom(body);

            log.info("new content item injected (URI: {}), preparing for analysis!", partEvent.getItemUri());
            try {
                if (persistenceService == null){
                    // happens during broker startup, if new Items where injected during broker down time
                    int i=0;
                    while (persistenceService == null){
                        if(i>10){
                            throw new RepositoryException("persistenceService initialization timeout");
                        }
                        log.warn("persistenceService not yet initialized!!! ... wait five seconds ({})", ++i);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("process was stopped during wait for persistenceService initialization");
                        }
                    }
                    log.info("persistenceService available: {}", persistenceService.getStoragePrefix());
                }

                Item item = getItem(new URIImpl(partEvent.getItemUri()));
                traceItem(item);
                if (checkItem(item))
                {

                    log.info("- adding initial content item state ...");
                    ItemState state = new ItemState(dependencies, item);
                    states.put(partEvent.getItemUri(), state);
    
                    log.info("- setting up messaging for content item analysis ...");
    
                    // create a new channel for the content item so it runs isolated from the other items
                    Channel channel = connection.createChannel();
    
                    log.info("- triggering analysis process for initial states ...");
                    ItemManager mgr = new ItemManager(item, state, channel);
                    Thread t = new Thread(mgr, "ItemManager_" + item.getURI().toString());
                    t.start();

                }else{ // something is wrong with the item, tell broker that we can not process it
                    ItemState state = new ItemState(dependencies, item);
                    states.put(partEvent.getItemUri(), state);

                }
            } catch (RepositoryException e) {
                log.error("could not load item from persistence layer (message: {})", e.getMessage());
                log.debug("Exception:", e);
            } catch (ClassCastException e) {
                log.error("could not cast item ({}) from persistence layer (message: {})",partEvent.getItemUri(), e.getMessage());
            } finally {
                // even when there was an error, we got it and tried to handle 
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            }
        }

    }


    /**
     * The ContentItemManager coordinates the analysis of a content item. Starting from the initial state, it sends
     * analysis events to appropriate analysers as found in the dependency graph. A thread loop waits and only terminates
     * once all service requests are finished. In this case, the manager sends a content event response to the output queue
     */
    private class ItemManager extends DefaultConsumer implements Runnable {
        private Item item;
        private ItemState state;
        private String queue;
        private String queueTag;

        public ItemManager(Item item, ItemState state, Channel channel) throws IOException {
            super(channel);
            this.item = item;
            this.state = state;

            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            queueTag = getChannel().basicConsume(queue, false, this);
        }


        private void executeStateTransitions() throws IOException {
            log.info("looking for possible state transitions ...");
            if (state.isFinalState()) {
                // send finish event
                log.info("state is final state ...");
                getChannel().basicPublish("", EventManager.QUEUE_PART_OUTPUT, null, Event.ItemEvent.newBuilder().setItemUri(item.getURI().stringValue()).build().toByteArray());

                synchronized (this) {
                    this.notifyAll();
                }
            } else {
                for (Transition t : state.getPossibleTransitions()) {
                    log.debug("- transition: {}", t);

                    String correlationId = UUID.randomUUID().toString();
                    state.addProgress(correlationId, t);

                    AMQP.BasicProperties ciProps = new AMQP.BasicProperties.Builder()
                            .correlationId(correlationId)
                            .replyTo(queue)
                            .build();

                    Event.AnalysisRequest analysisEvent = Event.AnalysisRequest.newBuilder()
                            .setItemUri(item.getURI().stringValue())
                            .addPartUri(t.getObject().stringValue())
                            .setServiceId(t.getService().getUri().stringValue()).build();

                    getChannel().basicPublish("", t.getService().getQueueName(), ciProps, analysisEvent.toByteArray());

                    // remove transition, as it is being processed
                    state.removeState(t.getObject());
                }
            }
        }

        /**
         * Handle response of a service with an analysis event in the reply to queue
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            try {
                Event.AnalysisEvent analysisResponse = Event.AnalysisEvent
                        .parseFrom(body);
                try {

                    switch (analysisResponse.getType()) {
                        case ERROR:
                        String errMsg = analysisResponse.getError().getMessage();
                        log.warn(errMsg,
                                    analysisResponse.getError().getDescription());
                            state.setError(errMsg);
                            state.removeProgress(properties.getCorrelationId());
                            break;
                        case NEW_PART:
                            setStateForContent(analysisResponse.getNew());
                            break;
                        case FINISH:
                            // this is the last message we are waiting for
                            state.removeProgress(properties.getCorrelationId());
                            break;
                        case PROGRESS:
                            Event.AnalysisEvent.Progress progress = analysisResponse.getProgress();
                            log.trace("got progress event ({}) for {}", progress.getProgress(), progress.getPartUri());
                            Transition transition = state.getProgress().get(properties.getCorrelationId());
                            transition.setProgress(progress.getProgress());
                    }

                } catch (StateNotFoundException e) {
                    e.printStackTrace();
                }
                executeStateTransitions();
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } catch (InvalidProtocolBufferException e) {
                log.warn("Error handling delivery", e);
            }
        }


        /**
         * update or set state for content part based on (mime-)type
         *
         * @param analysisResponse
         * @throws StateNotFoundException
         */
        private void setStateForContent(Event.AnalysisEvent.NewPart analysisResponse) throws StateNotFoundException {
            URIImpl itemUri = new URIImpl(analysisResponse.getItemUri());
            URIImpl partUri = new URIImpl(analysisResponse.getPartUri());
            String serviceId = analysisResponse.getServiceId();
            try {
                String mimetype = getItem(itemUri)
                        .getPart(partUri).getSyntacticalType();

                if (mimetype != null) {
                    TypeDescriptor newState = dependencies.getState(mimetype);
                    state.addState(partUri, newState);
                } else {
                    log.warn(
                            "Type not set for part {}, assume its type fits to produce value from service: {}.",
                            partUri, serviceId);
                    state.addState(partUri, dependencies.getTargetState(new URIImpl(serviceId)));
                }
            } catch (StateNotFoundException | RepositoryException e) {
                log.warn("unable to retrieve mimetype for {}, assume its type fits to produce value from service: {}.",
                        partUri, serviceId, e);
                state.addState(partUri, dependencies.getTargetState(new URIImpl(serviceId)));
            }
        }

        @Override
        public void run() {
            try {
                getChannel().confirmSelect();
                executeStateTransitions();

                if (!state.isFinalState()) {
                    // wait until executeStateTransitions notifies us of being finished
                    synchronized (this) {
                        this.wait();
                    }
                }
            } catch (IOException ex) {
                log.error("could not start processing of content item {} (message: {})", item.getURI().stringValue(), ex.getMessage());
                log.debug("Exception:", ex);
            } catch (InterruptedException e) {
                log.error("analysis of content item {} was interrupted ...", item.getURI().stringValue());
            } finally {
                try {
                    getChannel().basicCancel(this.queueTag);
                    getChannel().close();
                } catch (IOException e) {
                    log.error("unable to unregister queue and closing channel for content item {}", item.getURI().stringValue());
                }
            }
        }
    }

    /**
     * retrieve an item object with given uri
     * @param itemUri uri of the item
     * @return an item object
     * @throws RepositoryException if the returned item is null
     */
    private Item getItem(URIImpl itemUri) throws RepositoryException {
        Item item = persistenceService.getItem(itemUri);
        if (item == null) {
            throw new RepositoryException(
                    "persistenceService returned null for item with url: "
                            + itemUri);
        }
        return item;
    }

    private boolean checkItem(Item item) throws RepositoryException{
        boolean ret = true;
        if (item == null) {
            throw new RepositoryException("Unable to check an item which is null.");
        }
        String test = item.getSemanticType();
        if (test == null || test.isEmpty()) {
            log.warn("Semantic type of item {} must be set", item.getURI());
            ret= false;
        }
        test = item.getSyntacticalType();
        if (test == null || test.isEmpty()) {
            log.warn("Syntactical type of item {} must be set", item.getURI());
            ret= false;
        }
        if (item.hasAsset()){
            Asset asset = item.getAsset();
            if (asset == null ) {
                log.warn("Asset of item {} must not be null", item.getURI());
                ret= false;
            }
            test = asset.getFormat();
            if (test == null || test.isEmpty()) {
                log.warn("The Format of asset from item {} must be set", item.getURI());
                ret= false;
            }
            test = asset.getLocation().stringValue();
            if (test == null || test.isEmpty()) {
                log.warn("The location of asset from item {} must be set", item.getURI());
                ret= false;
            }
        }else{
            if(! item.getParts().iterator().hasNext()){
                log.warn("The item {} without an asset must have at least one part", item.getURI());
                ret= false;
            }
        }

        return ret;
    }
    private void traceItem(Item item) {
        if(item == null){
            log.debug("Unable to log an item which is null.");
        }else{
            log.trace("Item: {} semantic: {}  syntactic: {}",item.getURI(),item.getSemanticType(), item.getSyntacticalType());
        }
    }
    
    public String getRouteStatus(String camelRoute) {
    	//TODO implement
    	
    	log.warn("status check is not implemented yet, simply returning 'ONLINE'");
    	
    	//1. Parse the route
    	MICOCamelRoute route=new MICOCamelRoute();
    	route.parseCamelRoute(camelRoute);

    	return "ONLINE";
    }

}
