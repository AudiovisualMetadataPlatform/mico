package eu.mico.platform.camel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import de.fraunhofer.idmt.camel.MicoCamel;
import de.fraunhofer.idmt.mico.DummyExtractor;
import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import eu.mico.platform.persistence.model.Item;

/**
 * @author sld
 *
 */
public class CallParameterTest extends TestBase {

    private static String textItemUri;


    @Bean(ref="simpleAggregatorStrategy")
    public static SimpleAggregationStrategy aggregatorStrategy = new SimpleAggregationStrategy();
    
    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();
    private static Item textItem;


    /** test complex item aggregator via route defined in xml
     * @throws Exception
     */
    @Test(timeout=20000)
    public void testSingleExtractorParams() throws Exception {
        DummyExtractor extr1 = new DummyExtractor("video/mp4","mico:Video","parameter-selection-test","1.0.0","ParamTest");
        micoCamel.registerService(extr1);
        MockEndpoint mock1 = getMockEndpoint("mock:result_simpleParams");

        template.send("direct:workflow-simpleParams,mimeType=video/mp4,syntacticType=mico:Video",createExchange());

        mock1.expectedMessageCount(1);
        assertMockEndpointsSatisfied();
        HashMap<String, String> expectedMap = new HashMap<String,String>();
        expectedMap.put("value-param-0","8000");
        expectedMap.put("value-param-1","8000");
        expectedMap.put("value-param-2","_8kHz");
        expectedMap.put("value-param-3","enabled");
        expectedMap.put("value-param-4","1");
        expectedMap.put("value-param-5","3,7,56");
        assertEquals(expectedMap,extr1.getParameters());
        micoCamel.unregisterService(extr1);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                JndiRegistry registry = (JndiRegistry) (
                        (PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();

                //and here, it is bound to the registry
                registry.bind("simpleAggregatorStrategy", aggregatorStrategy);
                //and here, it is bound to the registry
                registry.bind("itemAggregatorStrategy", itemAggregatorStrategy);
                        
                loadXmlSampleRoutes();
  
            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(CONTEXT_DELAYER);
                String[] testFiles = {
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

    @BeforeClass
    static public void init() throws Exception {

        try{
            micoCamel = new MicoCamel();
            micoCamel.init();
            createTextItem();
        }catch (Exception e){
            e.printStackTrace();
            fail("unable to setup test env");
        }
   }
    
    @AfterClass
    static public void cleanup() throws IOException{
        // remove test items from platform
        micoCamel.deleteContentItem(textItemUri);

        micoCamel.shutdown();
    }

    
    /**
     * create exchange containing item and part uri of sample text content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange() {
        return createExchange(textItemUri);
    }

    /**
     * create and store/inject test data/item in mico persistence and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createTextItem() throws IOException, RepositoryException {
        textItem = micoCamel.createItem();
        String content = "This is a sample text for param testing ...";
        String type = "text/plain";
        micoCamel.addAsset(content.getBytes(), textItem, type);

        textItemUri = textItem.getURI().toString();
        System.out.println("textItem: " + textItemUri);
    }

}