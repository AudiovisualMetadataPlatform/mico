package eu.mico.platform.event.test.mock;

import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.test.mock.PersistenceServiceMock;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple Event Manager Mock for testing AnalysisService implementations
 *
 * @author Sergio Fernández
 */
public class EventManagerMock implements EventManager {

    private static Logger log = LoggerFactory.getLogger(EventManagerMock.class);

    private Set<AnalysisService> services;
    private PersistenceServiceMock persistenceService;
    private AnalysisResponseCollector responsesCollector;

    @Override
    public void registerService(AnalysisService service) throws IOException {
        services.add(service);
    }

    @Override
    public void unregisterService(AnalysisService service) throws IOException {
        services.remove(service);
    }

    @Override
    public void injectContentItem(ContentItem item) throws IOException {
        try {
            log.debug("Injecting content item {}...", item.getURI());
            for (Content content: item.listContentParts()) {
                for (AnalysisService service: services) {
                    if (service.getRequires().equals(content.getType())) {
                        try {
                            log.debug("calling service {} to analyze {}...", service.getServiceID(), content.getURI());
                            service.call(responsesCollector, item, content.getURI());
                        } catch (AnalysisException e) {
                            log.error("Analysis Exception processing {}: {}", content.getURI().stringValue(), e.getMessage());
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Repository Exception: {}", e.getMessage(), e);
        }
    }

    @Override
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public AnalysisResponseCollector getResponsesCollector() {
        return responsesCollector;
    }

    @Override
    public void init() throws IOException {
        services = new HashSet<>();
        persistenceService = new PersistenceServiceMock();
        responsesCollector = new AnalysisResponseCollector();
    }

    @Override
    public void shutdown() throws IOException {
        services = null;
        persistenceService = null;
        responsesCollector = null;
    }

}
