package eu.mico.platform.camel;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisRequest;
import eu.mico.platform.event.model.Event.AnalysisRequest.ParamEntry;

/**
 * The MicoRabbitProducer produces mico analyze events 
 * and sends them to the extractors via RabbitMQ
 */
public class MicoRabbitProducer extends DefaultProducer {
    public static final String KEY_MICO_ITEM = "mico_item";
    public static final String KEY_STARTING_DIRECT = "mico_direct";

    private static final Logger LOG = LoggerFactory.getLogger(MicoRabbitProducer.class);
    private MicoRabbitEndpoint endpoint;
    private String queueId;
    private Map<String,String> parameters = new HashMap<String, String>();
    private ObjectMapper mapper = new ObjectMapper();

    public MicoRabbitProducer(MicoRabbitEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        this.queueId = endpoint.getQueueId();
        readParamsFromEndpoint();
    }

    private void readParamsFromEndpoint() {
        String paramString = endpoint.getParameters();
        if(paramString != null && paramString.length() > 1){
            try {
                log.info("params         = {}",paramString);
                paramString = URLDecoder.decode(paramString,"UTF-8");
                log.info("params decoded = {}",paramString);
                this.parameters = mapper.readValue(paramString,
                        new TypeReference<HashMap<String, String>>() {});
            } catch (IOException e) {
                log.info("Unable to parse parameters:{} ", paramString, e);
            }
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
    	
    	//be smart and do nothing with exchanges produced by yourself
    	if(exchange.getFromEndpoint() != null){
    		
    		Endpoint fromEndpoint =  exchange.getFromEndpoint();
    		
    		if(fromEndpoint instanceof MicoRabbitEndpoint){
    			MicoRabbitEndpoint from = (MicoRabbitEndpoint) fromEndpoint;
    			if(from.getExtractorId().contentEquals(endpoint.getExtractorId()) &&
    			   from.getExtractorVersion().contentEquals(endpoint.getExtractorVersion()) &&
    			   from.getModeId().contentEquals(endpoint.getModeId())){
    				return;
    			}
    		}
    	}
    	
    	/**
    	 * NOTE: 
    	 * - exchange.getIn contains inside its body an AnalysisRequest
    	 * - The AnalysisRequest is build as such:
    	 * 
    	 *  Event.AnalysisRequest analysisEvent = Event.AnalysisRequest.newBuilder()
         *      .setItemUri(item)    //uri of the item, matches the header KEY_MICO_ITEM
         *      .addPartUri(part1)   //...
         *      .addPartUri(part2)	 //i-th part the old service produced (to be analyzed)
         *      .addPartUri(part3)   //
         *      .setServiceId(queueId).build();    //queueId of the old service (to be overridden)
    	 * 
    	 */
    	
        LOG.info("P R O D U C E analyze event for {} and put it to msg body", queueId);
        
        Message inMessage = exchange.getIn();
        
        AnalysisRequest inEvent=AnalysisRequest.parseFrom(inMessage.getBody(byte[].class));
        String headerItemURI = inMessage.getHeader(KEY_MICO_ITEM, String.class);
        
        try {
            if (headerItemURI == null){
                throw new Exception("no item found in header");
            }
            if ((headerItemURI.contentEquals(inEvent.getItemUri())) == false ){
                throw new Exception("the input event for the current exchange refers to an item different than the one declared");
            }
            inEvent = generateInputRequest(inEvent);
        } catch (Exception e) {
            log.error("unable to extract content item and part lists from camel exchange: {}", e.getMessage());
            return;
        }
        inMessage.setBody(inEvent.toByteArray());
        
        // create a new channel for the content item
        // so it runs isolated from the other items
        Channel channel = endpoint.getConnection().createChannel();

        AnalyseManager manager = new AnalyseManager(exchange, inEvent, channel);
        manager.sendEvent();
        if(exchange.getPattern().equals(ExchangePattern.InOut)||true){
        	int createdOutPart = 0;
        	String prevNewObjectURI = null;
            while (!manager.hasFinished() && !manager.hasError()) {
                LOG.debug("..waiting for response..");

                synchronized (queueId){
                    queueId.wait();
                }
                
                //Here starts the horror story: 
                
                //For every new part BUT THE LAST ONE, trigger the next extractor in the chain
                
                
                if(!manager.hasError()){
                	
                	String newObjectURI=manager.getNewObjectUri();
                	
    	            if(newObjectURI != null){
    	            	
    	            	if( prevNewObjectURI != null){
    	            
    	                	Exchange outExchange = endpoint.createExchange(exchange);
    	                	outExchange.setFromEndpoint(endpoint);
    	                	
    	    	            Message outMessage = outExchange.getIn();
    	    	            outMessage.getHeaders().putAll(exchange.getIn().getHeaders());
    	            		outMessage.setBody(generateTemplateRequest(inEvent.getItemUri(), prevNewObjectURI).toByteArray());
	    	            	
    	            		//NOTE: here we produce an exchange to ourself, since we do not know which consumers are connected after us
	    	            	outExchange.getContext().createProducerTemplate().send((String)outExchange.getProperty(Exchange.TO_ENDPOINT),outExchange);
    	            	}
    	            	createdOutPart= createdOutPart +1;
    	            	prevNewObjectURI=newObjectURI;
    	            }
    	            
    	        }
            }
            
            if(!manager.hasError()){

                //if no part was produced
	            if(createdOutPart == 0){
	            	LOG.warn("No new part produced by service {}, stopping the current message routing.",queueId);
	            	
		            //stop the routing of the current exchange
		            Message outMessage = exchange.getIn();
		            outMessage.setBody(inMessage.getBody());
		            exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
		            return;
	            }
	            //else, trigger the processing of the last unprocessed part
	            
             	Message outMessage = exchange.getIn();
	            outMessage.getHeaders().putAll(exchange.getIn().getHeaders());
	            outMessage.setBody(generateTemplateRequest(inEvent.getItemUri(), prevNewObjectURI).toByteArray());

            }
            else{
            	exchange.getIn().setHeader("error_class",exchange.getException());
            	exchange.getIn().setBody(exchange.getException());
            	throw exchange.getException();
            }
            
        }
        LOG.info("extraction finished - {}",headerItemURI);
    }


    private AnalysisRequest generateTemplateRequest(String item, String part) {
        LOG.info("generate template of an AnalysisRequest for item {} on part {}", item, part);
        Event.AnalysisRequest analysisEvent = Event.AnalysisRequest.newBuilder()
                .setItemUri(item)
                .addPartUri(part)
                .setServiceId(queueId).build();
    	
    	return analysisEvent;
    }
    
    private AnalysisRequest generateInputRequest(AnalysisRequest oldRequest) {
        LOG.info("generate AnalysisRequest for service {} on item {} ( input resource(s): {} )", queueId, oldRequest.getItemUri(), oldRequest.getPartUriList());
        Event.AnalysisRequest analysisEvent = Event.AnalysisRequest.newBuilder()
                .setItemUri(oldRequest.getItemUri())
                .addAllPartUri(oldRequest.getPartUriList())
                .addAllParams(getParamEntries())
                .setServiceId(queueId)
                .build();
    	
    	return analysisEvent;
    }

    private Iterable<? extends ParamEntry> getParamEntries() {
        ArrayList<ParamEntry> entries = new ArrayList<ParamEntry>();
        eu.mico.platform.event.model.Event.AnalysisRequest.ParamEntry.Builder builder = Event.AnalysisRequest.ParamEntry.newBuilder();
        for(Map.Entry<String, String> entry : parameters.entrySet()){
            entries.add(builder.setKey(entry.getKey()).setValue(entry.getValue()).build());
        }  
        return entries;
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
        private boolean hasError = false;
        private String newObjectUri = null;
        private Exchange exchange = null;

        public AnalyseManager(Exchange exchange, AnalysisRequest event, Channel channel) throws IOException {
            super(channel);
            this.req = event;

            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            log.debug("listen on queue {} for result. ",queue);
            getChannel().basicConsume(queue, false, this);
            getChannel().confirmSelect();
            this.exchange = exchange;
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
                log.warn("Received an error response {}, generating a new MICOCamelAnalysisException with what message : {}",response.getError().getMessage(), response.getError()
                        .getDescription());


                hasError=true;
                finished = true;
                exchange.setProperty(Exchange.DUPLICATE_MESSAGE, new MICOCamelAnalysisException(queueId,
	                       response.getError().getMessage(), 
	                       response.getError().getDescription()));
                exchange.setException( 
                		new MICOCamelAnalysisException(queueId,
                				                       response.getError().getMessage(), 
                				                       response.getError().getDescription()));
                exchange.getIn().setBody(new MICOCamelAnalysisException(queueId,
                				                       response.getError().getMessage(), 
                				                       response.getError().getDescription()));
                exchange.getOut().setBody(new MICOCamelAnalysisException(queueId,
	                       response.getError().getMessage(), 
	                       response.getError().getDescription()));
                
                getChannel().basicAck(envelope.getDeliveryTag(), false);
                synchronized (queueId) {
                    queueId.notify();
                }
                break;
            case NEW_PART:
                newObjectUri = response.getNew().getPartUri();
                log.debug(
                        "received processing result from service {} for content item {}: new object {}",
                        response.getNew().getServiceId(), response.getNew()
                                .getItemUri(), newObjectUri);
                getChannel().basicAck(envelope.getDeliveryTag(), false);
                synchronized (queueId) {
                    queueId.notify();
                }
                break;
            case FINISH:
                // analyze process finished correctly, notify waiting threads to continue
                // camel route
            	getChannel().basicAck(envelope.getDeliveryTag(), false);
                finished = true;
                synchronized (queueId) {
                    queueId.notify();
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
        
        public boolean hasError() {
            return hasError;
        }

        public String getNewObjectUri() {
            return newObjectUri;
        }

    }
}
