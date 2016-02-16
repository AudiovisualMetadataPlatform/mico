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
package eu.mico.platform.broker.model;

import eu.mico.platform.broker.exception.StateNotFoundException;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represent the current processing states of a content item
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ItemState {

    private static Logger log = LoggerFactory.getLogger(ItemState.class);

    private Item item;
    private Date        created;

    private Map<URI, TypeDescriptor> states;   // contains the currently non-processed states
    private Map<String,Transition>   progress; // contains the correlation IDs currently in progress, used to indicate when we are finished

    private ServiceGraph             graph;

    public ItemState(ServiceGraph graph, Item item) {
        this.graph       = graph;
        this.item = item;

        this.states = new HashMap<>();
        this.progress = new HashMap<>();
        this.created  = new Date();

        initState();
    }


    /**
     * Create the initial states for this content item. For each content part, we look up the vertex in the
     * dependency graph that matches the type, and add an appropriate entry into the states table.
     */
    private void initState() {
        try {
            for(Part part : item.getParts()) {
                try {
                    states.put(part.getURI(), graph.getState(part.getType()));
                } catch (StateNotFoundException e) {
                    log.warn("no starting state found for part type {}", part.getType());
                }
            }
        } catch (RepositoryException e) {
            log.error("could not access content item information for content item {} (error: {})", item.getURI(), e.getMessage());
            log.debug("Exception:",e);
        }
    }

    /**
     * Return true in case all parts have reached a final states, i.e. a states with no further outgoing service transitions.
     * @return
     */
    public boolean isFinalState() {
        if(!progress.isEmpty()) {
            return false;
        }

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
                Transition t = new Transition(item, state.getKey(), state.getValue(), graph.getEdgeTarget(svc), svc);
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


    /**
     * Indicate that the transaction with the given ID is currently still in progress.
     *
     * @param correlationId
     */
    public void addProgress(String correlationId, Transition t) {
        progress.put(correlationId, t);
    }

    /**
     * Indicate that the transaction with the given ID has been finished.
     *
     * @param correlationId
     */
    public void removeProgress(String correlationId) {
        progress.remove(correlationId);
    }


    public Map<String, Transition> getProgress() {
        return progress;
    }

    public Map<URI, TypeDescriptor> getStates() {
        return states;
    }

    public Date getCreated() {
        return created;
    }
}
