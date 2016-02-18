package eu.mico.platform.event.test.mock;

import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.test.mock.PersistenceServiceMock;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
    public void injectItem(Item item) throws IOException {
        try {
        	Map<String, String> params = new HashMap<String, String>();
            log.debug("Injecting content item {}...", item.getURI());
            for (Part part : item.getParts()) {
                for (AnalysisService service: services) {
                    if (service.getRequires().equals(part.getSyntacticalType())) {
                        URI uri = item.getURI();
                        List<URI> uris = new LinkedList<URI>();
                        uris.add(uri);
                        try {
                            log.debug("calling service {} to analyze {}...", service.getServiceID(), part.getURI());
                            service.call(responsesCollector, item, uris, params);
                        } catch (AnalysisException e) {
                            log.error("Analysis Exception processing {}: {}", uri.stringValue(), e.getMessage());
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
