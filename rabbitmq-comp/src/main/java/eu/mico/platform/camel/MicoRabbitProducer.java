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

package eu.mico.platform.camel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

import eu.mico.platform.event.model.Event;
import eu.mico.platform.event.model.Event.AnalysisEvent.Progress;
import eu.mico.platform.event.model.Event.AnalysisRequest;
import eu.mico.platform.event.model.Event.AnalysisRequest.ParamEntry;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;
import eu.mico.platform.persistence.util.URITools;

/**
 * The MicoRabbitProducer produces mico analyze events 
 * and sends them to the extractors via RabbitMQ
 */
public class MicoRabbitProducer extends DefaultProducer {
    public static final String KEY_MICO_ITEM = "mico_item";
    public static final String KEY_MICO_PARTS_NEW = "mico_parts_new";
    public static final String KEY_STARTING_DIRECT = "mico_direct";

    private static final Logger LOG = LoggerFactory.getLogger(MicoRabbitProducer.class);
    private MicoRabbitEndpoint endpoint;
    private final String queueId;
    
    //key = syntacticType, value = list of mime types
    private Map<String,List<String>> modeInputs = new HashMap<String, List<String>>();
    
    //key = param name, value = value 
    private Map<String,String> parameters = new HashMap<String, String>();
	private PersistenceService persistenceService;

    public MicoRabbitProducer(MicoRabbitEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        this.queueId = endpoint.getQueueId();
        this.parameters = endpoint.getParametersAsMap();
        this.modeInputs = endpoint.getModeInputsAsMap();
        this.persistenceService = MicoCamelContext.getPersistenceService();
    }
    

 
    @Override
    public void process(Exchange exchange) throws Exception {
    	
    	//if the exchange should be simply forwarded, stop immediately
    	if (checkIfExchangeShouldBeForwarded(exchange) == true){
    		return;
    	}
    	
    	//if the exchange is not compatible with the declared inputs, stop immediately
    	if (checkIfExchangeIsCompatible(exchange) == false){
        	exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        	log.warn("Received exchange containing an invalid request {} for the extractor {}, stopping its routing",
        			  exchange.getIn().getBody(String.class),queueId);
    		return;
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
        synchronized (manager){
            while (!manager.hasFinished() && !manager.hasError()) {
                LOG.debug("..waiting for response from {}", queueId);
                manager.wait(3000);   // wait for extractor to finish processing
                
            }// while manager not finished
        }// end synch
        
        if(!manager.hasError()){

            //if no part was produced
            String[] newObjectUris = manager.getNewObjectUris();
            if(newObjectUris != null && newObjectUris.length > 0){
                // add new parts list as header field
                exchange.getIn().setHeader(KEY_MICO_PARTS_NEW, newObjectUris);
            }
            else
            {
                LOG.info("No new part produced by service {}, stopping the current message routing.",queueId);
                //stop the routing of the current exchange
                Message outMessage = exchange.getIn();
                outMessage.setBody(inMessage.getBody());
                exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
            }
        }
        else{
        	exchange.getIn().setHeader("error_class",exchange.getException());
        	exchange.getIn().setBody(exchange.getException());
        	throw exchange.getException();
        }

        channel.close();
        LOG.debug("extraction finished: {} - {}",queueId, headerItemURI);
    }

    private boolean checkIfExchangeShouldBeForwarded(Exchange exchange){
    	//check for exchanges created by our custom endpoint: 
    	if(exchange.getFromEndpoint() != null){
    		
    		Endpoint fromEndpoint =  exchange.getFromEndpoint();
    		
    		if(fromEndpoint instanceof MicoRabbitEndpoint){
    			MicoRabbitEndpoint from = (MicoRabbitEndpoint) fromEndpoint;
    			log.trace("Detected exchange generated by one MicoRabbitEndpoint");
    			
    			//if you created the exchange, unset the description
    			if(from.getExtractorId().contentEquals(endpoint.getExtractorId()) &&
    			   from.getExtractorVersion().contentEquals(endpoint.getExtractorVersion()) &&
    			   from.getModeId().contentEquals(endpoint.getModeId())){
    				log.trace("The exchange was generated by {}, unsetting the FromEndpoint description",endpoint.getEndpointUri());
    				exchange.setFromEndpoint(null);
    				exchange.getIn().setHeader("skip_aggregation", "false");
    			}
    			
    			log.trace("Exchange forwarded to the next component(s)");
    			//and forward to the extractors that are going to be connected later on
    			return true;
    		}
    	}
    	return false;
    }

    private boolean checkIfExchangeIsCompatible(Exchange exchange) {

    	
    	//the check is applied only if an input description was provided during the route definition 
    	if(modeInputs.size()>0){
	    	try {
	    		
	    		//0. first of all, retrieve the persistence service
	        	if((persistenceService=MicoCamelContext.getPersistenceService()) == null){
	        		throw new NullPointerException("Unable to retrieve the persistence service");
	        	}
	    		
	    		//1. parse the analysis request
				AnalysisRequest inEvent=AnalysisRequest.parseFrom(exchange.getIn().getBody(byte[].class));
				
				String inItemURI = inEvent.getItemUri();
				Item inItem = persistenceService.getItem(new URIImpl(inItemURI));
				inItem.getParts();
				
				Map<String,Resource> inputResources=new HashMap<String,Resource>();
				
				//2. create a Map from resourceURI to resource 
				List<String> partURIs = inEvent.getPartUriList();
				for(String rURI : partURIs){
					if(rURI.contentEquals(inItemURI)){
						inputResources.put(rURI, inItem);
					}
					else{
						inputResources.put(rURI,inItem.getPart(new URIImpl(rURI)));
					}
				}
				
				//3. assert that the size of the map and the amount of input resources are equals
				if(inputResources.size() != modeInputs.size()){
					log.warn("The expected amount of input part(s) should be {}, but is {}",modeInputs.size(),inputResources.size());
					return false;
				}
				
				//4. for every input SyntacticType, look if a corresponding input exists
				boolean exchangeIsCompatible = true;
				
				for( String syntacticType : modeInputs.keySet()){
					
					boolean inputIsPresent = false;
					List<String> mimeTypeList = modeInputs.get(syntacticType);
					syntacticType = URITools.demangleNamespaceIfKnown(syntacticType);
					
					log.debug("Looking for resource with syntacticType '{}' and format in '{}'",syntacticType,mimeTypeList.toString());
					
					for(Resource r : inputResources.values()){
						
						if(r.hasAsset()){
							log.debug("Evaluating resource with syntacticType '{}' and format '{}'",r.getSyntacticalType(),r.getAsset().getFormat());
						}
						else{
							log.debug("Evaluating resource with syntacticType '{}' and no asset",r.getSyntacticalType());
						}
						
						if(syntacticType.contentEquals(URITools.demangleNamespaceIfKnown(r.getSyntacticalType())) && !inputIsPresent){
							
							
							//if the syntactic type is correct, and we are checking an rdf type, you found the input
							if(contains(mimeTypeList,"application/x-mico-rdf") && mimeTypeList.size()==1){
								inputIsPresent = true;
							}
							
							//otherwise, double-check existence and format of the asset
							if(r.hasAsset()){
								inputIsPresent = inputIsPresent  || contains(mimeTypeList,r.getAsset().getFormat());
							}
							
							if(inputIsPresent){
								log.debug("Found input resource {} with type '{}' and format in '{}'",r.getURI(),syntacticType,mimeTypeList.toString());
							}
						}
						
					}
					if(!inputIsPresent){
						log.debug("Unable to find eligible resource");
					}
					exchangeIsCompatible = exchangeIsCompatible && inputIsPresent;
				}
				return exchangeIsCompatible;
				
			} catch (Exception e) {
				log.error("Exception caught while processing the input exchange: ",e);
				return true;
			}
    	}
    	
		return true;
	}
    
    private boolean contains(List<String> list, String value){
    	for(String v : list){
    		if(v == null && value == null){
    			return true;
    		}
    		if(v != null && value != null && v.contentEquals(value)){
    			return true;
    		}
    	}
    	return false;
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
    private class AnalyseManager extends DefaultConsumer implements ReturnListener {
        private String           queue;
        private AnalysisRequest    req;
        private volatile boolean finished = false;
        private volatile boolean hasError = false;
        /**
         *  list with parts generated by called extractor
         */
        private volatile ConcurrentLinkedQueue<String> newObjectUris = new ConcurrentLinkedQueue<String>();
        private Exchange exchange = null;

        public AnalyseManager(Exchange exchange, AnalysisRequest event, Channel channel) throws IOException {
            super(channel);
            this.req = event;

            // create a reply-to queue for this content item and attach a transition consumer to it
            queue = getChannel().queueDeclare().getQueue();
            log.debug("listen on queue {} for result. ",queue);
            getChannel().basicConsume(queue, false, this);
            getChannel().confirmSelect();
            getChannel().addReturnListener(this);

            this.exchange = exchange;
        }

        /**send analysis request to the extractor
         * @throws IOException
         */
        public void sendEvent() throws IOException {
            String correlationId = UUID.randomUUID().toString();

            AMQP.BasicProperties ciProps = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId).replyTo(queue).deliveryMode(2).build();

            getChannel().basicPublish("", req.getServiceId(), true, ciProps,
                    req.toByteArray());

        }

        /**
         * mark exchange as failed
         */
        @Override
        public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
            BasicProperties properties, byte[] body) throws IOException {
          log.warn("could not send message with replyCode [{}], replyText [{}], exchange [{}], routingKey [{}], properties  [{}]", replyCode, replyText, exchange, routingKey, properties);
          hasError=true;
          finished = true;
          MICOCamelAnalysisException analysisException = new MICOCamelAnalysisException(queueId,
                  "Unable to route message with key "+ routingKey,
                  "");
          this.exchange.setProperty(Exchange.DUPLICATE_MESSAGE, analysisException);
          this.exchange.setException(analysisException);
          this.exchange.getIn().setBody(analysisException);
          this.exchange.getOut().setBody(analysisException);
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
                    Progress progress = response.getProgress();
                    log.debug("received progress {} for part {}",progress.getProgress(), progress.getPartUri());
                    break;
                case ERROR:
                    log.warn("Received an error response {}, generating a new MICOCamelAnalysisException with what message : {}",response.getError().getMessage(), response.getError()
                            .getDescription());

                    hasError=true;
                    finished = true;
                    MICOCamelAnalysisException analysisException = new MICOCamelAnalysisException(queueId,
                        response.getError().getMessage(),
                        response.getError().getDescription());

                    exchange.setProperty(Exchange.DUPLICATE_MESSAGE, analysisException);
                    exchange.setException(analysisException);
                    exchange.getIn().setBody(analysisException);
                    exchange.getOut().setBody(analysisException);
                    
                    getChannel().basicAck(envelope.getDeliveryTag(), false);
                    // notify producer, to process message
                    synchronized (this) {
                        this.notify();
                    }
                    break;
                case NEW_PART:
                    String newObjectUri = response.getNew().getPartUri();
                    newObjectUris.add(newObjectUri);
                    log.debug(
                            "received processing result from service {} for content item {}: new object {}",
                            response.getNew().getServiceId(), response.getNew()
                                    .getItemUri(), newObjectUri);
                    getChannel().basicAck(envelope.getDeliveryTag(), false);
                    break;
                case FINISH:
                    // analyze process finished correctly, notify waiting
                    // threads to continue camel route
                    getChannel().basicAck(envelope.getDeliveryTag(), false);
                    finished = true;
                    // notify producer, to process message
                    synchronized (this) {
                        this.notify();
                    }
                }
        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
            log.trace("Cancel: {}", consumerTag);
            super.handleCancel(consumerTag);
        }

        @Override
        public void handleCancelOk(String consumerTag) {
            log.trace("CancelOk: {}", consumerTag);
            super.handleCancelOk(consumerTag);
        }

        @Override
        public void handleConsumeOk(String consumerTag) {
            log.trace("ConsumeOk: {}", consumerTag);
            super.handleConsumeOk(consumerTag);
        }

        @Override
        public void handleRecoverOk(String consumerTag) {
            log.trace("RecoverOk: {}", consumerTag);
            super.handleRecoverOk(consumerTag);
        }

        @Override
        public void handleShutdownSignal(String consumerTag,
                ShutdownSignalException sig) {
            log.trace("ShutdownSignal: {} for consumer {}", sig, consumerTag);
            super.handleShutdownSignal(consumerTag, sig);
        }

        public boolean hasFinished() {
            return finished;
        }
        
        public boolean hasError() {
            return hasError;
        }

        public String[] getNewObjectUris() {
            return newObjectUris.toArray(new String[0]);
        }

    }
}
