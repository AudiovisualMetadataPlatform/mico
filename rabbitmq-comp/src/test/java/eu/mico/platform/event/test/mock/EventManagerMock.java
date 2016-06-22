package eu.mico.platform.event.test.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.anno4j.Transaction;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.AnalysisServiceAnno4j;
import eu.mico.platform.event.api.AnalysisServiceBase;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

/**
 * Simple Event Manager Mock for testing AnalysisService implementations
 *
 * @author Sergio Fernández
 */
public class EventManagerMock implements EventManager {

    private static Logger log = LoggerFactory.getLogger(EventManagerMock.class);

    private Set<AnalysisServiceBase> services;
    private PersistenceServiceAnno4j persistenceService;
    private AnalysisResponseCollector responsesCollector;

    @Override
    public void registerService(AnalysisServiceBase service) throws IOException {
        services.add(service);
    }

    @Override
    public void unregisterService(AnalysisServiceBase service) throws IOException {
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

    private void analyseItemResource(Item item, Resource resource, Map<String, String> params) throws IOException {
        for (AnalysisServiceBase service: services) {
            if (service.getRequires().equals(resource.getSyntacticalType())) {
                List<Resource> parts = new LinkedList<Resource>();
                parts.add(resource);
                log.debug("calling service {} to analyze {}...", service.getServiceID(), resource.getURI());
                boolean success = false;
                try {
                    if (service instanceof AnalysisServiceAnno4j) {
                        Transaction transaction = persistenceService.getAnno4j().adoptTransaction(item.getRDFObject().getObjectConnection());
                        transaction.begin();
                        ((AnalysisServiceAnno4j)service).call(responsesCollector, item, parts, params, transaction);
                    } else {
                        item.getObjectConnection().begin();
                        ((AnalysisService)service).call(responsesCollector, item, parts, params);
                    }
                    success = true;
                } catch (RepositoryException | AnalysisException | RuntimeException e) {
                    responsesCollector.sendError(item, ErrorCodes.UNEXPECTED_ERROR, e.getClass().getName() +": "+e.getMessage(),"");
                } finally {
                    try {
                        if(item.getObjectConnection().isActive()){
                            if(success){
                                item.getObjectConnection().commit();
                            } else {
                                item.getObjectConnection().rollback();
                            }
                        }
                    } catch(RepositoryException e) {/* ignore */}
                    //NOTE: do not close here as in tests one might want to call
                    //      methods on this Item for assertions
                    //item.getObjectConnection().close();
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
