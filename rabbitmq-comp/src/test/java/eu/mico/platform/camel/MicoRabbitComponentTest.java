package eu.mico.platform.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import de.fraunhofer.idmt.camel.MicoCamel;

public class MicoRabbitComponentTest extends CamelTestSupport {

    @Test
    public void testMicoRabbit() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }

    @Test
    @Ignore
    public void testMicoCamel() throws Exception {
    	MicoCamel micoCamel = new MicoCamel();
		micoCamel.init();
		micoCamel.testSimpleAnalyse();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
            	getContext().setDelayer(300L);                
                
                from("mico-comp://foo").pipeline()
//                  .inOut("mico-comp://bar")
                  .to("mico-comp://baz")
                  .to("mock:result");
                
                from("file:src/test/resources/data?startingDirectoryMustExist=true&startingDirectoryMustExist=true&initialDelay=100&delay=5000")
                .to("mico-comp:vbox?host=mico-platform&user=mico&password=mico")
                .to("mock:result");
            }
        };
    }
    
    
}
