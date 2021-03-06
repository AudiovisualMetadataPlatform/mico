/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.camel;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.google.common.collect.ImmutableSet;
import com.rabbitmq.client.AlreadyClosedException;
import de.fraunhofer.idmt.camel.MicoCamel;
import de.fraunhofer.idmt.mico.DummyExtractorComplexTest;
import eu.mico.platform.camel.log.LoggingSentEventNotifier;
import eu.mico.platform.camel.split.SplitterNewParts;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.junit.*;
import org.openrdf.repository.RepositoryException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author sld
 *
 */
public class MultithreadingTest extends TestBase {

    private static final int BATCH_SIZE = 200;
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

    @Bean(ref="splitterNewPartsBean")
    public static SplitterNewParts splitterNewParts = new SplitterNewParts();


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                JndiRegistry registry = (JndiRegistry) (
                        (PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();

                if(registry.lookup("splitterNewPartsBean") == null){
                    registry.bind("splitterNewPartsBean", splitterNewParts);
                }
                
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
                        .to("mock:in-direct:complex-test-A-B1?retainLast=100")
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=A-B1orB2-queue&parameters={\"outputType\":\"B1\"}")
                        .split().method("splitterNewPartsBean","splitMessage")
                        .multicast()
                        .to("direct:complex-test-B1-C1",
                                "direct:complex-test-B2-C2");

                from("direct:complex-test-A-B2")
                        .pipeline()
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=A-B1orB2-queue&parameters={\"outputType\":\"B2\"}")
                        .split().method("splitterNewPartsBean","splitMessage")
                        .multicast()
                        .to("direct:complex-test-B1-C1",
                                "direct:complex-test-B2-C2");

                from("direct:complex-test-A-B1andB2")
                        .pipeline()
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=A-B1orB2-queue&parameters={\"outputType\":\"B1;B2\"}")
                        .split().method("splitterNewPartsBean","splitMessage")
                        .multicast()
                        .to("direct:complex-test-B1-C1",
                                "direct:complex-test-B2-C2");

                from("direct:complex-test-B1-C1")
                        .pipeline()
                        .to("mock:in-direct:complex-test-B1-C1?retainLast=100")
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=B1-C1-queue&inputs={\"B1\":[\"application/x-mico-rdf\"]}")
                        .split().method("splitterNewPartsBean","splitMessage")
                        .to("mock:out-direct:complex-test-B1-C1?retainLast=100");

                from("direct:complex-test-B2-C2")
                        .pipeline()
                        .to("mock:in-direct:complex-test-B2-C2?retainLast=100")
                        .to("mico-comp:complexbox1?host="
                                + testHost
                                + "&extractorId=mico-extractor-complex-test&extractorVersion=1.0.0&modeId=B2-C2-queue&inputs={\"B2\":[\"application/x-mico-rdf\"]}")
                        .split().method("splitterNewPartsBean","splitMessage")
                        .to("mock:out-direct:complex-test-B2-C2?retainLast=100");

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
        if (micoCamel == null) {
            try {
                micoCamel = new MicoCamel();
                micoCamel.init();
                micoCamel.registerService(extractorA, extractorB1, extractorB2,
                        extractorB2B);

                testHost = micoCamel.getTestHost();
                cc = new MicoCamelContext();
                cc.init(micoCamel.getEventManager().getPersistenceService());
            } catch (AlreadyClosedException e) {
                Assume.assumeTrue("Config_request channel locked" +
                        "tests are probably run against a productive mico instance", false);
            } catch (java.net.ConnectException e) {
                Assume.assumeTrue("Unable to setup test environment: " +
                        e.getMessage(), false);
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("unable to setup test env: " + e.getMessage());
            }

        }

        Item testItem = micoCamel.createItem();
        if (testItem == null) {
            Assume.assumeTrue("Mico Camel not properly initialized", false);
        }
    }

    @AfterClass
    static public void shutdown() throws IOException {
        if (micoCamel != null) {
            micoCamel.shutdown();
            micoCamel = null;
        }
    }

    @Before
    public void prepare() throws RepositoryException {
        checkAssumptions();

        // set logLevel to warn, since we are running many tests
        changeLogConfig();

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
        MockEndpoint mock2 = getMockEndpoint("mock:in-direct:complex-test-A-B1?retainLast=100");
        mock2.expectedMessageCount(BATCH_SIZE);
        MockEndpoint mock = getMockEndpoint("mock:in-direct:complex-test-B1-C1?retainLast=100");
        mock.expectedMessageCount(BATCH_SIZE * PART_REPLICAS);
        MockEndpoint mock1 = getMockEndpoint("mock:out-direct:complex-test-B1-C1?retainLast=100");
        mock1.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * PART_REPLICAS);

        final String endpointUri = "direct:complex-test-A-B1";
        injectItems(endpointUri);

        checkItems(true, false);
        assertNull(camelException);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void TestA_B2() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:in-direct:complex-test-B2-C2?retainLast=100");
        mock.expectedMessageCount(BATCH_SIZE * PART_REPLICAS);
        MockEndpoint mock1 = getMockEndpoint("mock:out-direct:complex-test-B2-C2?retainLast=100");
        mock1.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * PART_REPLICAS);

        final String endpointUri = "direct:complex-test-A-B2";
        injectItems(endpointUri);
        
        checkItems(false, true);
        assertNull(camelException);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void TestA_B1andB2() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:in-direct:complex-test-B1-C1?retainLast=100");
        mock.expectedMessageCount(BATCH_SIZE * 2 * PART_REPLICAS );
        MockEndpoint mockout = getMockEndpoint("mock:out-direct:complex-test-B1-C1?retainLast=100");
        mockout.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * PART_REPLICAS);
        MockEndpoint mock2 = getMockEndpoint("mock:in-direct:complex-test-B2-C2?retainLast=100");
        mock2.expectedMessageCount(BATCH_SIZE * 2 * PART_REPLICAS);
        MockEndpoint mock2out = getMockEndpoint("mock:out-direct:complex-test-B2-C2?retainLast=100");
        mock2out.expectedMessageCount(BATCH_SIZE * PART_REPLICAS * PART_REPLICAS);

        final String endpointUri = "direct:complex-test-A-B1andB2";
        injectItems(endpointUri);

        checkItems(true, true);
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

            Assert.assertNotNull(item);

            item.setSyntacticalType("A");
            item.setSemanticType("Item-"+String.format("%03d", i));
            items.add(item);
        }

    }

    /**
     * @param endpointUri
     * @throws InterruptedException
     */
    private void injectItems(final String endpointUri)
            throws InterruptedException {
        List<Future<?>> tasks = new ArrayList<Future<?>>();
        for (Item i : items) {
            final String uri = i.getURI().stringValue();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            tasks.add(executor.submit(new Runnable() {

                @Override
                public void run() {
                    template.send(endpointUri,
                            createExchange(uri, endpointUri));
                }
            }));

        }
        //wait for all injects to finish
        for (Future<?> t : tasks){
            try {
                t.get();
            } catch (ExecutionException e) {
                fail(e.getMessage());
            }
        }
    }
    

    private void checkItems(boolean enableA, boolean enableB) {
        for (Item i : items) {
            try {
                int base = enableA && enableB ? 2 :1;
                int expected = base
                        * (PART_REPLICAS + PART_REPLICAS * PART_REPLICAS);
                ImmutableSet<Part> parts = ImmutableSet.copyOf(i.getParts());
                if (expected != parts.size()) {
                    log.error("item {}[{}] has wrong amount of parts ({}/{})",
                            i.getURI(), i.getSemanticType(), parts.size(),
                            expected);
                    fail("item with " + parts.size() + " parts should have "
                            + expected);
                }
                int partB1 = 0, partB2 = 0,partC1 = 0, partC2 = 0;
                for(Part part : parts){
                    if(part.getSyntacticalType().startsWith("B1")){
                        assertEquals("B1", part.getSyntacticalType());
                        partB1++;
                    }else if(part.getSyntacticalType().startsWith("B2")){
                        assertEquals("B2", part.getSyntacticalType());
                        partB2++;
                    }else if(part.getSyntacticalType().startsWith("C1")){
                        assertEquals("C1", part.getSyntacticalType());
                        partC1++;
                    }else{ //must be C2
                        assertEquals("C2", part.getSyntacticalType());
                        partC2++;
                    }
                    if(part.getSemanticType().startsWith("mico-extractor-complex-test-1.0.0-A-B1orB2-queue")){
                        assertEquals("mico-extractor-complex-test-1.0.0-A-B1orB2-queue", part.getSemanticType());
                    }else if(part.getSemanticType().startsWith("mico-extractor-complex-test-1.0.0-B1-C1-queue")){
                        assertEquals("mico-extractor-complex-test-1.0.0-B1-C1-queue", part.getSemanticType());
                    }else{ 
                        assertEquals("mico-extractor-complex-test-1.0.0-B2-C2-queue", part.getSemanticType());
                    }
                }

                if (enableA){
                    assertEquals(PART_REPLICAS, partB1);
                    assertEquals(PART_REPLICAS*PART_REPLICAS, partC1);
                }else{
                    assertEquals(0, partB1);
                    assertEquals(0, partC1);
                }
                if (enableB){
                    assertEquals(PART_REPLICAS, partB2);
                    assertEquals(PART_REPLICAS*PART_REPLICAS, partC2);
                }else{
                    assertEquals(0, partB2);
                    assertEquals(0, partC2);
                }
            } catch (RepositoryException e) {
                fail("Error getting parts: " + e.getMessage());
            }
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
