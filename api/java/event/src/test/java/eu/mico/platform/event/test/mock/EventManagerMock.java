package eu.mico.platform.event.test.mock;

import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.AnalysisServiceAnno4j;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Resource;
import eu.mico.platform.persistence.model.Item;

import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple Event Manager Mock for testing AnalysisService implementations
 *
 * @author Sergio Fernández
 */
public class EventManagerMock implements EventManager {

    private static Logger log = LoggerFactory.getLogger(EventManagerMock.class);

    private Set<AnalysisService> services;
    private PersistenceService persistenceService;
    private AnalysisResponseCollector responsesCollector;

    @Override
    public void registerService(AnalysisService service) throws IOException {
        if(service instanceof AnalysisServiceAnno4j){
            ((AnalysisServiceAnno4j)service).setAnno4j(persistenceService.getAnno4j());
        }
        services.add(service);
    }

    @Override
    public void unregisterService(AnalysisService service) throws IOException {
        if(service instanceof AnalysisServiceAnno4j){
            ((AnalysisServiceAnno4j)service).setAnno4j(null);
        }
        services.remove(service);
    }

    @Override
    public void injectItem(Item item) throws IOException {
        try {
        	Map<String, String> params = new HashMap<String, String>();
            log.debug("Injecting content item {}...", item.getURI());
            analyseItemResource(item, item, params);
            for (Resource part : item.getParts()) {
                analyseItemResource(item, part, params);
            }
        } catch (RepositoryException e) {
            log.error("Repository Exception: {}", e.getMessage(), e);
        }
    }

    private void analyseItemResource(Item item, Resource resource,
            Map<String, String> params) throws RepositoryException, IOException {
        for (AnalysisService service: services) {
            if (service.getRequires().equals(resource.getSyntacticalType())) {
                List<Resource> parts = new LinkedList<Resource>();
                parts.add(resource);
                try {
                    log.debug("calling service {} to analyze {}...", service.getServiceID(), resource.getURI());
                    service.call(responsesCollector, item, parts, params);
                } catch (AnalysisException e) {
                    log.error("Analysis Exception processing {}: {}", resource, e.getMessage());
                }
            }
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
    public void init() throws IOException, URISyntaxException {
        services = new HashSet<>();
        persistenceService = new PersistenceServiceAnno4j();
        responsesCollector = new AnalysisResponseCollector();
    }

    @Override
    public void shutdown() throws IOException {
        services = null;
        persistenceService = null;
        responsesCollector = null;
    }

}
