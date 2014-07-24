package eu.mico.platform.broker.api;

import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.persistence.api.PersistenceService;

import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface MICOBroker {
    ServiceGraph getDependencies();

    Map<String, ContentItemState> getStates();

    PersistenceService getPersistenceService();
}
