package eu.mico.platform.camel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import de.fraunhofer.idmt.camel.MicoCamel;
import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
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
        
        template.send("direct:a",createExchange());
        assertMockEndpointsSatisfied();
    }
    
    @Test(timeout=20000)
    public void testErrorSignaling() throws Exception {
    	
    	Exchange exc = createExchange();
    	
        try{
        	template.send("direct:error",exc);	
        }
        catch(MICOCamelAnalysisException e){
        	Assert.assertTrue(e.getErrorMessage().contentEquals("Mock error message"));
        	Assert.assertTrue(e.getErrorDescription().contentEquals("Mock error description"));
        }
        catch(Exception e){
        	Assert.fail("Unexpected exception");
        }
        
        Assert.assertTrue(exc.getProperty(Exchange.EXCEPTION_CAUGHT, Boolean.class));
        Assert.assertTrue(exc.getProperty(Exchange.EXCEPTION_HANDLED, String.class).contentEquals("Mock error message"));
        Assert.assertTrue(exc.getProperty(Exchange.DUPLICATE_MESSAGE, String.class).contentEquals("Mock error description"));
    }
    
    @Test(timeout=20000)
    public void testRouteStopsOnAnalysisException() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:error");
        mock.expectedMinimumMessageCount(0);
    	
    	try{
        	template.send("direct:error",createExchange());
        }
        catch(MICOCamelAnalysisException e){
        	log.info("Correctly caught MICOCamelAnalysisException : ",e);
        }
        catch(Exception e){
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

        template.send("direct:aggregateComplex-mimeType=mico/test,syntacticType=A",createExchange());
        template.send("direct:aggregateComplex-mimeType=mico/test,syntacticType=B",createExchange());
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
        MockEndpoint mock1 = getMockEndpoint("mock:result_aggregateSimple_1");
        mock1.expectedMessageCount(4);
        MockEndpoint mock2 = getMockEndpoint("mock:result_aggregateSimple_2");
        mock2.expectedMessageCount(4);

        template.send("direct:aggregateSimple-mimeType=mico/test,syntacticType=A",createExchange());
        template.send("direct:aggregateSimple-mimeType=mico/test,syntacticType=A",createExchange());
        assertMockEndpointsSatisfied();
    }

    
    @Test(timeout=10000)
    public void testParallelFlowsRoute() throws Exception {
        MockEndpoint mock1 = getMockEndpoint("mock:result_parallel_2");
        mock1.expectedMinimumMessageCount(1);
        MockEndpoint mock2 = getMockEndpoint("mock:result_parallel_2");
        mock2.expectedMinimumMessageCount(1);

        template.send("direct:parallelFlows-mimeType=mico/test,syntacticType=A",createExchange());
        template.send("direct:parallelFlows-mimeType=mico/test,syntacticType=C",createExchange());
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

        template.send("direct:simpleMulticast-mimeType=mico/test,syntacticType=C", createExchange());
        template.send("direct:simpleMulticast-mimeType=mico/test,syntacticType=C", createExchange());
        assertMockEndpointsSatisfied();

        template.send("direct:mimeType=mico/test2,syntacticType=C", createExchange());
        mock2.expectedMessageCount(3);
        mock3.expectedMessageCount(2);
        assertMockEndpointsSatisfied();
    }

    @Test(timeout = 20000)
    public void testSimpleXmlRoute() throws Exception {
            MockEndpoint mock2 = getMockEndpoint("mock:result_simple1");
            mock2.expectedMessageCount(2);
    
            template.send("direct:simple1-mimeType=mico/test,syntacticType=A", createExchange());
            template.send("direct:simple1-mimeType=mico/test,syntacticType=A", createExchange());
            assertMockEndpointsSatisfied();
    }

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

                if(registry.lookup("simpleAggregatorStrategy") == null)
                //and here, it is bound to the registry
                registry.bind("simpleAggregatorStrategy", aggregatorStrategy);
                
                if(registry.lookup("itemAggregatorStrategy") == null)
                //and here, it is bound to the registry
                registry.bind("itemAggregatorStrategy", itemAggregatorStrategy);
                        
                loadXmlSampleRoutes();
                
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

            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(CONTEXT_DELAYER);
                String[] testFiles = {
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
//        micoCamel.deleteContentItem(htmlItemUri);
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
