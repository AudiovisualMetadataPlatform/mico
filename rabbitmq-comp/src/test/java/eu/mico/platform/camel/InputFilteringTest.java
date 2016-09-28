package eu.mico.platform.camel;

import com.google.common.collect.ImmutableSet;
import de.fraunhofer.idmt.camel.MicoCamel;
import de.fraunhofer.idmt.mico.DummyExtractor;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import eu.mico.platform.camel.aggretation.ItemAggregationStrategy;
import eu.mico.platform.camel.aggretation.SimpleAggregationStrategy;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.language.Bean;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

;

/**
 * @author cvo
 *
 */
public class InputFilteringTest extends TestBase {

    private static MicoCamelContext cc = null;

    @Test
    public void testReadUndefinedInputsReturnNull() throws Exception {

        MicoRabbitEndpoint ep = context
                .getEndpoint(
                        "mico-comp://vbox1?host=localhost&amp;serviceId=ParamTest&amp;extractorId=parameter-selection-test&amp;extractorVersion=1.0.0&amp;modeId=ParamTest&amp;parameters={&quot;value-param-0&quot;:&quot;8000&quot;,&quot;value-param-1&quot;:&quot;8000&quot;,&quot;value-param-2&quot;:&quot;_8kHz&quot;,&quot;value-param-3&quot;:&quot;enabled&quot;,&quot;value-param-4&quot;:&quot;1&quot;,&quot;value-param-5&quot;:&quot;3,7,56&quot;}",
                        MicoRabbitEndpoint.class);
        assertNull(ep.getModeInputs());
        assertNotNull(ep.getModeInputsAsMap());
        assertEquals(0, ep.getModeInputsAsMap().size());
    }

    @Test
    public void testReadDefinedInputsReturnNotNull() throws Exception {

        MicoRabbitEndpoint ep = context
                .getEndpoint(
                        "mico-comp://vbox1?extractorId=mico-extractor-test&extractorVersion=1.0.0&host=localhost&inputs=%7B%22A%22%3A%5B%22mico%2Ftest-mime-A%22%5D%2C%22B%22%3A%5B%22mico%2Ftest-mime-B%22%5D%7D&modeId=AB-C-queue&serviceId=AB-C-queue",
                        MicoRabbitEndpoint.class);
        assertNotNull(ep.getModeInputs());
        assertNotNull(ep.getModeInputsAsMap());
        assertEquals(2, ep.getModeInputsAsMap().size());
    }

    @Test
    public void testGetModeInputsReturnsCorrectMap() throws Exception {

        MicoRabbitEndpoint ep = context
                .getEndpoint(
                        "mico-comp://vbox1?extractorId=mico-extractor-test&extractorVersion=1.0.0&host=localhost&inputs=%7B%22A%22%3A%5B%22mico%2Ftest-mime-A-1%22%2C%22mico%2Ftest-mime-A-2%22%2C%22mico%2Ftest-mime-A-3%22%5D%2C%22B%22%3A%5B%22mico%2Ftest-mime-B%22%5D%7D&modeId=AB-C-queue&serviceId=AB-C-queuequeue",
                        MicoRabbitEndpoint.class);
        assertNotNull(ep.getModeInputs());
        assertNotNull(ep.getModeInputsAsMap());
        assertEquals(2, ep.getModeInputsAsMap().size());

        Map<String, List<String>> expectedMap = new HashMap<String, List<String>>();
        expectedMap.put("A", new ArrayList<String>());
        expectedMap.get("A").add("mico/test-mime-A-1");
        expectedMap.get("A").add("mico/test-mime-A-2");
        expectedMap.get("A").add("mico/test-mime-A-3");
        expectedMap.put("B", new ArrayList<String>());
        expectedMap.get("B").add("mico/test-mime-B");
        assertEquals(expectedMap, ep.getModeInputsAsMap());

    }

    @Test
    public void testInputIsNotFilteredIfNotDeclared() throws Exception {

        DummyExtractor extr1 = new DummyExtractor("video/mp4", "mico:Video",
                "parameter-selection-test", "1.0.0", "ParamTest");
        micoCamel.registerService(extr1);

        Item testItem = micoCamel.createItem();
        testItem.setSyntacticalType("mico:InvalidSyntacticalType");
        testItem.getAsset().setFormat("mico/invalid-format");

        MockEndpoint mock = getMockEndpoint("mock:result_simpleParams");
        mock.reset();
        mock.expectedMessageCount(10);
        for (int i = 0; i < 10; i++) {
            template.send(
                    "direct:workflow-simpleParams,mimeType=video/mp4,syntacticType=mico:Video",
                    createExchange(testItem.getURI().stringValue(),
                            "direct:workflow-simpleParams,mimeType=video/mp4,syntacticType=mico:Video"));

        }
        assertMockEndpointsSatisfied();
        mock.reset();
        micoCamel.deleteContentItem(testItem.getURI().stringValue());
    }

    @Test
    public void testInvalidInputIsFiltered() throws Exception {

        Item testItem = micoCamel.createItem();
        testItem.setSyntacticalType("mico:InvalidSyntacticalType");
        testItem.getAsset().setFormat("mico/invalid-format");

        MockEndpoint mockBeforeFiltering = getMockEndpoint("mock:result_inputDefinitionAndFiltering_beforeExtractor");
        mockBeforeFiltering.expectedMessageCount(10);

        MockEndpoint mockAfterFiltering = getMockEndpoint("mock:result_inputDefinitionAndFiltering_afterExtractor");
        mockAfterFiltering.expectedMessageCount(0);

        for (int i = 0; i < 10; i++) {
            template.send(
                    "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-A,syntacticType=A",
                    createExchange(
                            testItem.getURI().stringValue(),
                            "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-A,syntacticType=A"));
            template.send(
                    "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-B,syntacticType=B",
                    createExchange(
                            testItem.getURI().stringValue(),
                            "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-B,syntacticType=B"));
        }

        assertMockEndpointsSatisfied();
        micoCamel.deleteContentItem(testItem.getURI().stringValue());
    }

    @Test
    public void testCorrectInputIsProcessed() throws Exception {

        MockEndpoint mockBeforeFiltering = getMockEndpoint("mock:result_inputDefinitionAndFiltering_beforeExtractor");
        mockBeforeFiltering.reset();

        MockEndpoint mockAfterFiltering = getMockEndpoint("mock:result_inputDefinitionAndFiltering_afterExtractor");
        mockAfterFiltering.reset();

        for (int mimeIdx = 1; mimeIdx <= 3; mimeIdx++) {

            Item testItem = micoCamel.createItem();

            Part partA = testItem.createPart(new URIImpl(
                    "uri:test-input-processing"));
            partA.setSyntacticalType("A");
            Asset assetA = partA.getAsset();
            assetA.setFormat("mico/test-mime-A-" + Integer.toString(mimeIdx));
            OutputStream outputStreamA = assetA.getOutputStream();
            outputStreamA.write(("Initial content of " + assetA.getFormat())
                    .getBytes());
            outputStreamA.close();

            Part partB = testItem.createPart(new URIImpl(
                    "uri:test-input-processing"));
            partB.setSyntacticalType(MMMTERMS.NS + "B");
            Asset assetB = partB.getAsset();
            assetB.setFormat("mico/test-mime+B");
            OutputStream outputStreamB = assetB.getOutputStream();
            outputStreamB.write(("Initial content of " + assetB.getFormat())
                    .getBytes());
            outputStreamB.close();

            for (int i = 0; i < 10; i++) {
                template.send(
                        "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-A,syntacticType=A",
                        createExchange(
                                testItem.getURI().stringValue(),
                                partA.getURI().stringValue(),
                                "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-A,syntacticType=A"));
                template.send(
                        "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-B,syntacticType=B",
                        createExchange(
                                testItem.getURI().stringValue(),
                                partB.getURI().stringValue(),
                                "direct:aggregateComplex-inputDefinition-mimeType=mico/test-mime-B,syntacticType=B"));
            }

            mockBeforeFiltering.expectedMessageCount(10 * mimeIdx);
            mockAfterFiltering.expectedMessageCount(10 * mimeIdx);

            Iterable<? extends Part> partsIt = testItem.getParts();
            ImmutableSet<Part> parts = ImmutableSet.copyOf(partsIt);
            assertEquals(12, parts.size());
            assertThat(parts, Matchers.<Part> hasItem(hasProperty("syntacticalType", equalTo("A"))));
            assertThat(parts, Matchers.<Part> hasItem(hasProperty("syntacticalType", equalTo(MMMTERMS.NS + "B"))));
            assertThat(parts, Matchers.<Part> hasItem(hasProperty("syntacticalType", equalTo("C"))));
            assertThat(parts, Matchers.<Part> hasItem(hasProperty("semanticType", equalTo((String) null))));
            assertThat(parts, Matchers.<Part> hasItem(hasProperty("semanticType", equalTo("mico-extractor-test-1.0.0-AB-C-queue"))));

            assertMockEndpointsSatisfied();
            micoCamel.deleteContentItem(testItem.getURI().stringValue());
        }
    }

    @Bean(ref = "simpleAggregatorStrategy")
    public static SimpleAggregationStrategy simpleAggregatorStrategy = new SimpleAggregationStrategy();

    @Bean(ref = "itemAggregatorStrategy")
    public static ItemAggregationStrategy itemAggregatorStrategy = new ItemAggregationStrategy();

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                JndiRegistry registry = (JndiRegistry) ((PropertyPlaceholderDelegateRegistry) context
                        .getRegistry()).getRegistry();

                if (registry.lookup("simpleAggregatorStrategy") == null)
                    // and here, it is bound to the registry
                    registry.bind("simpleAggregatorStrategy",
                            simpleAggregatorStrategy);

                if (registry.lookup("itemAggregatorStrategy") == null)
                    // and here, it is bound to the registry
                    registry.bind("itemAggregatorStrategy",
                            itemAggregatorStrategy);

                loadXmlSampleRoutes();

            }

            private void loadXmlSampleRoutes() {
                ModelCamelContext context = getContext();
                context.setDelayer(CONTEXT_DELAYER);
                String[] testFiles = {
                        "src/test/resources/routes/single_extractor_with_input_definitions.xml",
                        "src/test/resources/routes/single_extractor_with_parameters.xml" };
                try {
                    for (int i = 0; i < testFiles.length; i++) {
                        InputStream is = new FileInputStream(testFiles[i]);
                        log.debug("add Route: {}", testFiles[i]);
                        RoutesDefinition routes = context
                                .loadRoutesDefinition(is);
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

        if (micoCamel == null)
            try {
                micoCamel = new MicoCamel();
                micoCamel.init();

                cc = new MicoCamelContext();
                cc.init(micoCamel.getEventManager().getPersistenceService());

            } catch (Exception e) {
                e.printStackTrace();

                Assume.assumeTrue("Unable to setup test environment" +
                        "tests are probably run against a productive mico instance", false);

            }
    }

    @AfterClass
    static public void cleanup() throws IOException {

        if (micoCamel != null) {
            micoCamel.shutdown();
            micoCamel = null;
        }
    }

}
