package eu.mico.platform.event.api;

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;

import java.io.IOException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface EventManager {

    /**
     * Name of service registry exchange where brokers bind their registration queues. The event manager will send
     * a registration event to this exchange every time a new service is registered.
     */
    String EXCHANGE_SERVICE_REGISTRY  = "service_registry";
    /**
     * Name of service discovery exchange where brokers send discovery requests. The event manager binds its own
     * discovery queue to this exchange and reacts on any incoming discovery events by sending its service list to
     * the replyTo queue provided by the requester.
     */
    String EXCHANGE_SERVICE_DISCOVERY = "service_discovery";

    /**
     * Name of the queue used for injecting content items when they are newly added to the system.
     */
    String QUEUE_CONTENT_INPUT  = "content_input";

    /**
     * Name of the queue used for reporting about content items where processing is finished.
     */
    String QUEUE_CONTENT_OUTPUT = "content_output";

    /**
     * Register the given service with the event manager.
     *
     * @param service
     */
    public void registerService(AnalysisService service) throws IOException;


    /**
     * Unregister the service with the given ID.
     * @param service
     * @throws IOException
     */
    public void unregisterService(AnalysisService service) throws IOException;

    /**
     * Trigger analysis of the given content item.
     *
     * @param item content item to analyse
     * @throws IOException
     */
    public void injectContentItem(ContentItem item) throws IOException;


    PersistenceService getPersistenceService();

    /**
     * Initialise the event manager, setting up any necessary channels and connections
     */
    public void init() throws IOException;


    /**
     * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
     */
    public void shutdown() throws IOException;
}
