package eu.mico.platform.camel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryException;

import de.fraunhofer.idmt.camel.MicoCamel;
import eu.mico.platform.anno4j.model.impl.bodymmm.MultiMediaBody;
import eu.mico.platform.anno4j.model.impl.targetmmm.InitialTarget;
import eu.mico.platform.persistence.metadata.MICOProvenance;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_ITEM;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_PART;;

/**
 * @author sld
 *
 */
public class MicoRabbitComponentTest extends CamelTestSupport {

    private static final String SAMPLE_PNG = "sample.png";
    private static final String SAMPLE_MP4 = "sample-video.mp4";
    private static final String TEST_DATA_FOLDER = "src/test/resources/data/";
    private static MicoCamel micoCamel;
    private static String textItemUri,textPartUri;
    private static String imageItemUri,imagePartUri;
    private static String videoItemUri,videoPartUri;

    private static SimpleDateFormat isodate = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'", DateFormatSymbols.getInstance(Locale.US));
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

        template.send("direct:image",createExchange(imageItemUri, imagePartUri));
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

        template.send("direct:text",createExchange(textItemUri, textPartUri));
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

        template.send("direct:video_mp4",createExchange(videoItemUri,videoPartUri));
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
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

            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(400L);
                String[] testFiles = {
                        "src/test/resources/routes/sampleSplitRoute.xml",
                        "src/test/resources/routes/videoNerRoute_v1.xml",
                        "src/test/resources/routes/sampleRoute.xml" };
                try {
                    for (int i =0 ; i< testFiles.length; i++){
                        InputStream is = new FileInputStream(testFiles[i]);
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
        createImageItem();
        createVideoItem();
   }
    
    @AfterClass
    static public void cleanup() throws IOException{
        resetDataFolder();

        // remove test items from platform
        micoCamel.deleteContentItem(textItemUri);
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
    
    public static URI getServiceID() {
        return new URIImpl("http://example.org/services/CAMEL-TEST-injector");
    }

    /**
     * create and store/inject test data/item in mico persistence and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createTextItem() throws IOException, RepositoryException {
        ContentItem item = micoCamel.createItem();
        String content = "This is a sample text for testing ...";
        String type = "text/plain";
        Content part = micoCamel.addPart(content.getBytes(), type, item);
        part.setProperty(DCTERMS.SOURCE, "file://test-data.txt");              // set the analyzed content part as source for the new content part
        part.setProperty(DCTERMS.CREATED, isodate.format(new Date()));         // set the created date for the new content part
        part.setType(type);
        
        MultiMediaBody multiMediaBody = new MultiMediaBody();
        multiMediaBody.setFormat(type);
        InitialTarget target = new InitialTarget("test-data.txt");
        part.createAnnotation(multiMediaBody, null, getProvenance(), target);

        textItemUri = item.getURI().toString();
        textPartUri = part.getURI().toString();
        System.out.println("textPart: " + textPartUri);
    }

    /**
     * create and inject test item containing an image and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createImageItem() throws IOException, RepositoryException {
        ContentItem item = micoCamel.createItem();
        InputStream content = new FileInputStream(TEST_DATA_FOLDER+SAMPLE_PNG);
        String type = "image/png";
        Content part = micoCamel.addPart(IOUtils.toByteArray(content), type, item);
        part.setProperty(DCTERMS.SOURCE, "file://" + SAMPLE_PNG);              // set the analyzed content part as source for the new content part
        part.setProperty(DCTERMS.CREATED, isodate.format(new Date()));         // set the created date for the new content part
        part.setType(type);

        MultiMediaBody multiMediaBody = new MultiMediaBody();
        multiMediaBody.setFormat(type);
        InitialTarget target = new InitialTarget(SAMPLE_PNG);
        part.createAnnotation(multiMediaBody, null, getProvenance(), target);

        imageItemUri = item.getURI().toString();
        imagePartUri = part.getURI().toString();
        System.out.println("imageItem: " + imageItemUri);
        System.out.println("imagePart: " + imagePartUri);
    }
    

    /**
     * create and store/inject test data/item in mico persistence and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createVideoItem() throws IOException, RepositoryException {
        ContentItem item = micoCamel.createItem();
        InputStream content = new FileInputStream(TEST_DATA_FOLDER+SAMPLE_MP4);
        String type = "video/mp4";
        Content part = micoCamel.addPart(IOUtils.toByteArray(content), type, item);
        part.setProperty(DCTERMS.SOURCE, "file://test-video.mp4");              // set the analyzed content part as source for the new content part
        part.setProperty(DCTERMS.CREATED, isodate.format(new Date()));          // set the created date for the new content part
        part.setType(type);
        
        MultiMediaBody multiMediaBody = new MultiMediaBody();
        multiMediaBody.setFormat(type);
        InitialTarget target = new InitialTarget("test-video.mp4");
        part.createAnnotation(multiMediaBody, null, getProvenance(), target);

        videoItemUri = item.getURI().toString();
        videoPartUri = part.getURI().toString();
        System.out.println("videoPart: " + videoPartUri);
    }
    
    /**
     * create exchange containing item and part uri of sample text content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange() {
        return createExchange(textItemUri, textPartUri);
    }
    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange(String itemUri, String partUri) {
        Exchange exchange = context.getEndpoint("direct:a").createExchange();
        Message msg = exchange.getIn();
        msg.setHeader(KEY_MICO_ITEM, itemUri);
        msg.setHeader(KEY_MICO_PART, partUri);
        return exchange;
    }
    
    private static MICOProvenance getProvenance() {
        MICOProvenance micoProvenance = new MICOProvenance();
        micoProvenance.setExtractorName(getServiceID().toString());
        return micoProvenance;
    }
    
}
