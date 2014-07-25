package eu.mico.platform.broker.test;

import com.google.common.collect.ImmutableSet;
import com.rabbitmq.client.QueueingConsumer;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.Event;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceImpl;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class WebBrokerTest extends BaseBrokerTest {


    @Test
    public void testSetup() throws IOException, InterruptedException, RepositoryException {
        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        Thread.sleep(500);


        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(EventManager.QUEUE_CONTENT_OUTPUT, true, consumer);

        // create a content item with a single part of type "A"; it should walk through the registered mock services and
        // eventually finish analysis; we simply wait until we receive an event on the output queue.
        PersistenceService svc = new PersistenceServiceImpl(testHost);
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
