package eu.mico.platform.camel;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fraunhofer.idmt.camel.MicoCamel;

/**
 * @author sld
 *
 */
public class MicoRabbitComponentTest extends CamelTestSupport {

    private static final String TEST_DATA = "src/test/resources/data";
    private static MicoCamel micoCamel;


    @Test
    public void testMicoRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
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
                
                from("file:"
                        + TEST_DATA
                        + "?startingDirectoryMustExist=true&startingDirectoryMustExist=true&initialDelay=100&delay=5000").pipeline()
                .to("mico-comp:vbox?host=mico-platform&user=mico&password=mico&serviceId=wordcount")
                .to("mico-comp://foo?serviceId=foo")
                .to("mico-comp://bar?serviceId=bar")
                .to("mock:result");
            }
        };
    }

    @BeforeClass
    static public void init() throws Exception {
        resetDataFolder();

        micoCamel = new MicoCamel();
        micoCamel.init();
    }
    
    @AfterClass
    static public void cleanup(){
        resetDataFolder();
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
    
    
}
