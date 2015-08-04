package eu.mico.platform.camel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spi.PollingConsumerPollStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;

/**
 * Unit test for poll strategy
 */
public class FilePollTest extends CamelTestSupport {

    private static int maxPolls;
    private final CountDownLatch latch = new CountDownLatch(1);

    private String fileUrl = "file://target/pollstrategy/?consumer.pollStrategy=#myPoll";

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("myPoll", new MyPollStrategy());
        return jndi;
    }

    @Override
	public void setUp() throws Exception {
        deleteDirectory("target/pollstrategy");
        super.setUp();
    }

    public void testPolledMessages() throws Exception {
        template.sendBodyAndHeader("file:target/pollstrategy/", "Hello World", Exchange.FILE_NAME, "hello.txt");
        template.sendBodyAndHeader("file:target/pollstrategy/", "Bye World", Exchange.FILE_NAME, "bye.txt");

        // start route now files have been created
        context.startRoute("foo");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(2);

        assertMockEndpointsSatisfied();

        // wait for commit to be issued
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        assertEquals(2, maxPolls);
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(fileUrl).routeId("foo").noAutoStartup()
                    .convertBodyTo(String.class).to("mock:result");
            }
        };
    }

    private class MyPollStrategy implements PollingConsumerPollStrategy {

        public boolean begin(Consumer consumer, Endpoint endpoint) {
            return true;
        }

        public void commit(Consumer consumer, Endpoint endpoint, int polledMessages) {
            if (polledMessages > maxPolls) {
                maxPolls = polledMessages;
            }
            latch.countDown();
        }

        public boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter, Exception cause) throws Exception {
            return false;
        }
    }

}
