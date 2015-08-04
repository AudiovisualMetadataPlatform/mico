package eu.mico.platform.camel;

import java.io.IOException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisEvent;

/**
 * The MicoRabbitProducer produces mico analyze events 
 * and sends them to the extractors via RabbitMQ
 */
public class MicoRabbitProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(MicoRabbitProducer.class);
    private MicoRabbitEndpoint endpoint;

    public MicoRabbitProducer(MicoRabbitEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
    	LOG.info("{} - P R O D U C E analyze event and put it to msg body", endpoint.getEndpointKey());
    	String content = exchange.getIn().getBody(String.class);
    	log.warn("incomming CONTENT {}  \n  ... try to extract content and partURLs", content);
        AnalysisEvent event;
    	try{
    	    String[] lines = content.split("\r\n");
    	    String item = lines[0];
    	    String part = lines[1];
            LOG.info("try to generate event for {} {}",item,part);
    	    event = generateEvent(item,part);
    	}catch(Exception e){
    	    log.warn("unable to extract content item and part from message",e);
            event = generateEvent();
    	}
    	exchange.getIn().setBody(event);
        // create a new channel for the content item so it runs isolated from the other items
        Channel channel = endpoint.getConnection().createChannel();

    	
        AnalyseManager manager = new AnalyseManager(event, channel);
		manager.sendEvent();
        if(exchange.getPattern().equals(ExchangePattern.InOut)){
        	while(!manager.hasResponse()){
	        	LOG.info("..waiting for response..");
	        	Thread.sleep(300);
        	}
        }
        LOG.info("extraction finished");
    }
    
    
    public AnalysisEvent generateEvent() {
		LOG.info("generate PREDEFINED event");
        String item = "http://mico-platform:8080/marmotta/c04b7656-684d-44af-a3b7-a816ae45e7df";
        String part = "http://mico-platform:8080/marmotta/c04b7656-684d-44af-a3b7-a816ae45e7df/e61153e7-8618-472d-9c0b-b4a22d8249f4";
        return generateEvent(item, part);
    }

    private AnalysisEvent generateEvent(String item, String part) {
        Event.AnalysisEvent analysisEvent = Event.AnalysisEvent.newBuilder()
                .setContentItemUri(item)
                .setObjectUri(part)
                .setServiceId("wordcount").build();
    	
    	return analysisEvent;
    }

    /**
     * The ContentItemManager coordinates the analysis of a content item. Starting from the initial state, it sends
     * analysis events to appropriate analysers as found in the dependency graph. A thread loop waits and only terminates
     * once all service requests are finished. In this case, the manager sends a content event response to the output queue
     */
    private class AnalyseManager extends DefaultConsumer {
        private String           queue;
        private AnalysisEvent    event;
        private boolean response = false;

        public AnalyseManager(AnalysisEvent event, Channel channel) throws IOException {
            super(channel);
            this.event = event;

            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            log.info("listen on queue {} for result. ",queue);
            getChannel().basicConsume(queue, false, this);
            getChannel().confirmSelect();
        }


		public void sendEvent() throws IOException {
			String correlationId = UUID.randomUUID().toString();

			AMQP.BasicProperties ciProps = new AMQP.BasicProperties.Builder()
					.correlationId(correlationId).replyTo(queue).build();

			getChannel().basicPublish("", event.getServiceId(), ciProps,
					event.toByteArray());

		}

        /**
         * Handle response of a service with an analysis event in the replyto queue
         */
		@Override
		public void handleDelivery(String consumerTag, Envelope envelope,
				AMQP.BasicProperties properties, byte[] body)
				throws IOException {
			Event.AnalysisEvent analysisResponse = Event.AnalysisEvent
					.parseFrom(body);
			log.info(
					"received processing result from service {} for content item {}: new object {}",
					analysisResponse.getServiceId(),
					analysisResponse.getContentItemUri(),
					analysisResponse.getObjectUri());
			response=true;
			getChannel().basicAck(envelope.getDeliveryTag(), false);
		}

		@Override
		public void handleCancel(String consumerTag) throws IOException {
			log.warn("Cancel: {}",consumerTag);
			super.handleCancel(consumerTag);
		}

		@Override
		public void handleCancelOk(String consumerTag){ 
			log.warn("CancelOk: {}",consumerTag);
			super.handleCancelOk(consumerTag);
		}
		
		@Override
		public void handleConsumeOk(String consumerTag) {
			log.warn("ConsumeOk: {}",consumerTag);
			super.handleConsumeOk(consumerTag);
		}
		
		@Override
		public void handleRecoverOk(String consumerTag) {
			log.warn("RecoverOk: {}",consumerTag);
			super.handleRecoverOk(consumerTag);
		}
		
		@Override
		public void handleShutdownSignal(String consumerTag,
				ShutdownSignalException sig) {
			log.warn("ShutdownSignal: {} for consumer {}",sig,consumerTag);
			super.handleShutdownSignal(consumerTag, sig);
		}


		public boolean hasResponse() {
			return response;
		}


		public void setResponse(boolean hasResponse) {
			this.response = hasResponse;
		}
	}
}
