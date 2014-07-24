package eu.mico.platform.broker.model;

import eu.mico.platform.broker.exception.StateNotFoundException;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represent the current processing states of a content item
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContentItemState {

    private static Logger log = LoggerFactory.getLogger(ContentItemState.class);

    private ContentItem contentItem;

    private Map<URI, TypeDescriptor> states;
    private ServiceGraph             graph;

    public ContentItemState(ServiceGraph graph, ContentItem contentItem) {
        this.graph       = graph;
        this.contentItem = contentItem;

        this.states = new HashMap<>();

        initState();
    }


    /**
     * Create the initial states for this content item. For each content part, we look up the vertex in the
     * dependency graph that matches the type, and add an appropriate entry into the states table.
     */
    private void initState() {
        try {
            for(Content content : contentItem.listContentParts()) {
                try {
                    states.put(content.getURI(), graph.getState(content.getType()));
                } catch (StateNotFoundException e) {
                    log.warn("no starting state found for content type {}", content.getType());
                }
            }
        } catch (RepositoryException e) {
            log.error("could not access content item information for content item {} (error: {})", contentItem.getURI(), e.getMessage());
            log.debug("Exception:",e);
        }
    }

    /**
     * Return true in case all parts have reached a final states, i.e. a states with no further outgoing service transitions.
     * @return
     */
    public boolean isFinalState() {
        for(TypeDescriptor d : states.values()) {
            if(graph.outDegreeOf(d) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return a collection of all transitions that are currently possible given the states of content item parts. Returns
     * an empty set in case all states are final.
     * @return
     */
    public Set<Transition> getPossibleTransitions() {
        Set<Transition> result = new HashSet<>();
        for(Map.Entry<URI,TypeDescriptor> state : states.entrySet()) {
            for(ServiceDescriptor svc : graph.outgoingEdgesOf(state.getValue())) {
                Transition t = new Transition(contentItem, state.getKey(), state.getValue(), graph.getEdgeTarget(svc), svc);
                result.add(t);
            }
        }

        return result;
    }


    /**
     * Add or update the state for a given content item part (e.g. because a new part has been added or the part has
     * moved in a transition).
     *
     * @param part
     * @param state
     */
    public void addState(URI part, TypeDescriptor state) {
        states.put(part,state);
    }

    /**
     * Remove the state for the given content item part (e.g. because the part has been consumed)
     *
     * @param part
     */
    public void removeState(URI part) {
        states.remove(part);
    }
}
