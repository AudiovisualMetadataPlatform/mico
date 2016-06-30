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
package eu.mico.platform.camel;

import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_ITEM;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_STARTING_DIRECT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import eu.mico.platform.event.model.Event.AnalysisRequest;
import eu.mico.platform.persistence.api.PersistenceService;

public class MicoCamelContext {

    private static Logger log = LoggerFactory.getLogger(MicoCamelContext.class);
    
    CamelContext context;
    @Bean(ref="simpleAggregatorStrategy")
    public static SimpleAggregationStrategy aggregatorStrategy = new SimpleAggregationStrategy();
    
    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();
    
    private ProducerTemplate template;
    private static PersistenceService ps = null;
    
    private static ConcurrentHashMap<String, Exception> injExceptions = new ConcurrentHashMap<String, Exception>();

    public void init(PersistenceService ps){
    	if(ps == null){
    		throw new IllegalArgumentException("The input persistence service cannot be null");
    	}
    	MicoCamelContext.ps=ps;
        setupCamelContext();        
    }
    
    public static PersistenceService getPersistenceService(){
    	return ps;
    }

    private void setupCamelContext() {
        if(context != null){
            return;
        }

        log.info("adding camel stuff ...");
        try {
            context = new DefaultCamelContext();
            template = context.createProducerTemplate();

            context.setAutoStartup(true);
            context.start();
            
            
            JndiRegistry registry = (JndiRegistry) (
                    (PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();

            if(registry.lookup("simpleAggregatorStrategy") == null)
            //and here, it is bound to the registry
            registry.bind("simpleAggregatorStrategy", aggregatorStrategy);
            
            if(registry.lookup("itemAggregatorStrategy") == null)
            //and here, it is bound to the registry
            registry.bind("itemAggregatorStrategy", itemAggregatorStrategy);
            
        }catch(javax.naming.NameAlreadyBoundException e){
            log.info(e.getMessage());
        } catch (Exception e) {
            log.warn("Error setting up camel context", e);
        }
    }

    public void addRouteToContext(String xmlRoute) {
        try {
            RoutesDefinition routeDefs = getRoutesDefinition(xmlRoute);
            List<RouteDefinition> defs = routeDefs.getRoutes();
            for(RouteDefinition d : defs){
            	d.onException(MICOCamelAnalysisException.class).handled(true)
          	     .process(exceptionProcessor).handled(true).stop();
            }
            context.addRouteDefinitions(defs);
        } catch (Exception e) {
            log.warn("Error adding camel route to context",e);
        }
    }

    public void removeRouteFromContext(String xmlRoute) {
        try {
            RoutesDefinition routeDefs = getRoutesDefinition(xmlRoute);
            context.removeRouteDefinitions(routeDefs.getRoutes());
        } catch (Exception e) {
            log.warn("Error removing camel route from context",e);
        }
    }

    public void loadRoutes(InputStream is) throws Exception {
    	RoutesDefinition routeDefs = context.loadRoutesDefinition(is);
        List<RouteDefinition> defs = routeDefs.getRoutes();
        for(RouteDefinition d : defs){
        	d.onException(MICOCamelAnalysisException.class).handled(true)
      	     .process(exceptionProcessor).handled(true).stop();
        }

        context.addRouteDefinitions(defs);
        context.startAllRoutes();
        List<Route> routes = context.getRoutes();
        log.info("available camel Routes: {}",routes.size());
        for (Route r : routes){
            log.info(" - id: {} descr: {}", r.getId(), r.getDescription());
        }
    }
    
    private String getRouteExceptionId(Exchange exchange){
    	return getRouteExceptionId((String) exchange.getIn().getHeader(KEY_MICO_ITEM), (String) exchange.getIn().getHeader(KEY_STARTING_DIRECT));
    }
    private String getRouteExceptionId(String itemUri, String directUri){
    	return itemUri + "-" + directUri;
    }
    
    private Processor exceptionProcessor = new Processor(){
		public void process(Exchange exchange) throws Exception {
			Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
			log.error("Notifying exception",e);    			
			String exc_id = getRouteExceptionId(exchange);
			log.error("Storing with id {}",exc_id);   
			injExceptions.put(exc_id, e);
			log.error("cleaning the current exchange");
			exchange.setException(null);
		}
	};

    public void deleteRoutes(InputStream is) throws Exception {
        RoutesDefinition routeDefs = context.loadRoutesDefinition(is);
        context.removeRouteDefinitions(routeDefs.getRoutes());
        List<Route> routes = context.getRoutes();
        log.info("available camel Routes: {}",routes.size());
        for (Route r : routes){
            log.info(" - id: {} descr: {}", r.getId(), r.getDescription());
        }
    }
    private RoutesDefinition getRoutesDefinition(String xmlRoute) throws Exception {
    	ByteArrayInputStream stream = new ByteArrayInputStream(
    			xmlRoute.getBytes(StandardCharsets.UTF_8));
    	RoutesDefinition routeDefs = context
    			.loadRoutesDefinition(stream);    	
    	return routeDefs;
    }
        
    /**
     * process item with workflow
     * @param directUri
     * @param itemUri
     */
    public void processItem(String directUri, String itemUri) {
        Exchange inExc = createExchange(itemUri,itemUri,directUri);
        Exchange outExc = template.send(directUri,inExc);
        checkProcessingOutcome(itemUri, directUri);
     }
    
    /**
     * process single item part with workflow
     * @param directUri
     * @param itemUri
     */
    public void processPart(String directUri, String itemUri, String partUri) {
        Exchange inExc = createExchange(itemUri,partUri,directUri);
        Exchange outExc = template.send(directUri,inExc);
        checkProcessingOutcome(itemUri, directUri);
    }
    
    private void checkProcessingOutcome(String itemUri, String directUri) throws MICOCamelAnalysisException{

    	String excId= getRouteExceptionId(itemUri,directUri);
    	Exception e = injExceptions.get(excId);
    	if(e!=null){
    		if(e instanceof MICOCamelAnalysisException){
    			log.error("The current injection of the item {} failed on the extractor {}",
    					itemUri, ((MICOCamelAnalysisException) e).getFailingExtractor());
    			injExceptions.remove(excId);
    			throw (MICOCamelAnalysisException) e;
    		}
    		log.error("The current injection failed for an unexpected error");
    		MICOCamelAnalysisException eOut = new MICOCamelAnalysisException("Unknown source",e.getClass().getSimpleName(),e.getMessage());
    		eOut.setStackTrace(e.getStackTrace());
    		injExceptions.remove(excId);
    		throw eOut;
    	}

    }
    

    /**
     * create exchange containing item and part uri of sample/test content
     * 
     * @param itemUri
     * @return an exchange containing item in header
     */
    private Exchange createExchange(String itemUri, String partUri, String directUri) {
        Endpoint endpoint;
        if ((endpoint = context.hasEndpoint(directUri)) == null){
            throw new IllegalArgumentException("Endpoint with URI [" + directUri +"] is not registered in context");
        }

        Exchange exchange = endpoint.createExchange();
        Message msg = exchange.getIn();
        msg.setHeader(KEY_MICO_ITEM, itemUri);
        msg.setHeader(KEY_STARTING_DIRECT, directUri);

        AnalysisRequest event = AnalysisRequest.newBuilder()
                .setItemUri(itemUri)
                .setServiceId("http://mico-project.eu/services/workflow-injector")
                .build();
        
        if(partUri != null){
        	event = AnalysisRequest.newBuilder()
                    .setItemUri(itemUri)
                    .addPartUri(partUri)
                    .setServiceId("http://mico-project.eu/services/workflow-injector")
                    .build();
        }
        
        msg.setBody(event.toByteArray());
        return exchange;
    }

    public Endpoint hasEndpoint(String uri) {
        return context.hasEndpoint(uri);
    }

}

