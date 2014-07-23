package eu.mico.platform.event.api;

import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface EventManager {

    /**
     * Register the given service with the event manager.
     *
     * @param service
     */
    public void registerService(AnalysisService service) throws IOException;


    /**
     * Initialise the event manager, setting up any necessary channels and connections
     */
    public void init() throws IOException;


    /**
     * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
     */
    public void shutdown() throws IOException;
}
