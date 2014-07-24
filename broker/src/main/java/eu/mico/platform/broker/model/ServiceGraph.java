package eu.mico.platform.broker.model;

import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * Representation of a service dependency graph. The vertices are the symbolic representations of the input/output
 * types of the services- The edges are the symbolic representations of the services that require a certain input type
 * and provide a certain output type.
 *
 * The whole graph represents a state transition graph for the parts of a content item. When a new content item is
 * received by the broker, it looks in the graph for all vertices that have a matching type for each part of the content
 * item. This is the initial state of execution.
 *
 * For each state pair of the content item that has not been processed, the broker will query the graph for all outgoing
 * edges (service descriptors), calls the associated services, and then removes the pair.
 *
 * Every time a service adds a new part and notifies the broker, the broker will update the state, adding another
 * (part,state) pair to the current state.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ServiceGraph extends DefaultDirectedGraph<TypeDescriptor,ServiceDescriptor> {

    public ServiceGraph() {
        super(ServiceDescriptor.class);
    }


}
