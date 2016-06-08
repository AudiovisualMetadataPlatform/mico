/**
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
package eu.mico.platform.broker.test;

import com.google.common.collect.ImmutableSet;

import eu.mico.platform.camel.MicoCamelContext;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;

import org.apache.camel.Endpoint;
import org.apache.camel.NoSuchEndpointException;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Setup a simple analysis workflow, create a item with an appropriate input type, then let it run and see
 * if analysis will terminate in the right state.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CamelBrokerTest extends BaseBrokerTest {

    private static Logger log = LoggerFactory.getLogger(CamelBrokerTest.class);
    private static MicoCamelContext context = new MicoCamelContext();

    @Test(timeout=40000)
    public void testSimpleWorkflow() throws Exception {
        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");
        
        MockEndpoint mock = getMockEndpoint("mock:result_simple1");
        mock.expectedMinimumMessageCount(1);       

        PersistenceService ps = broker.getPersistenceService();
        Item item = null;
        try {

            // create a item with a single part of type "A"; it should walk through the registered mock services and
            // eventually finish analysis; we simply wait until we receive an event on the output queue.
            item = ps.createItem();
            item.setSemanticType("A");
            item.setSyntacticalType("A");
            Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(0, parts.size());

            context.processItem("direct:a", item.getURI().toString());;
            

            // wait for result notification and verify it contains what we expect
            assertMockEndpointsSatisfied(mock);


            // each service should have added a part, so there are now four different parts
            parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(2, parts.size());
            Assert.assertThat(item.getSyntacticalType(), equalTo("A"));
            Assert.assertThat(item.getSemanticType(), equalTo("A"));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("C"))));

        } finally {
            if(item != null){
                ps.deleteItem(item.getURI());
            }
        }
    }

    @Test(timeout=40000)
    public void testAggregateWorkflow() throws Exception {
        setupMockAnalyser("A","B1");
        setupMockAnalyser("A","B2");
        setupMockAnalyser("B","C");
        
        MockEndpoint mock = getMockEndpoint("mock:result_aggregateSimple_1");
        mock.expectedMessageCount(2);       
        MockEndpoint mock2 = getMockEndpoint("mock:result_aggregateSimple_2");
        mock.expectedMessageCount(2);       

        PersistenceService ps = broker.getPersistenceService();
        Item item = null;
        try {

            // create a item with a single part of type "A"; it should walk through the registered mock services and
            // eventually finish analysis; we simply wait until we receive an event on the output queue.
            item = ps.createItem();
            item.setSemanticType("A");
            item.setSyntacticalType("A");
            Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(0, parts.size());

            context.processItem("direct:b", item.getURI().toString());;
            

            // wait for result notification and verify it contains what we expect
            assertMockEndpointsSatisfied(mock, mock2);


            // each service should have added a part, so there are now four different parts
            parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(4, parts.size());
            Assert.assertThat(item.getSyntacticalType(), equalTo("A"));
            Assert.assertThat(item.getSemanticType(), equalTo("A"));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B1"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B2"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B1"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B2"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("C"))));

        } finally {
            if(item != null){
                ps.deleteItem(item.getURI());
            }
        }
    }

    /**
     * Resolves the {@link MockEndpoint} using a URI of the form
     * <code>mock:someName</code>, optionally creating it if it does not exist.
     *
     * @param uri
     *            the URI which typically starts with "mock:" and has some name
     * @return the mock endpoint or an {@link NoSuchEndpointException} is thrown
     *         if it could not be resolved
     * @throws NoSuchEndpointException
     *             is the mock endpoint does not exists
     */
    protected MockEndpoint getMockEndpoint(String uri)
            throws NoSuchEndpointException {
        Endpoint endpoint = context.hasEndpoint(uri);
        if (endpoint instanceof MockEndpoint) {
            return (MockEndpoint) endpoint;
        }
        throw new NoSuchEndpointException(String.format(
                "MockEndpoint %s does not exist.", uri));
    }

    /**
     * Asserts that all the expectations of the Mock endpoints are valid
     * @param mock 
     */
    protected void assertMockEndpointsSatisfied(MockEndpoint... mock) throws InterruptedException {
        MockEndpoint.assertIsSatisfied(mock);
    }

    /**
     * Asserts that all the expectations of the Mock endpoints are valid
     */
    protected void assertMockEndpointsSatisfied(long timeout, TimeUnit unit, MockEndpoint... mock) throws InterruptedException {
        MockEndpoint.assertIsSatisfied(timeout, unit,mock);
    }
    
    
    @BeforeClass
    public static void prepareContext() throws Exception {

        // load route from XML and add them to the existing camel context
        InputStream is = CamelBrokerTest.class.getClassLoader().getResourceAsStream("camel-routes.xml");
        if (is == null){
            Assert.fail("sample routes not found");
        }
        context.init();
        context.loadRoutes(is);
        log.info("sample routes loaded");
    }
}
