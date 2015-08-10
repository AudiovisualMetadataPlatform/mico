package eu.mico.platform.camel;

import java.io.File;
import java.io.IOException;
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
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryException;

import de.fraunhofer.idmt.camel.MicoCamel;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_ITEM;
import static eu.mico.platform.camel.MicoRabbitProducer.KEY_MICO_PART;;

/**
 * @author sld
 *
 */
public class MicoRabbitComponentTest extends CamelTestSupport {

    private static final String TEST_DATA = "src/test/resources/data";
    private static MicoCamel micoCamel;
    private static String itemUri,partUri;

    private static SimpleDateFormat isodate = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'", DateFormatSymbols.getInstance(Locale.US));
    static {
        isodate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    

    
    @Test
    public void testMicoRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        template.send("direct:a",createExchange());
        assertMockEndpointsSatisfied();
    }

    @Test
    @Ignore
    public void testMicoCamel() throws Exception {
		micoCamel.testSimpleAnalyse();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
            	getContext().setDelayer(300L);                
                
//                from("mico-comp://foo").pipeline()
////                  .inOut("mico-comp://bar")
//                  .to("mico-comp://baz")
//                  .to("mock:result");
                

                from("direct:a")
                        .pipeline()
                        .to("mico-comp://foo?serviceId=A-B-queue")
                        .to("mico-comp://foo?serviceId=B-text/plain-queue")
                        .to("mico-comp:vbox?host=mico-platform&user=mico&password=mico&serviceId=wordcount")
                        .to("mock:result");
            }
        };
    }

    @BeforeClass
    static public void init() throws Exception {
        resetDataFolder();

        micoCamel = new MicoCamel();
        micoCamel.init();
        createTestItem();
   }
    
    @AfterClass
    static public void cleanup() throws IOException{
        resetDataFolder();
        
        micoCamel.shutdown();
    }

    /**
     * camel stores all processed files in .camel folder, 
     * move them back and delete the folder
     */
    private static void resetDataFolder() {
        Path destPath = new File(TEST_DATA).toPath();
        File processed = new File(TEST_DATA,".camel");
        if(processed.exists()){
            for (File f : processed.listFiles()){
                Path source = f.toPath();
                try {
                    System.out.println("move file back to scr folder: " + source.getFileName());
                    Files.move(source, destPath.resolve(source.getFileName()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
         processed.delete();
        }
    }
    
    public static URI getServiceID() {
        return new URIImpl("http://example.org/services/CAMEL-TEST-Service");
    }

    /**
     * create and store test data/item in mico persistence and 
     * store item and part uri in class
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createTestItem() throws IOException, RepositoryException {
        ContentItem item = micoCamel.createItem();
        String content = "This is a sample text for testing ...";
        String type = "text/plain";
        Content part = micoCamel.addPart(content.getBytes(), type, item);
        part.setRelation(DCTERMS.CREATOR, getServiceID());  // set the service ID as provenance information for the new content part
        part.setRelation(DCTERMS.SOURCE, new URIImpl("file://test-data.txt"));              // set the analyzed content part as source for the new content part
        part.setProperty(DCTERMS.CREATED, isodate.format(new Date())); // set the created date for the new content part
        
        itemUri = item.getURI().toString();
        partUri = part.getURI().toString();
    }

    /**
     * create exchange containing item and part uri of sample/test content
     * @return an exchange containing item and part uri in headers
     */
    private Exchange createExchange() {
        Exchange exchange = context.getEndpoint("direct:a").createExchange();
        Message msg = exchange.getIn();
        msg.setHeader(KEY_MICO_ITEM, itemUri);
        msg.setHeader(KEY_MICO_PART, partUri);
        return exchange;
    }
    
}
