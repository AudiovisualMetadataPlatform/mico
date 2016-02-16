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
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

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

    @Test
    public void testSimpleAnalyse() throws IOException, InterruptedException, RepositoryException, URISyntaxException {
        MICOBroker broker = new MICOBrokerImpl(testHost);

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
        try {
            Part partA = item.createPart();
            partA.setType("A");

            eventManager.injectItem(item);

            // wait for result notification and verify it contains what we expect
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
            Assert.assertNotNull(delivery);

            Event.PartEvent event = Event.PartEvent.parseFrom(delivery.getBody());

            Assert.assertEquals(item.getURI().stringValue(), event.getItemUri());

            // each service should have added a part, so there are now four different parts
            Set<Part> parts = ImmutableSet.copyOf(item.getParts());
            Assert.assertEquals(4, parts.size());
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("type", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("type", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Part>hasItem(hasProperty("type", equalTo("C"))));

        } finally {
            svc.deleteItem(item.getURI());
        }
    }
}
