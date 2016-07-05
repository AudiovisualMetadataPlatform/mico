package eu.mico.platform.camel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import de.fraunhofer.idmt.camel.MicoCamel;
import de.fraunhofer.idmt.mico.DummyExtractorComplexTest;
import eu.mico.platform.camel.log.LoggingSentEventNotifier;
import eu.mico.platform.persistence.model.Item;

/**
 * @author sld
 *
 */
public class MultithreadingTest extends TestBase {

    private static final int BATCH_SIZE = 2;
    private static final int PART_REPLICAS = 5;

    // static variable for detecting if any exception was thrown during the
    // routing
    public static Exception camelException = null;
    private static List<Item> items = new ArrayList<Item>();
    private static DummyExtractorComplexTest extractorA = new DummyExtractorComplexTest(
            "A", "B1orB2", PART_REPLICAS);
    private static DummyExtractorComplexTest extractorB1 = new DummyExtractorComplexTest(
            "B1", "C1", PART_REPLICAS);
    private static DummyExtractorComplexTest extractorB2 = new DummyExtractorComplexTest(
            "B2", "C2", PART_REPLICAS);
    private static DummyExtractorComplexTest extractorB2B = new DummyExtractorComplexTest(
            "B2", "C2", PART_REPLICAS);
    private static MicoCamelContext cc = null;
    private static String testHost;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {

                onException(MICOCamelAnalysisException.class)
                        .process(new Processor() {
                            public void process(Exchange exchange)
                                    throws Exception {
                                log.error("Notifying exception", exchange
                                        .getProperty(Exchange.EXCEPTION_CAUGHT,
                                                Exception.class));
                                camelException = exchange.getProperty(
                                        Exchange.EXCEPTION_CAUGHT,
                                        Exception.class);
                                ;
                                exchange.setException(null);
                            }
                        }).handled(true).stop();

                from("direct:complex-test-A-B1")
                        .pipeline()
                        .to("mock:in-direct:complex-test-A-B1")
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=A-B1orB2-queue&parameters={\"outputType\":\"B1\"}")
                        .multicast()
                        .to("direct:complex-test-B1-C1",
                                "direct:complex-test-B2-C2");

                from("direct:complex-test-A-B2")
                        .pipeline()
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=A-B1orB2-queue&parameters={\"outputType\":\"B2\"}")
                        .multicast()
                        .to("direct:complex-test-B1-C1",
                                "direct:complex-test-B2-C2");

                from("direct:complex-test-A-B1andB2")
                        .pipeline()
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=A-B1orB2-queue&parameters={\"outputType\":\"B1;B2\"}")
                        .multicast()
                        .to("direct:complex-test-B1-C1",
                                "direct:complex-test-B2-C2");

                from("direct:complex-test-B1-C1")
                        .pipeline()
                        .to("mock:in-direct:complex-test-B1-C1")
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=B1-C1-queue&inputs={\"B1\":[\"application/x-mico-rdf\"]}")
                        .to("mock:out-direct:complex-test-B1-C1");

                from("direct:complex-test-B2-C2")
                        .pipeline()
                        .to("mock:in-direct:complex-test-B2-C2")
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=B2-C2-queue&inputs={\"B2\":[\"application/x-mico-rdf\"]}")
                        .to("mock:out-direct:complex-test-B2-C2");

                try {
                    context.getManagementStrategy().addEventNotifier(new LoggingSentEventNotifier());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
    }

    @BeforeClass
    static public void init() throws Exception {
        if (micoCamel == null)
            try {
                micoCamel = new MicoCamel();
                micoCamel.init();
                micoCamel.registerService(extractorA, extractorB1, extractorB2,
                        extractorB2B);
                testHost = micoCamel.getTestHost();
                cc = new MicoCamelContext();
                cc.init(micoCamel.getEventManager().getPersistenceService());

            } catch (Exception e) {
                e.printStackTrace();
                fail("unable to setup test env");
            }
    }

    @AfterClass
    static public void shutdown() throws IOException {
        micoCamel.shutdown();
        micoCamel = null;
    }

    @Before
    public void prepare() throws RepositoryException {
        checkAssumptions();

        // set logLevel to warn, since we are running many tests
//        changeLogConfig();

        createItems();
    }

    @After
    public void cleanup() {
        for (Item i : items) {
            micoCamel.deleteContentItem(i.getURI().stringValue());
        }
        items.clear();

        // reset to use the standard log configuration
        resetLogConfig();
    }

    @Test
    public void TestA_B1() throws InterruptedException {
        MockEndpoint mock2 = getMockEndpoint("mock:in-direct:complex-test-A-B1");
        mock2.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * (PART_REPLICAS+1)+BATCH_SIZE);
        MockEndpoint mock = getMockEndpoint("mock:in-direct:complex-test-B1-C1");
        mock.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * (PART_REPLICAS+1));
        MockEndpoint mock1 = getMockEndpoint("mock:out-direct:complex-test-B1-C1");
        mock1.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * PART_REPLICAS);

        for (Item i : items) {
            final String uri = i.getURI().stringValue();
            log.debug("send item: {}", uri);
            new Runnable() {

                @Override
                public void run() {
                    template.send("direct:complex-test-A-B1",
                            createExchange(uri, "direct:complex-test-A-B1"));
                }
            }.run();

        }
        assertNull(camelException);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void TestA_B2() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:in-direct:complex-test-B2-C2");
        mock.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * (PART_REPLICAS+1));
        MockEndpoint mock1 = getMockEndpoint("mock:out-direct:complex-test-B2-C2");
        mock1.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * PART_REPLICAS);

        for (Item i : items) {
            template.send(
                    "direct:complex-test-A-B2",
                    createExchange(i.getURI().stringValue(),
                            "direct:complex-test-A-B2"));
        }
        assertNull(camelException);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void TestA_B1andB2() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:in-direct:complex-test-B1-C1");
        mock.expectedMessageCount(BATCH_SIZE * 2 * PART_REPLICAS * (PART_REPLICAS+1));
        MockEndpoint mockout = getMockEndpoint("mock:out-direct:complex-test-B1-C1");
        mockout.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * (2 * PART_REPLICAS));
        MockEndpoint mock2 = getMockEndpoint("mock:in-direct:complex-test-B2-C2");
        mock2.expectedMessageCount(BATCH_SIZE * 2 * PART_REPLICAS * (PART_REPLICAS+1));
        MockEndpoint mock2out = getMockEndpoint("mock:out-direct:complex-test-B2-C2");
        mock2out.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * (2 * PART_REPLICAS));

        for (Item i : items) {
            template.send(
                    "direct:complex-test-A-B1andB2",
                    createExchange(i.getURI().stringValue(),
                            "direct:complex-test-A-B1andB2"));
        }
        assertNull(camelException);
        assertMockEndpointsSatisfied();
    }

    /**
     * create and store/inject test data/item in mico persistence and store item
     * and part uri in class
     * 
     * @throws IOException
     * @throws RepositoryException
     */
    private static void createItems() throws RepositoryException {
        for (int i = 0; i < BATCH_SIZE; i++) {
            Item item = micoCamel.createItem();
            item.setSyntacticalType("A");
            items.add(item);
        }

    }

    /**
     * check if test would run to long
     */
    private static void checkAssumptions() {
        Assume.assumeFalse("Number of items is to high", BATCH_SIZE > 250);
        Assume.assumeFalse("Number of part replication is to high",
                PART_REPLICAS > 10);
    }

    /**
     * set logLevel to warn, since we are running many tests with same log
     * output
     */
    private static void changeLogConfig() {
        changeLogConfig(false);
    }

    /**
     * reset logging to use the standard configuration file
     */
    private static void resetLogConfig() {
        changeLogConfig(true);
    }

    /**
     * @param reset
     *            <i>true</i> to use the standard log configuration file,
     *            <i>false</i> for non-verbose logging
     */
    private static void changeLogConfig(boolean reset) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory
                .getILoggerFactory();
        loggerContext.reset();

        String logbackPropertiesUserFile = reset ? "src/test/resources/logback-test.xml"
                : "src/test/resources/logback-test_warn.xml";

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);

        try {
            configurator.doConfigure(logbackPropertiesUserFile);// loads logback
                                                                // file
        } catch (JoranException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
