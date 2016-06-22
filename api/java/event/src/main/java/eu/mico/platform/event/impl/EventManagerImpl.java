/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.event.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.Transaction;
import com.github.anno4j.model.namespaces.OADM;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.event.api.*;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisEvent;
import eu.mico.platform.event.model.Event.AnalysisRequest.ParamEntry;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.event.model.Event.MessageType;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Rupert Westenthaler (rwesten@apache.org)
 */
public class EventManagerImpl implements EventManager {

    private final int RPC_CONFIG_TIMEOUT = 5 * 1000;

    private static Logger log = LoggerFactory.getLogger(EventManagerImpl.class);

    private final String amqpHost;
    private final String amqpVHost;
    private final int amqpPort;
    private final String amqpUser;
    private final String amqpPassword;

    private java.net.URI marmottaBaseUri;
    private java.net.URI storageBaseUri;

    private PersistenceServiceAnno4j persistenceService;

    private Connection connection;
    private Channel registryChannel;

    private Map<AnalysisServiceBase, AnalysisConsumer> services;

    private DiscoveryConsumer discovery; //TODO: do we need this in the EventManagerImpl?

    public EventManagerImpl(String amqpHost) throws IOException {
        this(amqpHost, "mico", "mico");
    }

    public EventManagerImpl(String amqpHost, String amqpUser, String amqpPassword) throws IOException {
        this(amqpHost, 5672, null, amqpUser, amqpPassword);
    }

    public EventManagerImpl(String amqpHost, String amqpUser, String amqpPassword, String amqpVHost) throws IOException {
        this(amqpHost, 5672, amqpVHost, amqpUser, amqpPassword);
    }

    public EventManagerImpl(String amqpHost, int amqpPort, String amqpVHost, String amqpUser, String amqpPassword) throws IOException {
        this.amqpHost = amqpHost;
        this.amqpPort = amqpPort;
        this.amqpVHost = amqpVHost;
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

        if (amqpVHost != null) {
            factory.setVirtualHost(amqpVHost);
        }

        connection = factory.newConnection();

        getConfiguration();
        // no longer needed as we do now have the configuration service
        //Preconditions.checkArgument(marmottaBaseUri.getPort() == 8080, "The marmotta port has to be 8080.");
        this.persistenceService = new PersistenceServiceAnno4j(marmottaBaseUri, storageBaseUri);

        registryChannel = connection.createChannel();

        // make sure the service registry and discovery channels exists
        registryChannel.exchangeDeclarePassive(EXCHANGE_SERVICE_REGISTRY);
        registryChannel.exchangeDeclarePassive(EXCHANGE_SERVICE_DISCOVERY);

        // register a listener queue for this event manager on the discovery exchange so we can react to discovery requests
        discovery = new DiscoveryConsumer();
    }

    private void getConfiguration() throws IOException, TimeoutException, java.net.URISyntaxException {
        log.info("Retrieving Marmotta and storage configuration...");
        Channel configChannel = null;
        Event.ConfigurationEvent config;
        RpcClient configClient = null;
        try {
            configChannel = connection.createChannel();
            configClient = new RpcClient(configChannel, "", QUEUE_CONFIG_REQUEST, RPC_CONFIG_TIMEOUT);
            config = Event.ConfigurationEvent.parseFrom(configClient.primitiveCall(Event.ConfigurationDiscoverEvent.newBuilder().build().toByteArray()));
        } finally {
            try { //do not fail in close operations
                if (configClient != null) {
                    configClient.close();
                }
                if (configChannel != null) {
                    configChannel.close();
                }
            } catch (IOException e) {/*ignore*/}
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
        for (Map.Entry<AnalysisServiceBase, AnalysisConsumer> svc : services.entrySet()) {
            unregisterService(svc.getKey());
            svc.getValue().getChannel().close();
        }

        if (registryChannel != null && registryChannel.isOpen()) {
            registryChannel.close();
        }

        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    /**
     * Register the given service with the event manager.
     *
     * @param service
     */
    @Override
    public void registerService(AnalysisServiceBase service) throws IOException {
        log.info("registering new service {} with message brokers ...", service.getServiceID());

        Channel chan = connection.createChannel();

        // first declare a new input queue for this service using the service queue name, and register a callback
        String queueName = service.getQueueName() != null ? service.getQueueName() : UUID.randomUUID().toString();

        // then override its value for compatibility with camel //TODO: remove getQueue() from the api
        queueName = service.getExtractorID() + "-" + service.getExtractorVersion() + "-" + service.getExtractorModeID();

        // then create a new analysis consumer (auto-registered to its queue name)
        services.put(service, new AnalysisConsumer(service, queueName));


        // then send a registration message to the broker's "service_registry" exchange; all running brokers will
        // receive this message, assuming that they bound their queue to the registry exchange

        Event.RegistrationEvent registrationEvent =
                Event.RegistrationEvent.newBuilder()
                        .setType(Event.RegistrationType.REGISTER)
                        .setServiceId(service.getServiceID().stringValue())
                        .setExtractorId(service.getExtractorID())
                        .setExtractorModeId(service.getExtractorModeID())
                        .setExtractorVersion(service.getExtractorVersion())
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
    public void unregisterService(AnalysisServiceBase service) throws IOException {
        log.info("unregistering new service {} with message brokers ...", service.getServiceID());

        Channel chan = connection.createChannel();

        // first declare a new input queue for this service using the service queue name, and register a callback
        String queueName = service.getQueueName() != null ? service.getQueueName() : UUID.randomUUID().toString();

        // then override its value for compatibility with camel //TODO: remove getQueue() from the api
        queueName = service.getExtractorID() + "-" + service.getExtractorVersion() + "-" + service.getExtractorModeID();

        // then create a new analysis consumer (auto-registered to its queue name)
        services.put(service, new AnalysisConsumer(service, queueName));


        // then send a registration message to the broker's "service_registry" exchange; all running brokers will
        // receive this message, assuming that they bound their queue to the registry exchange

        Event.RegistrationEvent registrationEvent =
                Event.RegistrationEvent.newBuilder()
                        .setType(Event.RegistrationType.UNREGISTER)
                        .setServiceId(service.getServiceID().stringValue())
                        .setExtractorId(service.getExtractorID())
                        .setExtractorModeId(service.getExtractorModeID())
                        .setExtractorVersion(service.getExtractorVersion())
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
        ObjectConnection con = item.getObjectConnection();
        try {
            if (con.isActive()) {
                con.commit(); //commit the current state of the item before notifying the broker
                con.begin();
            }
        } catch (RepositoryException e) {
            throw new IOException("Unable to commit Item data before inject", e);
        } //else  auto commit ... nothing to do 
        traceRDF(item, "injected");
        Channel chan = connection.createChannel();
        chan.basicPublish("", EventManager.QUEUE_CONTENT_INPUT, null, Event.ItemEvent.newBuilder().setItemUri(item.getURI().stringValue()).build().toByteArray());
        chan.close();
    }

    private void traceRDF(Item item, String taskName) {
        if (!log.isTraceEnabled()) {
            return;
        }
        //we copy all statements to a TreeModel as this one sorts them by SPO
        //what results in a much nicer TURTLE serialization
        final Model model = new TreeModel();
        //we also set commonly used namespaces
        model.setNamespace(OADM.PREFIX, OADM.NS);
        model.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
        model.setNamespace(RDFS.PREFIX, RDF.NAMESPACE);
        model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNamespace(MMM.PREFIX, MMM.NS);
        model.setNamespace("test", "http://localhost/mem/");
        model.setNamespace("services", "http://www.mico-project.eu/services/");
        model.setNamespace("fam", "http://vocab.fusepool.info/fam#");
        RepositoryConnection con = item.getObjectConnection();
        boolean startedTransaction = false;
        try {
            if (!con.isActive()) {
                con.begin();
                startedTransaction = true;
            }
            con.exportStatements(null, null, null, true, new RDFHandlerBase() {
                @Override
                public void handleStatement(Statement st) {
                    model.add(st);
                }
            });
        } catch (RDFHandlerException | RepositoryException e) {
            log.trace("Unable to LOG RDF for task " + taskName + " because " + e.getMessage(), e);
        } finally {
            if (startedTransaction) {
                try {
                    con.rollback();
                } catch (RepositoryException e) {/* ignore */}
            }
        }
        try (StringWriter rdfOut = new StringWriter()) {
            Rio.write(model, rdfOut, RDFFormat.TURTLE);
            log.debug("--- START {} RDF item: {} ---\n{}\n--- END {} RDF ---",
                    taskName, item.getURI(), rdfOut.toString(), taskName);
        } catch (RDFHandlerException e) {
            log.trace("Unable to serialize RDF for task " + taskName + " because " + e.getMessage(), e);
        } catch (IOException e) {
            log.trace("Unable to print serialized RDF for task " + taskName + " because " + e.getMessage(), e);
        }
    }

    /**
     * A consumer reacting to service discovery requests. Upon initialisation, it creates its own queue and binds it to
     * the service discovery exchange. Upon a discovery event, it simply sends back its list of services to the replyTo
     * queue provided in the discovery request.
     *
     * TODO: currently this is unused by this implementation ... should we remove this cass?
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


            for (Map.Entry<AnalysisServiceBase, AnalysisConsumer> svc : services.entrySet()) {
                log.info("- discover service {} ...", svc.getKey().getServiceID());

                //Override the queue declared by the service
                String queueName = svc.getKey().getExtractorID() + "-" +
                        svc.getKey().getExtractorVersion() + "-" +
                        svc.getKey().getExtractorModeID();


                Event.RegistrationEvent registrationEvent =
                        Event.RegistrationEvent.newBuilder()
                                .setServiceId(svc.getKey().getServiceID().stringValue())
                                .setExtractorId(svc.getKey().getExtractorID())
                                .setExtractorModeId(svc.getKey().getExtractorModeID())
                                .setExtractorVersion(svc.getKey().getExtractorVersion())
                                .setQueueName(queueName)
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
            private boolean finished = false;
            private boolean hasNew = false;
            private AnalysisEvent.Error errorMessage = null;
            private boolean sentError = false;
            private final BasicProperties properties;
            private final BasicProperties replyProps;
            private long progressSentMS = 0;
            private final long progressDeltaMS = 1000;

            private AnalysisResponseImpl(BasicProperties properties, BasicProperties replyProps) {
                this.properties = properties;
                this.replyProps = replyProps;
            }

            @Override
            public void sendFinish(Item item) throws IOException, RepositoryException {
                if (item == null) {
                    throw new NullPointerException("The parsed Item MUST NOT be NULL!");
                }
                if (isFinished() | isError()) {
                    throw new IllegalStateException("Unable to send finished as this AnalysisResponse is already in " +
                            (isError() ? "an error" : "a finished") + "  state");
                }
                // NOTE: This does NOT close or rollback the connection in case
                //       of an IOException. It is expected that the AnalysisService
                //       does deal with IOExceptions and try sendError
                ObjectConnection con = item.getObjectConnection();
                //first commit the RDF data to the RDF repository
                if (con.isActive()) {
                    con.commit();
                } //else no transaction active we do not need to commit

                //send the finished event
                AnalysisEvent.Finish finishMsg = AnalysisEvent.Finish.newBuilder()
                        .setServiceId(service.getServiceID().stringValue())
                        .setItemUri(item.getURI().stringValue())
                        .build();

                AnalysisEvent analysisEvent = AnalysisEvent.newBuilder()
                        .setFinish(finishMsg)
                        .setType(MessageType.FINISH)
                        .build();

                getChannel().basicPublish("", properties.getReplyTo(), replyProps, analysisEvent.toByteArray());
                finished = true;
                //after finishing a response we need to close the connection as
                //the analysis service MUST NOT modify the Item after sending a
                //finished event!
                try { //
                    con.close();
                } catch (RepositoryException e) {/* do not fail on closing connections */}
            }

            @Override
            public void sendProgress(Item item, URI part, float progress) throws IOException {
                if (item == null) {
                    throw new NullPointerException("The parsed Item MUST NOT be NULL!");
                }
                if (part == null) {
                    throw new NullPointerException("The parsed part URI MUST NOT be NULL!");
                }
                if (isFinished() | isError()) {
                    throw new IllegalStateException("Unable to send progress as this AnalysisResponse is already in " +
                            (isError() ? "an error" : "a finished") + "  state");
                }
                long current = System.currentTimeMillis();
                if (current < progressDeltaMS + progressSentMS) {
                    // prevent broker from progress flooding and wait at least deltaMS millis
                    log.trace("suppress sending progress: {} for object: {}", progress, part);
                    return;
                }
                log.trace("sending progress of: {} for object: {}", progress, part);
                if (progress > 1f) {
                    log.warn("progress should not be grater then 1.0 but is: {}", progress);
                    //TODO: maybe we should set progress to 1f here!
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
            public void sendNew(Item item, URI part) throws IOException, RepositoryException {
                if (item == null) {
                    throw new NullPointerException("The parsed Item MUST NOT be NULL!");
                }
                if (part == null) {
                    throw new NullPointerException("The parsed part URI MUST NOT be NULL!");
                }
                if (isFinished() | isError()) {
                    throw new IllegalStateException("Unable to send progress as this AnalysisResponse is already in " +
                            (isError() ? "an error" : "a finished") + "  state");
                }
                ObjectConnection con = item.getObjectConnection();
                if (con.isActive()) {
                    con.commit(); //save the current state
                    con.begin(); //start a new transaction
                }  //else no transaction active we do not need to commit
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
                hasNew = true;
                log.debug(" - sentNew [service: {} | item: {} | part: {}]", service, item.getURI(), part);
            }

            @Override
            public void sendError(Item item, AnalysisException e) throws IOException {
                if (e != null) {
                    sendError(item, e.getCode(), e.getMessage(), e.getCause());
                } else {
                    sendError(item, ErrorCodes.UNEXPECTED_ERROR, "", "");
                }
            }

            @Override
            public void sendError(Item item, ErrorCodes code, String msg, Throwable t) throws IOException {
                if (t != null) {
                    StringWriter writer = new StringWriter();
                    t.printStackTrace(new PrintWriter(writer));
                    sendError(item, code, msg, writer.toString());
                } else {
                    sendError(item, code, msg, "");
                }
            }

            @Override
            public void sendError(Item item, ErrorCodes code, String msg, String desc) throws IOException {
                if (sentError) { //ignore additional errors if already in an error state
                    return;
                }
                if (item == null) {
                    throw new NullPointerException("The parsed Item MUST NOT be NULL!");
                }
                ObjectConnection con = item.getObjectConnection();
                try {
                    try {
                        if (con.isActive()) {
                            con.rollback();
                        }
                    } catch (RepositoryException e) {
                        //we MUST NOT Fail if we can't rollback as we need to send the error response!
                        //actually getting RepositoryExceptions from the connection might be the
                        //reason why the caller has called sendError() in the first place
                        log.warn("Unable to rollback transaction for Item {} after Error[code: {}, msg: {}] - reason: {}",
                                item.getURI(), code, msg, e.getMessage());
                        log.debug("TACKTRACE: ", e);
                    }
                    if (errorMessage == null) {
                        errorMessage = AnalysisEvent.Error.newBuilder()
                                .setItemUri(item.getURI().stringValue())
                                .setServiceId(service.getServiceID().stringValue())
                                .setErrorCode(code)
                                .setMessage(msg)
                                .setDescription(desc).build();
                    } //else ... always try to send the first error
                    AnalysisEvent event = AnalysisEvent.newBuilder()
                            .setType(MessageType.ERROR)
                            .setError(errorMessage).build();
                    getChannel().basicPublish("", properties.getReplyTo(), replyProps, event.toByteArray());
                    sentError = true;
                    log.debug(" - sentError [service: {} | item: {} | code: {}]", service, item.getURI(),
                            errorMessage.getErrorCode());
                } finally {
                    //after an error the connection is no longer needed so close it
                    try {
                        con.close();
                    } catch (RepositoryException e) {/* ignore */}
                }
            }

            @Override
            public boolean hasNew() {
                return hasNew;
            }

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public boolean isError() {
                return errorMessage != null;
            }

            /**
             * If an error message was successfully sent. NOTE: In difference to
             * {@link #isError()} this will only return <code>true</code> if
             * {@link #sendError(Item, ErrorCodes, String, String)} was called
             * and successfully sent the error message. {@link #isError()}
             * returns <code>true</code> as soon as 
             * {@link #sendError(Item, ErrorCodes, String, String)} was called.
             * @return only <code>true</code> if an error message was successfully sent
             */
            boolean sentError() {
                return sentError;
            }
        }

        private AnalysisServiceBase service;
        private String queueName;

        public AnalysisConsumer(AnalysisServiceBase service, String queueName) throws IOException {
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

            AnalysisResponseImpl response = new AnalysisResponseImpl(properties, replyProps);
            final URI itemUri = new URIImpl(analysisRequest.getItemUri());
            Item item = null;
            try {
                item = persistenceService.getItem(itemUri);
            } catch (RepositoryException e) {
                log.warn("RepositoryException while creating Item {} on "
                                + "RepositoryService (message: {})",
                        analysisRequest.getItemUri(), e.getMessage());
            } finally {
                if (item == null || response == null) {
                    requeue(envelope, analysisRequest);
                    return; //stop here
                }
            }

            boolean reEnqueue = false; //if the message should be re-enqueued
            try {
                final List<Resource> resourceList = parseResourceList(analysisRequest.getPartUriList(), item);
                final Map<String, String> params = parseParams(analysisRequest);

                // Call relevant extractor execution
                if (service instanceof AnalysisServiceAnno4j) {
                    executeAnno4jExtractor(envelope, resourceList, params, response, item, (AnalysisServiceAnno4j) service);
                } else {
                    executeExtractor(envelope, resourceList, params, response, item, (AnalysisService) service);
                }

                if (!response.isFinished() && !response.isError()) {
                    //the lazy AnalysisService implementor was not calling
                    //response.sendFinish(item) ... so lets help him out
                    // ... but not without complaining!
                    log.info("AnalysisService {} has not sent a finished message "
                                    + "for Item {}. Please adapt the implementation to "
                                    + "that finished events are sent. Until than the "
                                    + "EventManager will care about sending the finished "
                                    + "event on behalf of the AnalysisService",
                            service.getClass().getName(), analysisRequest.getItemUri());
                    response.sendFinish(item);
                }
            } catch (AnalysisException e) {
                String errorMsg = new StringBuilder("AnalysisException while processing item ")
                        .append(analysisRequest.getItemUri()).append(" with service ")
                        .append(service.getServiceID()).append(" (message: ")
                        .append(e.getMessage()).append(")").toString();
                log.error(errorMsg);
                log.debug("STACKTRACE:", e);
                //for those errors we do not want to try again
                if (!response.sentError()) { //so just check if no error was sent yet
                    response.sendError(item, e);
                } //else an error message was already sent
            } catch (RuntimeException e) {
                String errorMsg = new StringBuilder("Could not analyse item with URI ")
                        .append(analysisRequest.getItemUri()).append(" with service ")
                        .append(service.getServiceID()).append(" (exception: ")
                        .append(e.getClass().getSimpleName()).append(" | message: ")
                        .append(e.getMessage()).append(")").toString();
                log.error(errorMsg);
                log.debug("STACKTRACE:", e);
                //for those errors we do not want to try again
                if (!response.sentError()) { //so just check if no error was sent yet
                    response.sendError(item, ErrorCodes.UNEXPECTED_ERROR, errorMsg, e);
                } //else an error message was already sent
            } catch (RepositoryException e) {
                log.warn("Encountered error while reading/writing to RDF repository while processing "
                        + "Item {} (message: {})", analysisRequest.getItemUri(), e.getMessage());
                log.debug("STACKTRACE:", e);
                //for RepositoryExceptions we actually would like to try again.
                //however we can only to this if we have not sent any messages
                //to the broker
                reEnqueue = !response.sentError() && !response.isFinished() && !response.hasNew();
            } finally { //make sure we come to a clean end whatever happend ...
                if (reEnqueue) {
                    requeue(envelope, analysisRequest);
                } else { //we need to ack the message ...
                    try { // ... but first check if we might also need to send an error
                        if (!response.isFinished() && !response.sentError()) {
                            //somehow no response was sent up to now ...
                            if (!response.isError()) {
                                // ... this can happen in case of an Error (e.g. OutOfMemoryError)
                                log.warn("Analysis request for service {} and item {} (message:{}) "
                                                + "terminated unfinished and without an error. EventManager "
                                                + "will sent an Error message", service,
                                        item.getURI(), envelope.getDeliveryTag());
                            } //else error was raised but not sent (because of an IOException)
                            response.sendError(item, ErrorCodes.UNEXPECTED_ERROR,
                                    "Analyze process of " + service.getClass().getName()
                                            + " has not finished for an unknown reason", "");
                        }
                    } finally {
                        traceRDF(item, "after call to " + service.getServiceID());
                        getChannel().basicAck(envelope.getDeliveryTag(), false);
                        log.trace("ack message: {}[item: {}]", envelope.getDeliveryTag(),
                                analysisRequest.getItemUri());
                    }
                }
            }
        }

        private void executeAnno4jExtractor(Envelope envelope, List<Resource> resourceList, Map<String, String> params, AnalysisResponseImpl response, Item item, AnalysisServiceAnno4j service) throws RepositoryException, IOException, AnalysisException {
            Anno4j anno4j = persistenceService.getAnno4j();
            Transaction transaction = anno4j.adoptTransaction(item.getRDFObject().getObjectConnection());
            transaction.begin();

            log.debug("> process Item {} (message: {})", item.getURI(), envelope.getDeliveryTag());
            traceRDF(item, "before call to " + service.getServiceID());

            log.debug(" - call {} with Item {}", service.getClass().getName(), item.getURI());
            service.call(response, item, resourceList, params, transaction);

            // Transaction will be committed by the sendFinished method

        }

        private void executeExtractor(Envelope envelope, List<Resource> resourceList, Map<String, String> params, AnalysisResponseImpl response, Item item, AnalysisService service) throws IOException, AnalysisException, RepositoryException {

            // legacy implementation of non-anno4j extractors
            ObjectConnection con = item.getRDFObject().getObjectConnection();
            //make sure we have everything we need to continue
            if (con == null) { //nope ... re-queue
                throw new RepositoryException("Couldn't get object connection from item.");
            } //else everything we need was correctly initialized

            log.debug("> process Item {} (message: {})", item.getURI(), envelope.getDeliveryTag());
            traceRDF(item, "before call to " + service.getServiceID());

            con.begin(); //start a transaction on the connection
            log.debug(" - call {} with Item {}", service.getClass().getName(), item.getURI());
            service.call(response, item, resourceList, params);

            // Connection will be committed by the sendFinished method
        }

        private Map<String, String> parseParams(Event.AnalysisRequest analysisRequest) {
            final Map<String, String> params = new HashMap<>();
            for (ParamEntry entry : analysisRequest.getParamsList()) {
                params.put(entry.getKey(), entry.getValue());
            }
            return params;
        }

        private void requeue(Envelope envelope, Event.AnalysisRequest analysisRequest) throws IOException {
            log.info("Requeue message: {}[item: {}]", envelope.getDeliveryTag(),
                    analysisRequest.getItemUri());
            getChannel().basicNack(envelope.getDeliveryTag(), false, true);
        }

        private List<Resource> parseResourceList(List<String> resourceUriList, Item item) throws RepositoryException {
            List<Resource> resources = new ArrayList<>();

            for (String resourceUri : resourceUriList) {
                if (item.getURI().toString().equals(resourceUri)) {
                    resources.add(item);
                } else {
                    resources.add(item.getPart(new URIImpl(resourceUri)));
                }
            }
            return resources;
        }
    }
}
