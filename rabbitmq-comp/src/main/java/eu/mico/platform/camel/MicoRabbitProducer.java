package eu.mico.platform.camel;

import java.io.IOException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisRequest;

/**
 * The MicoRabbitProducer produces mico analyze events 
 * and sends them to the extractors via RabbitMQ
 */
public class MicoRabbitProducer extends DefaultProducer {
    public static final String KEY_MICO_ITEM = "mico_item";
    public static final String KEY_MICO_PART = "mico_part";

    private static final Logger LOG = LoggerFactory.getLogger(MicoRabbitProducer.class);
    private MicoRabbitEndpoint endpoint;
    private String serviceId;

    public MicoRabbitProducer(MicoRabbitEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        this.serviceId = endpoint.getServiceId();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("P R O D U C E analyze event for {} and put it to msg body", serviceId);
        AnalysisRequest event;
        Message inItem = exchange.getIn();
        String item = inItem.getHeader(KEY_MICO_ITEM, String.class);
        String part = inItem.getHeader(KEY_MICO_PART, String.class);
        try {
            if (item == null){
                throw new Exception("no item found in header");
            }
            if (part == null) {
                log.debug("process item without part");
                part = item;
            }
            event = generateRequest(item, part);
        } catch (Exception e) {
            log.error("unable to extract content item and part uri from message: {}", e.getMessage());
            return;
        }
        inItem.setBody(event);
        // create a new channel for the content item
        // so it runs isolated from the other items
        Channel channel = endpoint.getConnection().createChannel();

        AnalyseManager manager = new AnalyseManager(event, channel);
        manager.sendEvent();
        if(exchange.getPattern().equals(ExchangePattern.InOut)||true){
            while (!manager.hasFinished()) {
                LOG.debug("..waiting for response..");

                synchronized (serviceId){
                    //Thread.sleep(300);
                    serviceId.wait();
                }
            }
            // save new part in header to tell next extractor to process that part
            inItem.setHeader(KEY_MICO_PART, manager.getNewObjectUri());
        }
        LOG.info("extraction finished - {}",item);
    }


    private AnalysisRequest generateRequest(String item, String part) {
        LOG.info("generate event for {} {}", serviceId, part);
        Event.AnalysisRequest analysisEvent = Event.AnalysisRequest.newBuilder()
                .setItemUri(item)
                .addPartUri(part)
                .setServiceId(serviceId).build();
    	
    	return analysisEvent;
    }

    /**
     * The ContentItemManager coordinates the analysis of a content item. Starting from the initial state, it sends
     * analysis events to appropriate analyzers as found in the dependency graph. A thread loop waits and only terminates
     * once all service requests are finished. In this case, the manager sends a content event response to the output queue
     */
    private class AnalyseManager extends DefaultConsumer {
        private String           queue;
        private AnalysisRequest    req;
        private boolean finished = false;
        private String newObjectUri = null;

        public AnalyseManager(AnalysisRequest event, Channel channel) throws IOException {
            super(channel);
            this.req = event;

            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            log.debug("listen on queue {} for result. ",queue);
            getChannel().basicConsume(queue, false, this);
            getChannel().confirmSelect();
        }

        public void sendEvent() throws IOException {
            String correlationId = UUID.randomUUID().toString();

            AMQP.BasicProperties ciProps = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId).replyTo(queue).build();

            getChannel().basicPublish("", req.getServiceId(), ciProps,
                    req.toByteArray());

        }

        /**
         * Handle response of a service with an analysis event in the replyto queue
         */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                AMQP.BasicProperties properties, byte[] body)
                throws IOException {
            Event.AnalysisEvent response = Event.AnalysisEvent
                    .parseFrom(body);

            switch (response.getType()) {
            case PROGRESS:
            case ERROR:
                log.warn(response.getError().getMessage(), response.getError()
                        .getDescription());
            case NEW_PART:
                newObjectUri = response.getNew().getPartUri();
                log.debug(
                        "received processing result from service {} for content item {}: new object {}",
                        response.getNew().getServiceId(), response.getNew()
                                .getItemUri(), newObjectUri);
                getChannel().basicAck(envelope.getDeliveryTag(), false);

            case FINISH:
                // analyze process finished, notify waiting threads to continue
                // camel route
                finished = true;
                synchronized (serviceId) {
                    serviceId.notify();
                }
            }
        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
            log.info("Cancel: {}", consumerTag);
            super.handleCancel(consumerTag);
        }

        @Override
        public void handleCancelOk(String consumerTag) {
            log.info("CancelOk: {}", consumerTag);
            super.handleCancelOk(consumerTag);
        }

        @Override
        public void handleConsumeOk(String consumerTag) {
            log.info("ConsumeOk: {}", consumerTag);
            super.handleConsumeOk(consumerTag);
        }

        @Override
        public void handleRecoverOk(String consumerTag) {
            log.info("RecoverOk: {}", consumerTag);
            super.handleRecoverOk(consumerTag);
        }

        @Override
        public void handleShutdownSignal(String consumerTag,
                ShutdownSignalException sig) {
            log.info("ShutdownSignal: {} for consumer {}", sig, consumerTag);
            super.handleShutdownSignal(consumerTag, sig);
        }

        public boolean hasFinished() {
            return finished;
        }

        public String getNewObjectUri() {
            return newObjectUri;
        }

    }
}
