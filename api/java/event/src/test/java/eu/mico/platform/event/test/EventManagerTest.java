package eu.mico.platform.event.test;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import org.junit.Test;

import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class EventManagerTest extends BaseCommunicationTest {


    @Test
    public void testCreateEventManager() throws IOException {
        EventManager mgr = new EventManagerImpl(testHost);
        mgr.init();
        mgr.shutdown();
    }
}
