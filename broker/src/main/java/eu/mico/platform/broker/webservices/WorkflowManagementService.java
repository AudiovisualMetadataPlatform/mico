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
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.model.wf.Workflow;
import eu.mico.platform.camel.MicoCamelContext;

import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.ArrayList;
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

    
    @Context
    private ServletContext servletContext;
    
    private MICOBroker broker;

    private MicoCamelContext camelContext;
    private Map<Integer,MICOCamelRoute> camelRoutes;

    public WorkflowManagementService(MICOBroker broker, MicoCamelContext camelContext, Map<Integer,MICOCamelRoute> camelRoutes) {
        this.broker = broker;
        this.camelContext = camelContext;
        this.camelRoutes = camelRoutes;
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
    public int addWorkflow(@FormParam("user") String user,
    		@FormParam("workflowName") String workflowName,
    		@FormParam("route") String route,
    		@FormParam("links") String links, 
    		@FormParam("nodes") String nodes)
            throws RepositoryException, IOException {
        
    	log.info("Persisting new workflow with name {} for user {}",workflowName,user);
    	
    	//add to database
        Workflow workflow = new Workflow(user, workflowName, route, links, nodes);
        persistWorkflow(workflow);
        
        
        //add to memory
        String xmlCamelRoute = new String(workflow.getRoute());
        xmlCamelRoute = xmlCamelRoute.replaceAll("WORKFLOW_ID", workflow.getId().toString());
        
        camelContext.addRouteToContext(xmlCamelRoute);
        camelRoutes.put(workflow.getId(),new MICOCamelRoute().parseCamelRoute(xmlCamelRoute));
        
        log.info("Persisted new workflow {} belonging to user {}",workflow.toString(),user);

        return workflow.getId().intValue();
    }

    //TODO: with @delete the ui gets a 403 forbidden error, and this method is not triggered. check why
    @POST
    @Path("/del/{id}")
    @Produces("application/json")
    public Response deleteWorkflow(@PathParam("id") Integer workflowId ) throws RepositoryException,
            IOException {
    	log.info("Removing workflow with ID {}",workflowId);

    	//delete from database
    	deleteWorkflow(getWorkflow(workflowId));
    	
    	//delete from memory
        String xmlRoute=getCamelRoute(workflowId);        
    	camelContext.removeRouteFromContext(xmlRoute);
        camelRoutes.remove(workflowId);        
        
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
    @Deprecated
    public List<String> listWorkflows(@QueryParam("user") String user) 
            throws RepositoryException, IOException {
    	log.info("Retrieving list of workflows ids for user {}",user);
    	List<String> WorkflowIds = getWorkflowIdsForUser(user);        
    	return WorkflowIds;
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
    public String getStatus(@QueryParam("user") String user,
            @PathParam("id") Integer workflowId ) throws RepositoryException,
            IOException {
        
    	
    	String status="BROKEN";
    	try{
	        if (broker instanceof MICOBrokerImpl ){
	        	String xmlCamelRoute=new String(getCamelRoute(workflowId));
	        	status =  broker.getRouteStatus(xmlCamelRoute);
	        }
        }
    	catch(Exception e){
    		log.error("Unable to retrieve status for workflow {}",workflowId);
    	}
        return status;
    }
    
    @GET
    @Path("/ui-params/{id}")
    @Produces("application/json")
    @Deprecated
    public Response getUIParams(@PathParam("id") Integer workflowId ) throws RepositoryException,
            IOException {
    	log.info("Retrieving UI Parameters for workflow with ID {}",workflowId);
        Workflow w=getWorkflow(workflowId);
        
        return Response.ok(ImmutableMap.of("workflowName",w.getName(), "nodes",w.getNodes(), "links",w.getLinks())).build();
    }
    
    @GET
    @Path("/camel-route/{id}")
    @Produces("text/plain")
    public String getCamelRoute(@PathParam("id") Integer workflowId ) throws RepositoryException,
            IOException {
    	log.info("Retrieving CamelRoute for workflow with ID {}",workflowId);
    	
    	//retrieve from memory
    	return camelRoutes.get(workflowId).getXmlCamelRoute();
    }
    
    
    //------------------- private methods handling the persistence backend
    
    @Deprecated
    private void persistWorkflow(Workflow workflow){
    	em.getTransaction().begin();
        em.persist(workflow);
        em.flush();
        em.getTransaction().commit();
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    private List<String> getWorkflowIdsForUser(String user) {
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
    
    @Deprecated
    private Workflow getWorkflow(Integer wId) {
    	
    	TypedQuery<Workflow> querySingleWorkflow = em.createNamedQuery(Workflow.QUERY_SINGLE_WORKFLOW_BY_ID, Workflow.class);
    	querySingleWorkflow.setParameter("id", wId);
    	return querySingleWorkflow.getSingleResult();
    }
    
    @Deprecated
    private void deleteWorkflow(Workflow workflow) {
    	
    	em.getTransaction().begin();
    	em.remove(workflow);
    	em.flush();
    	em.getTransaction().commit();
    }


}
