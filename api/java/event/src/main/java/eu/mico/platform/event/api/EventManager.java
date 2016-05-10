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
package eu.mico.platform.event.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;

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
    String QUEUE_PART_OUTPUT = "content_output";

    /**
     * Name of the queue used to to receive config requests.
     */
    String QUEUE_CONFIG_REQUEST = "config_request";

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
    public void injectItem(Item item) throws IOException;


    /**
     * Get persistence service to create, get and delete ContentItems.
     *
     * @return PersistenceService
     */
    PersistenceService getPersistenceService();

    /**
     * Initialise the event manager, setting up any necessary channels and connections
     *
     * @throws IOException, if something goes wrong while interacting with the AMQP server.
     * @throws TimeoutException, if there is no reply on a a configuration discovery request.
     * @throws URISyntaxException, if the configuration reply contains invalid URIs.
     */
    public void init() throws IOException, TimeoutException, URISyntaxException;


    /**
     * Shut down the event manager, cleaning up and closing any registered channels, services and connections.
     */
    public void shutdown() throws IOException;
}
