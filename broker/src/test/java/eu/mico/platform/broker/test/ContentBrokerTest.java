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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Setup a simple analysis workflow, create a content item with an appropriate input type, then let it run and see
 * if analysis will terminate in the right state.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContentBrokerTest extends BaseBrokerTest {

    @Test
    public void testSimpleAnalyse() throws IOException, InterruptedException, RepositoryException {
        MICOBroker broker = new MICOBrokerImpl(testHost);

        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(EventManager.QUEUE_CONTENT_OUTPUT, true, consumer);

        // create a content item with a single part of type "A"; it should walk through the registered mock services and
        // eventually finish analysis; we simply wait until we receive an event on the output queue.
        PersistenceService svc = broker.getPersistenceService();
        ContentItem item = svc.createContentItem();
        try {
            Content partA = item.createContentPart();
            partA.setType("A");

            eventManager.injectContentItem(item);

            // wait for result notification and verify it contains what we expect
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
            Assert.assertNotNull(delivery);

            Event.ContentEvent event = Event.ContentEvent.parseFrom(delivery.getBody());

            Assert.assertEquals(item.getURI().stringValue(), event.getContentItemUri());

            // each service should have added a part, so there are now four different parts
            Set<Content> parts = ImmutableSet.copyOf(item.listContentParts());
            Assert.assertEquals(4, parts.size());
            Assert.assertThat(parts, Matchers.<Content>hasItem(hasProperty("type", equalTo("A"))));
            Assert.assertThat(parts, Matchers.<Content>hasItem(hasProperty("type", equalTo("B"))));
            Assert.assertThat(parts, Matchers.<Content>hasItem(hasProperty("type", equalTo("C"))));

        } finally {
            svc.deleteContentItem(item.getURI());
        }
    }
}
