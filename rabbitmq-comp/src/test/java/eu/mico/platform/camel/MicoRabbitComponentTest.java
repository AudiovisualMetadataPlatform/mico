package eu.mico.platform.camel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TimeZone;

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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import de.fraunhofer.idmt.camel.MicoCamel;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import eu.mico.platform.persistence.model.Item;;

/**
 * @author sld
 *
 */
public class MicoRabbitComponentTest extends TestBase {

    private static String textItemUri;
    private static String htmlItemUri;
    private static String imageItemUri;
    private static String videoItemUri;

    static {
        isodate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    


    @Ignore
    @Test(timeout=20000)
    public void testMicoRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        template.send("direct:a",createExchange());
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

    /** test route defined in xml
     * @throws Exception
     */
    @Test(timeout=5000)
    public void testSampleXmlRoute() throws Exception {
        MockEndpoint mock2 = getMockEndpoint("mock:bar");
        mock2.expectedMinimumMessageCount(1);

        template.send("direct:foo",createExchange());
        assertMockEndpointsSatisfied();
    }

    /** test route defined in xml
     * @throws Exception
     */
    @Test(timeout=20000)
    public void testSampleSplitRoute() throws Exception {
        MockEndpoint mock2 = getMockEndpoint("mock:result_split2");
        MockEndpoint mock3 = getMockEndpoint("mock:result_split3");
        mock2.expectedMinimumMessageCount(1);
        mock3.expectedMinimumMessageCount(1);

        template.send("direct:test_split",createExchange());
        assertMockEndpointsSatisfied();
    }

    @Test(timeout = 20000)
    public void testAutomaticXmlRoute() throws Exception {
        MockEndpoint mock2 = getMockEndpoint("mock:workflow-WORKFLOW_ID-pipeline-2");
        mock2.expectedMinimumMessageCount(1);
        mock2.expectedMessageCount(2);
        MockEndpoint mock3 = getMockEndpoint("mock:workflow-WORKFLOW_ID-pipeline-3");
        mock3.expectedMessageCount(2);

        template.send("direct:mico/test", createExchange());
        template.send("direct:mico/test", createExchange());
        assertMockEndpointsSatisfied();
    }

    /** test ner sample route defined in xml
     * @throws Exception
     */
    @Ignore("all extractors for video-kaldi-ner pipeline must be connected")
    @Test(timeout=400000l)
    public void testSampleNerRoute() throws Exception {
        MockEndpoint mock2 = getMockEndpoint("mock:result_kaldi_rdf");
        MockEndpoint mock3 = getMockEndpoint("mock:result_kaldi_ner");
        mock2.expectedMinimumMessageCount(1);
        mock3.expectedMinimumMessageCount(1);

        template.send("direct:video_mp4",createExchange(videoItemUri));
        assertMockEndpointsSatisfied();
    }

    @Bean(ref="aggregatorStrategy")
    public static SimpleAggregationStrategy aggregatorStrategy = new SimpleAggregationStrategy();
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                JndiRegistry registry = (JndiRegistry) (
                        (PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();

                        //and here, it is bound to the registry
                        registry.bind("aggregatorStrategy", aggregatorStrategy);
                        
                loadXmlSampleRoutes();
                
                from("direct:a").pipeline()
                        .to("mico-comp://foo1?serviceId=A-B-queue")
                        .to("mico-comp://foo2?serviceId=B-text/plain-queue")
                        .to("mock:result");

                from("direct:image")
                        .pipeline()
                        .to("mico-comp:vbox1?host=mico-platform&user=mico&password=mico&serviceId=ocr-queue-png")
                        .to("mico-comp:vbox2?host=mico-platform&user=mico&password=mico&serviceId=wordcount")
                        .to("mock:result_image");
            
                from("direct:text")
                .pipeline()
                .to("mico-comp:vbox2?host=mico-platform&user=mico&password=mico&serviceId=wordcount")
                .to("mock:result_text");

                from("direct:text_html")
                .pipeline()
                .to("mico-comp:vbox2?host=mico-box&user=mico&password=mico&serviceId=microformats")
                .to("mock:result_text_html");

            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(40L);
                String[] testFiles = {
                        "src/test/resources/routes/sampleSplitRoute.xml",
                        "src/test/resources/routes/videoNerRoute_v1.xml",
                        "src/test/resources/routes/automaticCreation_sampleSplitRoute.xml",
                        "src/test/resources/routes/sampleRoute.xml" 
                        };
                try {
                    for (int i =0 ; i< testFiles.length; i++){
                        InputStream is = new FileInputStream(testFiles[i]);
			System.out.println(testFiles[i]);
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

        micoCamel = new MicoCamel();
        micoCamel.init();
        createTextItem();
        createHtmlItem();
        createImageItem();
        createVideoItem();
   }
    
    @AfterClass
    static public void cleanup() throws IOException{
        resetDataFolder();

        // remove test items from platform
        micoCamel.deleteContentItem(textItemUri);
//        micoCamel.deleteContentItem(htmlItemUri);
        micoCamel.deleteContentItem(imageItemUri);
        micoCamel.deleteContentItem(videoItemUri);

        micoCamel.shutdown();
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
        Item item = micoCamel.createItem();
        String content = "This is a sample text for testing ...";
        String type = "text/plain";
        micoCamel.addAsset(content.getBytes(), item, type);

        textItemUri = item.getURI().toString();
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
