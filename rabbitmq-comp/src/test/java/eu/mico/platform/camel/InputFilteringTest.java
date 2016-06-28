package eu.mico.platform.camel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.junit.Test;

import de.fraunhofer.idmt.mico.DummyExtractor;
import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;

;

/**
 * @author cvo
 *
 */
public class InputFilteringTest extends TestBase {

    
    @Test
    public void testReadUndefinedInputsReturnNull() throws Exception {
    	
    	MicoRabbitEndpoint ep= context.getEndpoint("mico-comp://vbox1?host=localhost&amp;serviceId=ParamTest&amp;extractorId=parameter-selection-test&amp;extractorVersion=1.0.0&amp;modeId=ParamTest&amp;parameters={&quot;value-param-0&quot;:&quot;8000&quot;,&quot;value-param-1&quot;:&quot;8000&quot;,&quot;value-param-2&quot;:&quot;_8kHz&quot;,&quot;value-param-3&quot;:&quot;enabled&quot;,&quot;value-param-4&quot;:&quot;1&quot;,&quot;value-param-5&quot;:&quot;3,7,56&quot;}", MicoRabbitEndpoint.class);
    	assertNull(ep.getModeInputs());
    	assertNull(ep.getModeInputsAsMap());
    }
    
    @Test
    public void testReadDefinedInputsReturnNotNull() throws Exception {
    	
    	MicoRabbitEndpoint ep= context.getEndpoint("mico-comp://vbox1?extractorId=mico-extractor-test&extractorVersion=1.0.0&host=localhost&inputs=%7B%22A%22%3A%5B%22mico%2Ftest-mime-A%22%5D%2C%22B%22%3A%5B%22mico%2Ftest-mime-B%22%5D%7D&modeId=AB-C-queue&serviceId=AB-C-queue", MicoRabbitEndpoint.class);
    	assertNotNull(ep.getModeInputs());
    	assertNotNull(ep.getModeInputsAsMap());
    }
    
    @Test
    public void testGetModeInputsReturnsCorrectMap() throws Exception {
    	
    	MicoRabbitEndpoint ep= context.getEndpoint("mico-comp://vbox1?extractorId=mico-extractor-test&extractorVersion=1.0.0&host=localhost&inputs=%7B%22A%22%3A%5B%22mico%2Ftest-mime-A-1%22%2C%22mico%2Ftest-mime-A-2%22%2C%22mico%2Ftest-mime-A-3%22%5D%2C%22B%22%3A%5B%22mico%2Ftest-mime-B%22%5D%7D&modeId=AB-C-queue&serviceId=AB-C-queuequeue", MicoRabbitEndpoint.class);
    	assertNotNull(ep.getModeInputs());
    	assertNotNull(ep.getModeInputsAsMap());
    	Map<String, List<String>> expectedMap = new HashMap<String,List<String>>();
         expectedMap.put("A",new ArrayList<String>());
         expectedMap.get("A").add("mico/test-mime-A-1");
         expectedMap.get("A").add("mico/test-mime-A-2");
         expectedMap.get("A").add("mico/test-mime-A-3");
         expectedMap.put("B",new ArrayList<String>());
         expectedMap.get("B").add("mico/test-mime-B");
         assertEquals(expectedMap,ep.getModeInputsAsMap());
    	
    }


    @Bean(ref = "simpleAggregatorStrategy")
    public static SimpleAggregationStrategy simpleAggregatorStrategy = new SimpleAggregationStrategy();

    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
        	public void configure() {
                JndiRegistry registry = (JndiRegistry) (
                        (PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();

                
                if(registry.lookup("simpleAggregatorStrategy") == null)
                //and here, it is bound to the registry
                registry.bind("simpleAggregatorStrategy", simpleAggregatorStrategy);
                
                if(registry.lookup("itemAggregatorStrategy") == null)
                //and here, it is bound to the registry
                registry.bind("itemAggregatorStrategy", itemAggregatorStrategy);
                        
                loadXmlSampleRoutes();
  
            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(CONTEXT_DELAYER);
                String[] testFiles = {
                        "src/test/resources/routes/single_extractor_with_input_definitions.xml",
                        "src/test/resources/routes/single_extractor_with_parameters.xml"
                        };
                try {
                    for (int i =0 ; i< testFiles.length; i++){
                        InputStream is = new FileInputStream(testFiles[i]);
                        log.debug("add Route: {}",testFiles[i]);
                        RoutesDefinition routes = context.loadRoutesDefinition(is);
                        context.addRouteDefinitions(routes.getRoutes());
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        };
    }

}
