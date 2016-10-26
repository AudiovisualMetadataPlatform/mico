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

import com.google.common.collect.ImmutableMap;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.api.MICOBroker.WorkflowStatus;
import eu.mico.platform.broker.api.rest.WorkflowInfo;
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.camel.MicoCamelContext;

import org.codehaus.plexus.util.FileUtils;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.jsondoc.core.annotation.ApiVersion;
import org.jsondoc.core.pojo.ApiStage;
import org.jsondoc.core.pojo.ApiVerb;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * broker service for managing camel routes
 *
 * @author Marcel Sieland
 */
@Api(name = "workflow services", description = "Methods for workflow management", group = "broker")
@ApiVersion(since = "3.0")
@Path("/workflow")
public class WorkflowManagementService {

    private static Logger log = LoggerFactory.getLogger(WorkflowManagementService.class);
    private static Integer newID = -1;
    private static final String DEFAULT_ROUTES_PATH = "/usr/share/mico/camel-routes/";
    

    
    @Context
    private ServletContext servletContext;
    
    private MICOBroker broker;

    private MicoCamelContext camelContext;
    private Map<String,MICOCamelRoute> camelRoutes;
    private static Object camelLock = new Object();

    public WorkflowManagementService(MICOBroker broker, MicoCamelContext camelContext, Map<String,MICOCamelRoute> camelRoutes) {
        this.broker = broker;
        this.camelContext = camelContext;
        this.camelRoutes = camelRoutes;
    }
    
    //------------------- public REST API

    /**
     * add a new workflow.
     *
     * @return
     */
    @ApiMethod(verb = ApiVerb.POST,
            path = "/workflow/add",
            description = "add all routes of a new workflow",
            produces = { MediaType.APPLICATION_JSON},
            consumes = { MediaType.APPLICATION_JSON}
    )
    @POST
    @Path("/add")
    @Produces("application/json")
    public String addWorkflow(@FormParam("user") String user,
    					   @FormParam("route") String route)
            throws RepositoryException, IOException {
        
    	synchronized (camelLock) {
    		//add to memory
    		
    		//1. verify that the route is correct
    		MICOCamelRoute newRoute = new MICOCamelRoute().parseCamelRoute(route);
    		if (newRoute.getWorkflowId() == null ||
    			newRoute.getWorkflowId().isEmpty() ||
    			newRoute.getEntryPoints().size() == 0 ||
    			newRoute.getExtractorConfigurations().size() == 0){
    			throw new IllegalArgumentException("The input route cannot be parsed correctly, aborting");
    		}

            String newId = newRoute.getWorkflowId();
            
            MICOCamelRoute oldRoute = camelRoutes.get(newId);
            if(oldRoute!=null){
            	log.warn("Replacing existing route with id {}",newId);
            	camelContext.removeRouteFromContext(oldRoute.getXmlCamelRoute());
            }
            
            camelContext.addRouteToContext(route);
            camelRoutes.put(newId,newRoute);            
            log.info("Persisted new workflow with ID {} belonging to user {}",newId,user);

            return newId;
		}
        
    }
    @ApiMethod(verb = ApiVerb.POST,
            path = "/workflow/del/{id}",
            description = "remove workflow with given id",
            produces = { MediaType.APPLICATION_JSON}
    )
    //TODO: with @delete the ui gets a 403 forbidden error, and this method is not triggered. check why
    @POST
    @Path("/del/{id}")
    @Produces("application/json")
    public Response deleteWorkflow(@PathParam("id") String workflowId ) throws RepositoryException,
            IOException {
    	
    	synchronized (camelLock) {
    		
	    	log.info("Removing workflow with ID {}",workflowId);
	    	//delete from memory
	        String xmlRoute=getCamelRoute(workflowId);   
	        if(xmlRoute != null){
		    	camelContext.removeRouteFromContext(xmlRoute);
		        camelRoutes.remove(workflowId);
		        return Response.ok(ImmutableMap.of()).build();
	        }
	        return Response.status(Response.Status.NOT_FOUND).build();
        
    	}
    }

    @ApiMethod(
            path = "/workflow/routes",
            description = "returns a list with route ids and their description",
            produces = { MediaType.APPLICATION_JSON}
    )
    @GET
	@Path("/routes")
	@Produces("application/json")
	public Response getWorkflows() throws RepositoryException,
			IOException {

		synchronized (camelLock) {

			ImmutableMap.Builder<String, String> responseMap = ImmutableMap.builder();

			for (String key : camelRoutes.keySet()) {
				responseMap.put(key, camelRoutes.get(key).getWorkflowDescription());
			}

			return Response.ok(
					responseMap.build()
			).build();

		}

	}

    @ApiMethod(
            path = "/workflow/routesInfo",
            description = "returns a list with all routes and their current status",
            produces = { MediaType.APPLICATION_JSON}
    )
    @GET
    @Path("/routesInfo")
    @Produces("application/json")
    public @ApiResponseObject List<WorkflowInfo> getWorkflowInfos() throws RepositoryException,
            IOException {

        synchronized (camelLock) {

            ArrayList<WorkflowInfo> response = new ArrayList<WorkflowInfo>();

            for (String key : camelRoutes.keySet()) {
                response.add(getStatusInfo(null, camelRoutes.get(key)
                        .getWorkflowId()));
            }

            response.sort(new Comparator<WorkflowInfo>() {

                @Override
                public int compare(WorkflowInfo o1, WorkflowInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return response;

        }

    }

    /**
     * return status of specific workflow. The returned status can be one of the
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
    @ApiMethod(
            path = "/workflow/status/{id}",
            description = "return status of specific workflow.",
            produces = { MediaType.TEXT_PLAIN},
            stage = ApiStage.DEPRECATED
    )
    @GET
    @Path("/status/{id}")
    @Produces("text/plain")
    @Deprecated()
    public String getStatus(@QueryParam("user") String user,
            @ApiPathParam(name = "id", description = "id of the workflow")
            @PathParam("id") String workflowId ) throws RepositoryException,
            IOException {
        
    	synchronized (camelLock) {
	    		
	    	String status="BROKEN";
	    	try{
		        if (broker instanceof MICOBrokerImpl){
		        	String xmlCamelRoute=getCamelRoute(workflowId);
		        	if(xmlCamelRoute != null){
		        		WorkflowInfo wfi = broker.getRouteStatus(xmlCamelRoute);
		        		log.debug("status of route {}", wfi.toString());
                        status =  wfi.getState().name();
		        	}
		        	else{
		        		log.error("No route with id {} is currently registered",workflowId);
		        	}
		        }
	        }
	    	catch(Exception e){
	    		log.error("Unable to retrieve status for workflow {}:{}",workflowId,e.getMessage());
	    	}
	    	
	        return status;
    	}
    }
    
    @ApiMethod(
            path = "/workflow/statusInfo/{id}",
            description = "return extended information about status of specific workflow.",
            produces = { MediaType.APPLICATION_JSON}
    )
    @GET
    @Path("/statusInfo/{id}")
    @Produces("application/json")
    public @ApiResponseObject WorkflowInfo getStatusInfo(@QueryParam("user") String user,
            @ApiPathParam(name = "id", description = "id of the workflow")
            @PathParam("id") String workflowId ) throws RepositoryException,
            IOException {
        
        synchronized (camelLock) {
                
            try{
                if (broker instanceof MICOBrokerImpl){
                    String xmlCamelRoute=getCamelRoute(workflowId);
                    if(xmlCamelRoute != null){
                        WorkflowInfo wfi = broker.getRouteStatus(xmlCamelRoute);
                        log.debug("status of route {}", wfi.toString());
                        return  wfi;
                    }
                    else{
                        log.error("No route with id {} is currently registered",workflowId);
                    }
                }
            }
            catch(Exception e){
                log.error("Unable to retrieve status for workflow {}:{}",workflowId,e.getMessage());
            }
        }
        return new WorkflowInfo(WorkflowStatus.BROKEN,workflowId,"---");
    }

    @ApiMethod(
            path = "/workflow/camel-route/{id}",
            description = "return xml representation of specific workflow.",
            produces = { MediaType.TEXT_XML}
    )
    @GET
    @Path("/camel-route/{id}")
    @Produces("text/plain")
    public String getCamelRoute(@PathParam("id") String workflowId ) throws RepositoryException,
            IOException {
    	log.info("Retrieving CamelRoute for workflow with ID {}",workflowId);
    	
    	synchronized (camelLock) {
    		
	    	//retrieve from memory
	    	MICOCamelRoute route = camelRoutes.get(workflowId);
	    	if(route != null){
	    		return route.getXmlCamelRoute();
	    	}
	    	return null ;
	    	
    	}
    }


    public WorkflowManagementService loadCamelRoutesFrom(String resourceDirectory){

    	try {

        String resourcePath = WorkflowManagementService.class.getClassLoader().getResource(resourceDirectory).getPath();
        List<File> routes = null;
        // first try to load from mico-default-routes package (/usr/share/mico/camel-routes)
        try{
          File routesDir = new File(DEFAULT_ROUTES_PATH);
          if (routesDir.exists()){
            routes = FileUtils.getFiles(routesDir, "*.xml", "");
          }
        }catch(IOException e){
          log.warn("Unable to load default-routes: " + e.getMessage());
        }
        if (routes == null || routes.size() == 0) {
          // load packaged routes, if no default-routes are installed
          routes = FileUtils.getFiles(new File(resourcePath), "*.xml", "");
        }
        log.info("routes is {}", routes);
        log.info("Found {} files", routes.size());

    		for(File r :routes ){
    			if( ! r.isDirectory()){
    				
    				log.debug("Adding file {} to the camel context ... ",r.getName());
    				
    				try{
    					addWorkflow("mico", new String(Files.readAllBytes(r.toPath())));
    				}catch(IllegalArgumentException | IOException | RepositoryException e){
    					log.warn("Unable to add route [{}]",r.getName(),e);
    				}
    			}				
    		}
    	} catch (Exception e) {
    		log.error("Unable to load predefined camel routes",e);
    	}
    	
    	return this;
    }
}
