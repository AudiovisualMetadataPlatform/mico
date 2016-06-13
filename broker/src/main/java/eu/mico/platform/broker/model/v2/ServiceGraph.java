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
package eu.mico.platform.broker.model.v2;

import eu.mico.platform.broker.exception.StateNotFoundException;
import org.jgrapht.graph.DirectedPseudograph;
import org.openrdf.model.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class ServiceGraph extends DirectedPseudograph<TypeDescriptor,ServiceDescriptor> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5827400620881291109L;
	private Map<URI,ServiceDescriptor> services;

    public ServiceGraph() {
        super(ServiceDescriptor.class);
        services = new HashMap<>();
    }

    public Set<URI> getDescriptorURIs() {
        return services.keySet();
    }
    
    public List<ServiceDescriptor> getServices() {
        return new ArrayList<ServiceDescriptor>(services.values());
    }


    public TypeDescriptor getState(String type) throws StateNotFoundException {
        TypeDescriptor d = new TypeDescriptor(type);
        if(containsVertex(d)) {
            return d;
        } else {
            throw new StateNotFoundException("the state with type '"+type+"' does not exist in the graph");
        }
    }

    public TypeDescriptor getTargetState(URI serviceId) throws StateNotFoundException {
        ServiceDescriptor svc = services.get(serviceId);
        if(svc != null) {
            return getEdgeTarget(svc);
        } else {
            throw new StateNotFoundException("the service with ID " + serviceId.stringValue() + " is not registered!");
        }
    }


    @Override
    public boolean addEdge(TypeDescriptor sourceVertex, TypeDescriptor targetVertex, ServiceDescriptor serviceDescriptor) {
        services.put(serviceDescriptor.getUri(), serviceDescriptor);
        addVertex(sourceVertex);
        addVertex(targetVertex);
        return super.addEdge(sourceVertex, targetVertex, serviceDescriptor);
    }

    @Override
    public boolean removeEdge(ServiceDescriptor serviceDescriptor) {
    	//get source and target edges
    	TypeDescriptor sourceVertex = super.getEdgeSource(serviceDescriptor);
    	TypeDescriptor targetVertex = super.getEdgeTarget(serviceDescriptor);
    	
        services.remove(serviceDescriptor.getUri());
        boolean result=super.removeEdge(serviceDescriptor);
        if(result){
        	//if the edge existed
        	
        	//if the source vertex is unnecessary, remove it
        	if(super.inDegreeOf(sourceVertex)<1 && super.outDegreeOf(sourceVertex)<1){
        		super.removeVertex(sourceVertex);
        	}
        	
        	//same for the target vertex
        	if(super.inDegreeOf(targetVertex)<1 && super.outDegreeOf(targetVertex)<1){
        		super.removeVertex(targetVertex);
        	}
        }        
        return result;
    }
}
