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
import com.rabbitmq.client.QueueingConsumer;

import eu.mico.platform.broker.testutils.TestUtils;
import eu.mico.platform.broker.webservices.SwingImageCreator;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.Event.ItemEvent;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Setup a simple analysis workflow, create a item with an appropriate input type, then let it run and see
 * if analysis will terminate in the right state.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PartBrokerTest extends BaseBrokerTest {

    private static Logger log = LoggerFactory.getLogger(PartBrokerTest.class);
            
    @Test(timeout=10000)
    public void testSimpleAnalyse() throws IOException, InterruptedException, RepositoryException, URISyntaxException {

        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C",true);
        setupMockAnalyser("A","C");

        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(EventManager.QUEUE_PART_OUTPUT, true, consumer);

        // create a item with a single part of type "A"; it should walk through the registered mock services and
        // eventually finish analysis; we simply wait until we receive an event on the output queue.
        PersistenceService svc = broker.getPersistenceService();
        Item item = svc.createItem();
        item.setSemanticType("A");
        item.setSyntacticalType("A");
        try {
            Part partA = item.createPart(item.getURI());
            partA.setSemanticType("A");
            partA.setSyntacticalType("A");

            eventManager.injectItem(item);

            FileOutputStream output = new FileOutputStream("graph.png");
            SwingImageCreator.createGraph(broker.getDependencies(), new Dimension(640, 480), "png", output);
            
            
            // wait for result notification and verify it contains what we expect
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(200000);
            Assert.assertNotNull(delivery);

            ItemEvent event = ItemEvent.parseFrom(delivery.getBody());

            Assert.assertEquals(item.getURI().stringValue(), event.getItemUri());

            // each service should have added a part, so there are now four different parts
            Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(4, parts.size());
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("C"))));

        } finally {
            svc.deleteItem(item.getURI());
        }
    }

    @Test
    public void test2ItemAnalyse() throws IOException, InterruptedException, RepositoryException, URISyntaxException {

        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(EventManager.QUEUE_PART_OUTPUT, true, consumer);

        // create a item with a single part of type "A"; it should walk through the registered mock services and
        // eventually finish analysis; we simply wait until we receive an event on the output queue.
        PersistenceService svc = broker.getPersistenceService();
        Item item = svc.createItem();
        item.setSemanticType("A");
        item.setSyntacticalType("A");
        Item item2 = svc.createItem();
        item2.setSemanticType("A");
        item2.setSyntacticalType("A");
        item2.getAsset().setFormat("A");
        try {
            ImmutableSet<Part> parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals("This item should not have parts",0, parts.size());

            Part partA = item.createPart(new URIImpl("http://test2ItemAnalyse-injector"));
            partA.setSemanticType("A");
            partA.setSyntacticalType("A");
            
            
            // TODO: check why the test fails when executing this 2 lines see PUBLISHING-139
//            parts = ImmutableSet.copyOf(item.getParts());
//            Assert.assertEquals("There should be one part with type:'A'",1, parts.size());
            
            eventManager.injectItem(item);

            // wait for result notification and verify it contains what we expect
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(200000);
            Assert.assertNotNull(delivery);

            ItemEvent event = ItemEvent.parseFrom(delivery.getBody());

            Assert.assertEquals(item.getURI().stringValue(), event.getItemUri());

            // each service should have added a part, so there are now four different parts
            parts = null;
            parts = ImmutableSet.copyOf(item.getParts());
            if(parts.size() != 4){
                // This shows correct content of item annotations (4 parts)  
                TestUtils.debugRDF(log, item.getObjectConnection());
            }
            Assert.assertEquals("checking #parts after processing, ",4, parts.size());
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("C"))));

            //inject item2
            eventManager.injectItem(item2);

            // wait for result notification and verify it contains what we expect
            delivery = consumer.nextDelivery(200000);
            Assert.assertNotNull(delivery);

            event = ItemEvent.parseFrom(delivery.getBody());
            Assert.assertEquals(item2.getURI().stringValue(), event.getItemUri());

            // same test as above ... parts should not have changed
            parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals("injecting 2nd item should not change this",4, parts.size());
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("C"))));


            //item2 only has 3 parts, the first asset is direct on the item
            parts = ImmutableSet.copyOf(item2.getParts());
            Assert.assertEquals(3, parts.size());
            Assert.assertThat(parts, Matchers.not(Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("A")))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("syntacticalType", equalTo("C"))));
            Assert.assertThat(parts, Matchers.not(Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("A")))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("semanticType", equalTo("C"))));

        } finally {
            svc.deleteItem(item.getURI());
            svc.deleteItem(item2.getURI());
        }
    }
}
