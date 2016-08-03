package eu.mico.platform.camel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import com.google.common.collect.ImmutableSet;

import de.fraunhofer.idmt.camel.MicoCamel;
import de.fraunhofer.idmt.mico.DummyExtractor;
import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import eu.mico.platform.camel.split.SplitterNewParts;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;

/**
 * @author sld
 *
 */
public class MicoRabbitComponentTest extends TestBase {

    private static String textItemUri;
    private static String htmlItemUri;
    private static String imageItemUri;
    private static String videoItemUri;

    @Ignore
    @Test(timeout=20000)
    public void testMicoRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        template.send("direct:a",createExchange("direct:a"));
        assertMockEndpointsSatisfied();
    }
    


    @Test(timeout=20000)
    public void testErrorSignaling() throws Exception {
    	
    	Exchange exc = createExchange("direct:error");
    	
        try{
        	exc = template.send("direct:error",exc);	
        }
        catch(Exception e){
        	Assert.fail("Unexpected exception");
        }
        exc.setException(camelException);
        
        Assert.assertTrue("The exchange should have failed",exc.getException()!=null);
        Exception e = exc.getException();
        Assert.assertTrue("The exchange should have failed with a MICOCamelAnalysisException",
        				   e instanceof MICOCamelAnalysisException);
        Assert.assertTrue(((MICOCamelAnalysisException)e).getErrorMessage().contentEquals("Mock error message"));
    	Assert.assertTrue(((MICOCamelAnalysisException)e).getErrorDescription().contentEquals("Mock error description"));
    	camelException=null;
       
    }
    
    public static Exception camelException=null;
    
    @Test(timeout=20000)
    public void testErrorSignalingAfterAggregate() throws Exception {
    	
    	Exchange inExc = createExchange("direct:complex-error");
    	Exchange outExc = null;
    	
        try{
        	outExc = template.send("direct:complex-error",inExc);	
        }
        catch(Exception e){
        	Assert.fail("Unexpected exception");
        }
        
        outExc.setException(camelException);
        
        Assert.assertTrue("The exchange should have failed",outExc.getException()!=null);
        Exception e = outExc.getException();
        Assert.assertTrue("The exchange should have failed with a MICOCamelAnalysisException",
        				   e instanceof MICOCamelAnalysisException);
        Assert.assertTrue(((MICOCamelAnalysisException)e).getErrorMessage().contentEquals("Mock error message"));
    	Assert.assertTrue(((MICOCamelAnalysisException)e).getErrorDescription().contentEquals("Mock error description"));
    	camelException=null;       
    }
    
    @Test(timeout=20000)
    public void testRouteStopsOnAnalysisException() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:error");
        mock.expectedMessageCount(0);
    	
    	try{
        	template.send("direct:error",createExchange("direct:error"));
        }
        catch(MICOCamelAnalysisException e){
        	log.info("Correctly caught MICOCamelAnalysisException : ",e);
        }
        catch(Exception e){
        	Assert.fail("Unexpected exception");
        }
    	assertMockEndpointsSatisfied();
    }
    
    @Test(timeout=20000)
    public void testStopMessageFromNoNewPartExtractor() throws Exception {
            	
    	MockEndpoint mockStop = getMockEndpoint("mock:stop");
        mockStop.expectedMessageCount(0);
        
        MockEndpoint mockNoError = getMockEndpoint("mock:no-error");
        mockNoError.expectedMessageCount(0);
        
        MockEndpoint mockNoStop = getMockEndpoint("mock:no-stop");
        mockNoStop.expectedMessageCount(1);
    	
    	try{
        	template.send("direct:stop-propagation",createExchange("direct:stop-propagation"));
        }
        catch(Exception e){
        	e.printStackTrace();
        	Assert.fail("Unexpected exception");
        }
    	assertMockEndpointsSatisfied();
    }

    /**
     * @throws Exception
     */
    @Ignore // ignored, because a mico_wordcount and mico_ocr_service must be connected to run this test
    @Test(timeout=60000)
    public void testImageRoute() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result_image");
        mock.expectedMinimumMessageCount(1);

        template.send("direct:image",createExchange(imageItemUri));
        assertMockEndpointsSatisfied();
    }

    /**
     * @throws Exception
     */
    @Ignore("ignored, because a mico_wordcount must be connected to run this test")
    @Test(timeout=10000)
    public void testTextRoute() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result_text");
        mock.expectedMinimumMessageCount(1);

        template.send("direct:text",createExchange(textItemUri));
        assertMockEndpointsSatisfied();
    }

    /**
     * @throws Exception
     */
    @Ignore("ignored, because a mico_microformats must be connected to run this test")
    @Test(timeout=20000)
    public void testMicroformatsRoute() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result_text_html");
        mock.expectedMinimumMessageCount(1);

        template.send("direct:text_html",createExchange(htmlItemUri));
        assertMockEndpointsSatisfied();
    }

    /** test complex item aggregator via route defined in xml
     * @throws Exception
     */
    @Test(timeout=20000)
    public void testComplexAggregateRoute() throws Exception {
        MockEndpoint mock1 = getMockEndpoint("mock:result_aggregateComplex_1");

        template.send("direct:aggregateComplex-mimeType=mico/test,syntacticType=A",createExchange("direct:aggregateComplex-mimeType=mico/test,syntacticType=A"));
        template.send("direct:aggregateComplex-mimeType=mico/test,syntacticType=B",createExchange("direct:aggregateComplex-mimeType=mico/test,syntacticType=B"));
        int i = 0;
        for (Part p : textItem.getParts()){
            if ("http://example.org/services/AB-C-Service".equals(p.getSerializedBy().toString())){
                i++;
            }
        }
        assertEquals("Number of Parts generated by AB-C-Service",1, i);
        mock1.expectedMessageCount(1);
        assertMockEndpointsSatisfied();
    }

    @Test(timeout=20000)
    public void testSimpleAggregateRoute() throws Exception {
    	//TODO: check that the amount of C parts is correct 
    	
        MockEndpoint mock1 = getMockEndpoint("mock:result_aggregateSimple_1");
        mock1.expectedMessageCount(4);
        MockEndpoint mock2 = getMockEndpoint("mock:result_aggregateSimple_2");
        mock2.expectedMessageCount(4);

        micoCamel.deleteContentItem(textItemUri);
        createTextItem();

        template.send("direct:aggregateSimple-mimeType=mico/test,syntacticType=A",
       createExchange("direct:aggregateSimple-mimeType=mico/test,syntacticType=A"));
        template.send("direct:aggregateSimple-mimeType=mico/test,syntacticType=A",
       createExchange("direct:aggregateSimple-mimeType=mico/test,syntacticType=A"));

        int cParts = 0, b1Parts = 0, b2Parts = 0;
        for (Part p : textItem.getParts()){
            if ("http://example.org/services/B-C-Service".equals(p.getSerializedBy().toString())){
                cParts++;
            }else if ("http://example.org/services/A-B1-Service".equals(p.getSerializedBy().toString())){
                b1Parts++;
            } else if ("http://example.org/services/A-B2-Service".equals(p.getSerializedBy().toString())){
                b2Parts++;
            } else{
               fail("item contains an unknown part serialized by " + p.getSerializedBy().toString());
            }
        }
        assertEquals("Number of Parts generated by B-C-Service",4, cParts);
        assertEquals("Number of Parts generated by A-B1-Service",2, b1Parts);
        assertEquals("Number of Parts generated by A-B2-Service",2, b2Parts);
        assertMockEndpointsSatisfied();
    }

    /**
     * testing complex-route.xml containing splitter, aggregator and sample extractors consuming 2 parts at once
     * @throws Exception
     */
    @Test(timeout=20000)
    public void testComplexCombinedRoute() throws Exception {
        MockEndpoint mock1 = getMockEndpoint("mock:result_complex-route_1");
        mock1.expectedMessageCount(1);

        DummyExtractor extrBC_D = new DummyExtractor("BC","D","mico-extractor-test","1.0.0","BC-D-queue");
        DummyExtractor extrE_F = new DummyExtractor("E","F","mico-extractor-test","1.0.0","E-F-queue");
        micoCamel.registerService(extrBC_D);
        micoCamel.registerService(extrE_F);

        Item item = micoCamel.createItem();
        String content = "This is a sample text for testing ...";
        String type = "text/plain";
        micoCamel.addAsset(content.getBytes(), item, type);
        
        template.send("direct:complex-route,mimeType=video/mp4,syntacticType=mico:Video",createExchange(item.getURI().toString(),"direct:complex-route,mimeType=video/mp4,syntacticType=mico:Video"));
        try{
            assertMockEndpointsSatisfied();
            
            int partB=0, partC=0, partD=0, partE=0, partF=0;
            Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            for(Part p : parts){
                log.trace("checking part [{}]-[{}] serialized by {} at: {}",p.getSyntacticalType(),p.getSemanticType(),p.getSerializedBy().getName(),p.getSerializedAt());
                if(p.getSerializedBy().getName() == null){
                    log.error("creator of part {} [{}]-[{}] serialized at: {} should not be 'null'",p.getURI(),p.getSyntacticalType(),p.getSemanticType(),p.getSerializedAt());
                }
                assertTrue("All created parts should have assets",p.hasAsset());
                InputStream is = p.getAsset().getInputStream();
                String data = IOUtils.toString(is,"UTF-8");
                is.close();
                if(p.getSyntacticalType().equals("B")){
                    partB++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/B-C-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/BC-D-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/D-E-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/E-F-Service"));
                }
                else if(p.getSyntacticalType().equals("C")){
                    partC++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/B-C-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/BC-D-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/D-E-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/E-F-Service"));
                }
                else if(p.getSyntacticalType().equals("D")){
                    partD++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/B-C-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/BC-D-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/D-E-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/E-F-Service"));
                }
                else if(p.getSyntacticalType().equals("E")){
                    partE++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/B-C-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/BC-D-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/D-E-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/E-F-Service"));
                }
                else if(p.getSyntacticalType().equals("F")){
                    partF++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/B-C-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/BC-D-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/D-E-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/E-F-Service"));
                }
                else{
                    fail("Part "+p.getSyntacticalType()+" - "+p.getSemanticType()+" created by "+p.getSerializedBy().getName()+" should not exist");
                }
            }
            assertEquals("counted parts with syntactical type 'B'" ,1, partB);
            assertEquals("counted parts with syntactical type 'C'" ,1, partC);
            assertEquals("counted parts with syntactical type 'D'" ,1, partD);
            assertEquals("counted parts with syntactical type 'E'" ,1, partE);
            assertEquals("counted parts with syntactical type 'F'" ,1, partF);
        }
        finally{
            micoCamel.deleteContentItem(item.getURI().toString());
            micoCamel.unregisterService(extrBC_D);
            micoCamel.unregisterService(extrE_F);
        }
    }
    
    @Test(timeout=20000)
    public void testParallelFlowsRoute() throws Exception {
        MockEndpoint mock1 = getMockEndpoint("mock:result_parallel_1");
        mock1.expectedMinimumMessageCount(1);
        MockEndpoint mock2 = getMockEndpoint("mock:result_parallel_2");
        mock2.expectedMinimumMessageCount(1);

        template.send("direct:parallelFlows-mimeType=mico/test,syntacticType=A",createExchange("direct:parallelFlows-mimeType=mico/test,syntacticType=A"));
        template.send("direct:parallelFlows-mimeType=mico/test,syntacticType=C",createExchange("direct:parallelFlows-mimeType=mico/test,syntacticType=C"));
        assertMockEndpointsSatisfied();
    }
    
    @Test(timeout=30000)
    public void testMultipleOutputPartsRoute() throws Exception {
        
    	MockEndpoint mock1 = getMockEndpoint("mock:two-bb-parts");
        mock1.expectedMessageCount(2);	// NOTE: equal to the amount of DD parts created (MUST NOT be 2!)
        MockEndpoint mock2 = getMockEndpoint("mock:four-cc-parts");
        mock2.expectedMessageCount(4);  // NOTE: equal to the amount of DD parts created (MUST NOT be 4!)
        MockEndpoint mock3 = getMockEndpoint("mock:eight-dd-parts");
        mock3.expectedMessageCount(8); 

        String directUri = "direct:pipeline-with-multiple-outputs";
        template.send(directUri,createExchange(directUri));
        assertMockEndpointsSatisfied();
    }

    /** test route defined in xml
     * @throws Exception
     */
    @Test(timeout=200000)
    public void testSampleSplitRoute() throws Exception {
        MockEndpoint mock2 = getMockEndpoint("mock:result_multicast_1");
        mock2.expectedMessageCount(2);
        MockEndpoint mock3 = getMockEndpoint("mock:result_multicast_2");
        mock3.expectedMessageCount(2);

        template.send("direct:simpleMulticast-mimeType=mico/test,syntacticType=C", createExchange("direct:simpleMulticast-mimeType=mico/test,syntacticType=C"));
        template.send("direct:simpleMulticast-mimeType=mico/test,syntacticType=C", createExchange("direct:simpleMulticast-mimeType=mico/test,syntacticType=C"));
        assertMockEndpointsSatisfied();

        template.send("direct:mimeType=mico/test2,syntacticType=C", createExchange("direct:mimeType=mico/test2,syntacticType=C"));
        mock2.expectedMessageCount(3);
        mock3.expectedMessageCount(2);
        assertMockEndpointsSatisfied();
    }

    /**
     * test route defined in {@link simple_pipeline.xml}
     * 
     * @throws Exception
     */
    @Test(timeout = 20000)
    public void testSimpleXmlRoute() throws Exception {
            MockEndpoint mock2 = getMockEndpoint("mock:result_simple1");
            mock2.expectedMessageCount(2);
            Item item = micoCamel.createItem();
            String content = "This is a sample text for testing ...";
            String type = "text/plain";
            micoCamel.addAsset(content.getBytes(), item, type);

            template.send("direct:simple1-mimeType=mico/test,syntacticType=A", createExchange(item.getURI().toString(),"direct:simple1-mimeType=mico/test,syntacticType=A"));
            template.send("direct:simple1-mimeType=mico/test,syntacticType=A", createExchange(item.getURI().toString(),"direct:simple1-mimeType=mico/test,syntacticType=A"));
            assertMockEndpointsSatisfied();

            Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(4, parts.size());
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            
            int partB=0, partC=0;
            for(Part p : parts){
                log.trace("checking part [{}]-[{}] serialized by {} at: {}",p.getSyntacticalType(),p.getSemanticType(),p.getSerializedBy().getName(),p.getSerializedAt());
                if(p.getSerializedBy().getName() == null){
                    log.error("creator of part {} [{}]-[{}] serialized at: {} should not be 'null'",p.getURI(),p.getSyntacticalType(),p.getSemanticType(),p.getSerializedAt());
                }
                assertTrue("All created parts should have assets",p.hasAsset());
                InputStream is = p.getAsset().getInputStream();
                String data = IOUtils.toString(is,"UTF-8");
                is.close();
                if(p.getSyntacticalType().equals("B")){
                    partB++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertFalse(data.contains("Data added by: http://example.org/services/B-C-Service"));
                }
                else if(p.getSyntacticalType().equals("C")){
                    partC++;
                    assertTrue(data.contains("Data added by: http://example.org/services/A-B-Service"));
                    assertTrue(data.contains("Data added by: http://example.org/services/B-C-Service"));
                }
                else{
                    fail("Part "+p.getSyntacticalType()+" - "+p.getSemanticType()+" created by "+p.getSerializedBy().getName()+" should not exist");
                }
            }
            assertEquals("counted parts with syntactical type 'B'" ,2, partB);
            assertEquals("counted parts with syntactical type 'C'" ,2, partC);
            
            micoCamel.deleteContentItem(item.getURI().toString());
    }

    @Bean(ref="splitterNewPartsBean")
    public static SplitterNewParts splitterNewParts = new SplitterNewParts();

    @Bean(ref="simpleAggregatorStrategy")
    public static SimpleAggregationStrategy aggregatorStrategy = new SimpleAggregationStrategy();
    
    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();
    private static Item textItem;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                JndiRegistry registry = (JndiRegistry) (
                        (PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();

                if(registry.lookup("simpleAggregatorStrategy") == null){
	                //and here, it is bound to the registry
	                registry.bind("simpleAggregatorStrategy", aggregatorStrategy);
                }
                
                if(registry.lookup("itemAggregatorStrategy") == null){
	                //and here, it is bound to the registry
	                registry.bind("itemAggregatorStrategy", itemAggregatorStrategy);
                }
                
                if(registry.lookup("splitterNewPartsBean") == null){
                    registry.bind("splitterNewPartsBean", splitterNewParts);
                }
                
                loadXmlSampleRoutes();
                
                onException(MICOCamelAnalysisException.class)
                  .process(new Processor(){
                	    public void process(Exchange exchange) throws Exception {
                	    	log.error("Notifying exception",exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
                	    	camelException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);;
                	    	exchange.setException(null);
                	   }
                }).stop();
                
                from("direct:a").pipeline()
                        .to("mico-comp://foo1?host=localhost&extractorId=A-B-queue")
                        .to("mico-comp://foo2?host=localhost&extractorId=B-text/plain-queue")
                        .to("mock:result");

                from("direct:image")
                        .pipeline()
                        .to("mico-comp:vbox1?host=localhost&extractorId=ocr-queue-png")
                        .to("mico-comp:vbox2?host=localhost&extractorId=wordcount")
                        .to("mock:result_image");
            
                from("direct:text")
                .pipeline()
                .to("mico-comp:vbox2?host=localhost&extractorId=wordcount")
                .to("mock:result_text");

                from("direct:text_html")
                .pipeline()
                .to("mico-comp:vbox2?host=localhost&extractorId=microformats")
                .to("mock:result_text_html");
                
                from("direct:error")
                .pipeline()
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=ERROR-ERROR-queue")
                .to("mock:error");
                
                from("direct:complex-error")
                .multicast()
                .to("direct:pipeline-to-aggregate","direct:pipeline-to-aggregate");
                
                from("direct:pipeline-to-aggregate")
                .pipeline()
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=FINISH1-FINISH2-queue")
                .to("direct:pipeline-aggregate-with-errors");
                
                from("direct:pipeline-aggregate-with-errors")
                .pipeline()
                .aggregate(header("mico_item"), new ItemAggregationStrategy()).completionSize(2)
				  .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=ERROR-ERROR-queue");
                
                
                from("direct:stop-propagation")
                .multicast()
                  .to("direct:pipeline-to-stop", "direct:pipeline-to-finish");
                  
                from("direct:pipeline-to-stop")
                .pipeline()
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=STOP-STOP-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mock:no-error")
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=ERROR-ERROR-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mock:no-error");
                
                from("direct:pipeline-to-finish")
                .pipeline()
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=FINISH1-FINISH2-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=FINISH2-FINISH3-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=FINISH3-FINISH4-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mock:no-stop");
                
                from("direct:pipeline-with-multiple-outputs")
                .pipeline()
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=AA-BB-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mock:two-bb-parts")
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=BB-CC-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mock:four-cc-parts")
                .to("mico-comp:ebox1?host=localhost&extractorId=mico-extractor-test&extractorVersion=1.0.0&modeId=CC-DD-queue")
                .split().method("splitterNewPartsBean","splitMessage")
                .to("mock:eight-dd-parts");
                

            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(CONTEXT_DELAYER);
                String[] testFiles = {
                        "src/test/resources/routes/complex-route.xml",
                        "src/test/resources/routes/containing_complex_aggregator.xml",
                        "src/test/resources/routes/containing_simple_aggregator.xml",
                        "src/test/resources/routes/parallel_different_input.xml", 
                        "src/test/resources/routes/sampleSplitRoute.xml",
                        "src/test/resources/routes/simple_pipeline.xml" 
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
        resetDataFolder();

        if(micoCamel == null){
	        try{
		        micoCamel = new MicoCamel();
		        micoCamel.init();
		        createTextItem();
		        createHtmlItem();
		        createImageItem();
		        createVideoItem();
	        }catch (Exception e){
	            e.printStackTrace();
	            fail("unable to setup test env");
	        }
        }
   }
    
    @AfterClass
    static public void cleanup() throws IOException{

        // remove test items from platform
        micoCamel.deleteContentItem(textItemUri);
        micoCamel.deleteContentItem(htmlItemUri);
        micoCamel.deleteContentItem(imageItemUri);
        micoCamel.deleteContentItem(videoItemUri);

        micoCamel.shutdown();
        micoCamel=null;

    }

    /**
     * camel stores all processed files in .camel folder, 
     * move them back and delete the folder
     */
    private static void resetDataFolder() {
        Path destPath = new File(TEST_DATA_FOLDER).toPath();
        File processed = new File(TEST_DATA_FOLDER,".camel");
        if(processed.exists()){
            for (File f : processed.listFiles()){
                Path source = f.toPath();
                try {
                    System.out.println("move file back to scr folder: " + source.getFileName());
                    Files.move(source, destPath.resolve(source.getFileName()));
                } catch (FileAlreadyExistsException e){
                    // file was not processed correctly and is still in source folder, 
                    // remove the copy from processed folder
                    f.delete();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
         processed.delete();
        }
    }
    
    /**
     * create exchange containing item and part uri of sample text content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange(String directUri) {
        return createExchange(textItemUri,directUri);
    }

    /**
     * create and store/inject test data/item in mico persistence and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createTextItem() throws IOException, RepositoryException {
        textItem = micoCamel.createItem();
        String content = "This is a sample text for testing ...";
        String type = "text/plain";
        micoCamel.addAsset(content.getBytes(), textItem, type);

        textItemUri = textItem.getURI().toString();
        System.out.println("textItem: " + textItemUri);
    }

    /**
     * create and inject test item containing an image and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createHtmlItem() throws IOException, RepositoryException {
        Item item = micoCamel.createItem();
        InputStream content = new FileInputStream(TEST_DATA_FOLDER+SAMPLE_HTML);
        String type = "text/html";
        micoCamel.addAsset(IOUtils.toByteArray(content), item, type);

        htmlItemUri = item.getURI().toString();
        System.out.println("htmlItem: " + htmlItemUri);
    }
    
    /**
     * create and inject test item containing an image and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createImageItem() throws IOException, RepositoryException {
        Item item = micoCamel.createItem();
        InputStream content = new FileInputStream(TEST_DATA_FOLDER+SAMPLE_PNG);
        String type = "image/png";
        micoCamel.addAsset(IOUtils.toByteArray(content), item, type);

        imageItemUri = item.getURI().toString();
        System.out.println("imageItem: " + imageItemUri);
    }
    

    /**
     * create and store/inject test data/item in mico persistence and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createVideoItem() throws IOException, RepositoryException {
        Item item = micoCamel.createItem();
        InputStream content = new FileInputStream(TEST_DATA_FOLDER+SAMPLE_MP4);
        String type = "video/mp4";
        micoCamel.addAsset(IOUtils.toByteArray(content), item, type);

        videoItemUri = item.getURI().toString();
        System.out.println("videoItem: " + videoItemUri);
    }
    
    
}
