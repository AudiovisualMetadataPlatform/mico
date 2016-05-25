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
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.ArrayList;
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

//    @PersistenceContext(unitName = "inMemoryPersistenceUnit", type = PersistenceContextType.TRANSACTION)
    private EntityManagerFactory emf;
    private EntityManager em;

    
    private int nextID = 1;

    @Context
    private ServletContext servletContext;
    
    private MICOBroker broker;

    public WorkflowManagementService(MICOBroker broker) {
        this.broker = broker;
    	this.emf = Persistence.createEntityManagerFactory("inMemoryPersistenceUnit");
    	this.em = emf.createEntityManager();
    }
    
    //------------------- public REST API

    /**
     * add a new workflow.
     *
     * @return
     */
    @POST
    @Path("/add")
    @Produces("application/json")
    public Response createItem(@FormParam("user") String user,
    		@FormParam("workflowName") String workflowName,
    		@FormParam("route") String route,
    		@FormParam("links") String links, 
    		@FormParam("nodes") String nodes)
            throws RepositoryException, IOException {
        
    	log.info("Persisting new workflow with name {} for user {}",workflowName,user);
    	
        Workflow workflow = new Workflow(user, workflowName, route, links, nodes);
        persistWorkflow(workflow);
        log.info("Persisted new workflow {} belonging to user {}",workflow.toString(),user);

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
    	log.info("Retrieving list of workflows ids for user {}",user);
    	List<String> WorkflowIds = getUserWorkflowIds(user);        
    	return Response.ok(WorkflowIds).build();
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
    @Produces("text/plain")
    public Response createItem(@QueryParam("user") String user,
            @QueryParam("id") String workflowId ) throws RepositoryException,
            IOException {
        log.warn("status check is not implemented yet, simply returning 'ONLINE'");
        String status="ONLINE";
        return Response.ok(status).build();
    }
    
    
    //------------------- private methods handling the persistence backend
    
    private void persistWorkflow(Workflow workflow){
    	em.getTransaction().begin();
        em.persist(workflow);
        em.flush();
        em.getTransaction().commit();
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserWorkflowIds(String user) {
    	Query queryAllIDs = em.createNamedQuery(Workflow.QUERY_WORKFLOW_IDS_BY_USER);
    	queryAllIDs.setParameter("user", user);
    	List<Integer> res = queryAllIDs.getResultList();
    	log.info("Retrieved {} workflow IDs for user {}",res.size(),user);
    	List<String> out=new ArrayList<String>();
    	for(Integer id : res){
    		out.add(id.toString());
    	}
    	return out;
    }


}
