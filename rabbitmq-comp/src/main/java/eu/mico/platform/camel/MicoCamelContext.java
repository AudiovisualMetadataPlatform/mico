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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import eu.mico.platform.event.model.Event.AnalysisRequest;

public class MicoCamelContext {

    private static Logger log = LoggerFactory.getLogger(MicoCamelContext.class);
    
    CamelContext context;
    @Bean(ref="simpleAggregatorStrategy")
    public static SimpleAggregationStrategy aggregatorStrategy = new SimpleAggregationStrategy();
    
    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();
    
    private ProducerTemplate template;

    public void init(){
        setupCamelContext();
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
            
        } catch (IllegalArgumentException | NullPointerException
                | ClassCastException | UnsupportedOperationException e) {
            log.warn("Unable to check camel routes to context", e);
        } catch (Exception e) {
            log.warn("Unable to add camel routes", e);
        }
    }

    public void addRouteToContext(String xmlRoute) {
        try {
            RoutesDefinition routeDefs = getRoutesDefinition(xmlRoute);
            context.addRouteDefinitions(routeDefs.getRoutes());
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
        context.addRouteDefinitions(routeDefs.getRoutes());
        context.startAllRoutes();
        context.setupRoutes(true);
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
        Exchange exc = createExchange(itemUri,directUri);

        template.send(directUri,exc);
        
    }

    /**
     * create exchange containing item and part uri of sample/test content
     * 
     * @param itemUri
     * @return an exchange containing item in header
     */
    private Exchange createExchange(String itemUri,String directUri) {
        Endpoint endpoint;
        if ((endpoint = context.hasEndpoint(directUri)) == null){
            throw new IllegalArgumentException("Endpoint with URI [" + directUri +"] is not registered in context");
        }

        Exchange exchange = endpoint.createExchange();
        Message msg = exchange.getIn();
        msg.setHeader(KEY_MICO_ITEM, itemUri);
        AnalysisRequest event = AnalysisRequest.newBuilder()
                .setItemUri(itemUri)
                .setServiceId("http://mico-project.eu/services/workflow-injector")
                .build();
        msg.setBody(event.toByteArray());
        return exchange;
    }

    public Endpoint hasEndpoint(String uri) {
        return context.hasEndpoint(uri);
    }

}
