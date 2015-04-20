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
package eu.mico.platform.event.api.spring;

import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.persistence.api.PersistenceService;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Configuration and lifecycle of the MICO platform configuration. Consists of initialisation methods, shutdown hook and access to central platform components.
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
public class PlatformConfiguration implements ApplicationContextAware{


    /**
     * Host address of the MICO platform
     */
    private String host;
    /**
     * Connection to the MICO platform
     */
    private EventManagerImpl eventManager;
    /**
     * Connected analysis services
     */
    private List<eu.mico.platform.event.api.AnalysisService> analysisServices = new ArrayList<AnalysisService>();

    /**
     * Spring application context
     */
    private ApplicationContext applicationContext;
    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(PlatformConfiguration.class);

    /**
     * Constructor
     */
    public PlatformConfiguration() {
    }

    /**
     *
     * @throws IOException, URISyntaxException
     */
    public void init() throws IOException, URISyntaxException {
        logger.info("Connect to MICO platform at " + host);
        eventManager = new EventManagerImpl(host);
        eventManager.init();

        final Map<String, Object> extractors = applicationContext.getBeansWithAnnotation(eu.mico.platform.event.api.spring.AnalysisService.class);

        if(extractors.size() == 0) {
            logger.info("No extractor with AnalysisService annotation found.");
        }

        for (Object extractor : extractors.values()) {
            Class<? extends Object> extractorClass = extractor.getClass();

            if(Arrays.asList(extractorClass.getInterfaces()).contains(Analyser.class)) {
                eu.mico.platform.event.api.spring.AnalysisService annotation = extractorClass.getAnnotation(eu.mico.platform.event.api.spring.AnalysisService.class);

                logger.info("Subscribing " + extractorClass + " (" + annotation.id() + "," + annotation.requires() + "," + annotation.provides() + ") to MICO platform");
                String queueName = annotation.queueName().isEmpty() ? null : annotation.queueName();

                AnalysisService analysisService = new AnalyserProxy(new URIImpl(annotation.id()), annotation.provides(), annotation.requires(), queueName, (Analyser) extractor);

                eventManager.registerService(analysisService);
                analysisServices.add(analysisService);

            } else {
                logger.info("Skipping " + extractorClass + ". Cannot be registered to MICO platform because of missing interface AnalysisService");
            }
        }
    }

    /**
     * Unregisters all analysers and disconnects the MICO platform
     * @throws IOException
     * @throws InterruptedException
     */
    public void disconnect() throws IOException, InterruptedException {
        logger.info("Unsubscribe extractors at MICO platform");

        if(analysisServices.size() == 0) {
            logger.info("No extractors connected to MICO platform.");
        }

        for(eu.mico.platform.event.api.AnalysisService analysisService : analysisServices) {
            logger.info("Unsubscribe extractor " + analysisService.getServiceID().toString() + " at MICO platform");
            eventManager.unregisterService(analysisService);
        }

        logger.info("Disconnect MICO platform");
        if(eventManager != null) {
            eventManager.shutdown();
        }

        Thread.sleep(2000);
    }

    /**
     * Sets the MICO platform address
     * @param host MICO platform address
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the configured host address of the MICO platform
     * @return host MICO platform address
     */
    public String getHost() {
        return host;
    }

    /**
     * Setter for the spring application context
     * @param applicationContext spring application context
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * The {@link eu.mico.platform.event.api.EventManager} contains methods to (un)register {@link eu.mico.platform.event.api.AnalysisService} to the MICO platform.
     * @return eventManager already connected to the configured MICO platform
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * The {@link eu.mico.platform.persistence.api.PersistenceService} is responsible for creating, retrieving and deleting content items in the MICO platform.
     * @return persistenceService already connected to the configured MICO platform
     */
    public PersistenceService getPersistenceService() {
        return eventManager.getPersistenceService();
    }
}
