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
package eu.mico.platform.broker.webservices;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.wf.Workflow;

import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * broker service for managing camel routes
 *
 * @author Marcel Sieland
 */
@Path("/workflow")
public class WorkflowManagementService {

    private static Logger log = LoggerFactory.getLogger(WorkflowManagementService.class);

//    @PersistenceContext(unitName = "MeineJpaPU", type = PersistenceContextType.TRANSACTION)
//    EntityManager entityManager;
    private Map<String,Workflow> workflows = new HashMap<String,Workflow>();
    private int nextID = 1;

    @Context
    private ServletContext servletContext;
    
    private MICOBroker broker;

    public WorkflowManagementService(MICOBroker broker) {
        this.broker = broker;
    }

    /**
     * add a new workflow.
     *
     * @return
     */
    @POST
    @Path("/add")
    @Produces("application/json")
    public Response createItem(@QueryParam("user") String user,
            @QueryParam("workflowName") String workflowName,
            @QueryParam("route") String route,
            @QueryParam("links") String links, 
            @QueryParam("nodes") String nodes)
            throws RepositoryException, IOException {
        
        Workflow workflow = new Workflow(user, workflowName, route, links, nodes);
        persistWorkflow(workflow);

        return Response.ok(ImmutableMap.of()).build();
    }
    
    /**
     * Get all workflows for a user
     *
     * @return
     */
    @GET
    @Path("/list")
    @Produces("application/json")
    public Response listWorkflows(@QueryParam("user") String user) 
            throws RepositoryException, IOException {
        
        Workflow workflow = getWorkflowNames(user);

        return Response.ok(ImmutableMap.of()).build();
    }
    
    /**
     * return information about workflow
     *
     * @return
     */
    @GET
    @Path("/get/{id}")
    @Produces("application/json")
    public Response getWorkflow(@QueryParam("user") String user,
            @PathParam("id") String workflowId,
            @Context HttpServletRequest request) throws RepositoryException,
            IOException {
        
        Workflow workflow = loadWorkflow(workflowId);

        return Response.ok(ImmutableMap.of()).build();
    }
    

    /**
     * return status of specific workflow The returned status can be one of the
     * following:
     * 
     * The value can be: <br>
     * <b>ONLINE</b> - (all extractors registred and connected <br>
     * <b>RUNNABLE</b> - (all extractors registered, but at least one is not connected.
     * The missing extractors can still be started by the broker <br>
     * <b>UNAVAILABLE</b> - all extractors registered, but at least one is deployed <br>
     * <b>BROKEN</b> - at least one extractor is not registered anymore <br>
     *
     * @param user
     * @param workflowId
     * @return
     */
    @GET
    @Path("/status/{id}")
    @Produces("application/json")
    public Response createItem(@QueryParam("user") String user,
            @QueryParam("id") String workflowId ) throws RepositoryException,
            IOException {
        log.warn("status check is not implemented yet, simply returning 'ONLINE'");
        return Response.ok(ImmutableMap.of("status", "ONLINE")).build();
    }
    
    
    private void persistWorkflow(Workflow workflow){
//        entityManager.persist(workflow);
        workflows.put(String.valueOf(nextID++),workflow);
    }

    private Workflow loadWorkflow(String id){
//        return entityManager.find(Workflow.class, id);
        return workflows.get(id);
    }

    private Workflow getWorkflowNames(String user) {
        // TODO Auto-generated method stub
        return null;
    }


}
