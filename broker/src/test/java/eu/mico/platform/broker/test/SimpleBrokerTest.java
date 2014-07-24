package eu.mico.platform.broker.test;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SimpleBrokerTest extends BaseBrokerTest {

    @Test
    public void testInit() throws IOException, InterruptedException {
        MICOBroker broker = new MICOBrokerImpl(testHost);

        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }

        // check dependency graph
        Assert.assertEquals(3, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(3, broker.getDependencies().vertexSet().size());
    }


    @Test
    public void testDiscovery() throws IOException, InterruptedException {
        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        MICOBroker broker = new MICOBrokerImpl(testHost);

        // wait for broker to finish with discovery ...
        Thread.sleep(500);


        // check dependency graph
        Assert.assertEquals(3, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(3, broker.getDependencies().vertexSet().size());
    }
}
