/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import eu.mico.platform.broker.model.CamelJob;
import eu.mico.platform.broker.model.MICOCamelRoute;
import eu.mico.platform.broker.model.MICOCamelRoute.EntryPoint;
import eu.mico.platform.broker.model.MICOJob;
import eu.mico.platform.broker.model.MICOJobStatus;
import eu.mico.platform.camel.MicoCamelContext;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * A webservice for creating new  items and adding new  parts. Used by the inject.html frontend
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Path("/inject")
public class InjectionWebService {

	private static  String MICO_RDF_MIME_TYPE = "application/x-mico-rdf";
	
    private static Logger log = LoggerFactory.getLogger(InjectionWebService.class);

    private MICOBroker broker;
    private EventManager eventManager;
    private MicoCamelContext camelContext;
    private Map<String,MICOCamelRoute> camelRoutes;

    private final URI extratorID = new URIImpl("http://www.mico-project.eu/injection-webservice/");

    public InjectionWebService(MICOBroker broker, EventManager manager, MicoCamelContext camelContext, Map<String,MICOCamelRoute> camelRoutes) {
        this.broker=broker;
    	this.eventManager = manager;
        this.camelContext = camelContext;
        this.camelRoutes = camelRoutes;
    }

    /**
     * Create a new item and return its URI in the "uri" field of the JSON response.
     *
     * @return
     */
    @POST
    @Path("/create")
    @Produces("application/json")
    public Response createItem(
            @QueryParam("type") String type,
            @QueryParam("name") String name,
            @QueryParam("mimeType") String mimeType,
            @QueryParam("existingAssetLocation") String existingAssetLocation,
            @Context HttpServletRequest request
    ) throws RepositoryException, IOException {

    	PersistenceService ps = eventManager.getPersistenceService();
    	InputStream in = null;


    	try {
	    	in = new BufferedInputStream(request.getInputStream());
	    	if (mimeType == null || mimeType.trim().length() == 0){
	    	    mimeType=guessMimeType(in);
	            if(mimeType == null ){
	                mimeType=type;
	            }
	    	}
	    	int bytes = in.available();
	    	log.info("available bytes from upload: {}",bytes);
	
	    	if(bytes > 0){
	    		
	    		log.info("Creating item with new asset");
	    		Asset asset = null;
	    		if(existingAssetLocation == null || existingAssetLocation.isEmpty()){
	    			
	    			Item item = ps.createItem();
	    			asset = item.getAsset();
	
	    			OutputStream out = asset.getOutputStream();
	    			bytes = IOUtils.copy(in, out);
	    			out.close();
	    			asset.setFormat(mimeType);

                    if (name == null || name.isEmpty()) {
                        name = asset.getLocation().toString();
                    }
                    asset.setName(name);

	    			if(type == null || type.isEmpty()){
	    				type=guessSyntacticTypeFromMimeType(mimeType);
	    			}
	    			
	    			item.setSyntacticalType(type);
	    	    	item.setSemanticType("Item created by application/injection-webservice");


	
	    	    	log.info("item created {}: uploaded {} bytes", item.getURI(), bytes);
	    	    	return Response.ok(
							ImmutableMap.of(
									"itemUri", item.getURI().stringValue(),
									"assetLocation", item.getAsset().getLocation(),
									"created", item.getSerializedAt(),
									"syntacticalType", type,
									"assetName", name
							)).build();
	    		}
	    		else {
	    			log.error("Overriding the content of {} is forbidden", existingAssetLocation);
	    			throw new IllegalArgumentException("Overriding pre-existing content stored in "+existingAssetLocation+" is forbidden");
	    		}
	
	    	}
	    	else{
	    		//if the user provided an existing asset location
	        	if(existingAssetLocation != null && ! existingAssetLocation.isEmpty()){
	        		
	        		InputStream assetIS = null;
	        		try{
	        			
                        if (mimeType == null) {
                            mimeType = guessMimeTypeFromRemoteLocation(ps,
                                    existingAssetLocation);
                            if (mimeType == null) {
                                mimeType = type;
                            }
                        }
	        			
	        			Item item = ps.createItem();
	        			Asset asset = item.getAssetWithLocation(new URIImpl(existingAssetLocation));
	        			asset.setFormat(mimeType);

                        if (name == null || name.isEmpty()) {
                            name = asset.getLocation().toString();
                        }
                        asset.setName(name);


		    			if(type == null || type.isEmpty()){
		    				type=guessSyntacticTypeFromMimeType(mimeType);
		    			}
	        			
	        			item.setSyntacticalType(type);
		    	    	item.setSemanticType("Item created by application/injection-webservice from a pre-existing asset");

		    	    	
		    	    	log.info("item created {}", item.getURI());
		    	    	return Response.ok(ImmutableMap.of("itemUri", item.getURI().stringValue(), "assetLocation", item.getAsset().getLocation(), "created", item.getSerializedAt())).build();
	        			
	        		}
	        		catch( IOException | NullPointerException e) {
	        			//thrown from the persistence if the data does not exist / the url is malformed 
	        		    throw new IllegalArgumentException("No data found at "+existingAssetLocation+" for the asset of the new item", e);
	        		}
	        		finally{
	        			if(assetIS!=null){
	        				assetIS.close();
	        			}
	        		}
	        		
	        	}
	        	else{
	        		
	        		//create an item without asset
	        		Item item = ps.createItem();
	    			if(type == null || type.isEmpty()){
	    				type="mico:Item";
	    			}
        			item.setSyntacticalType(type);
	    	    	item.setSemanticType("empty item created by application/injection-webservice");
	    	    	
	    	    	log.warn("empty item created {}", item.getURI());
	    	    	return Response.ok(ImmutableMap.of("itemUri", item.getURI().stringValue(), "created", item.getSerializedAt())).build();
	    	    	
	        	}
	    	}
    	}
    	finally{
    		if(in != null){
    			in.close();
    		}
    	}
    }

    @GET
    @Path("/items")
    @Produces("application/json")
    public List<Map<String, Object>> getItems(@QueryParam("uri") String itemUri, @QueryParam("parts") boolean showParts) throws RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();

        List<Map<String, Object>> result = new ArrayList<>();
        
        if (itemUri == null) {
            // retrieve a list of all items
            for (Item item : ps.getItems()) {
                result.add(wrapItem(item, showParts));
            }
        } else if (ps.getItem(new URIImpl(itemUri)) != null) {
            result.add(wrapItem(ps.getItem(new URIImpl(itemUri)), showParts));
        } else {
            throw new NotFoundException("item with uri " + itemUri + " not found in broker");
        }
        return result;
    }

    /**
     * Submit the item for analysis by notifying the broker about its parts.
     *
     * @return
     */
    @POST
    @Path("/submit")
    @Produces("text/plain")
    public Response submitItem(
			@QueryParam("item") String itemURI,
			@QueryParam("route") String routeId,
			@QueryParam("notifyTo") String notificationURI
	) throws RepositoryException, IOException {


    	if(itemURI == null || itemURI.isEmpty()){
    		//wrong item
    		return Response.status(Response.Status.BAD_REQUEST).entity("item parameter not set").build();
    	}
    	if(routeId != null && routeId.isEmpty()){
    		//wrong routeId
    		return Response.status(Response.Status.BAD_REQUEST).entity("route parameter not set").build();
    	}
    	
    	PersistenceService ps = eventManager.getPersistenceService();
        Item item = ps.getItem(new URIImpl(itemURI));
        if(item == null){
            //wrong routeId
            return Response.status(Response.Status.BAD_REQUEST).entity("No item found with uri: " + itemURI).build();
        }
        
        if(routeId == null){


			// broker v2

        	eventManager.injectItem(item);
        	log.debug("submitted item {} to every compatible extractor", item.getURI());
        	return Response.ok("submitted item to every compatible extractor\n").build();

        }
        else{

			// broker v3
    		
    		log.debug("Retrieving CamelRoute with ID {}",routeId);
    		MICOCamelRoute route  = camelRoutes.get(routeId);
    		
    		if(route == null ){
    			//the route does not exist
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No route found with id: " + routeId).build();
    		}
    		


    		//check route status
    		WorkflowInfo routeStatus = broker.getRouteStatus(camelRoutes.get(routeId).getXmlCamelRoute());
    		WorkflowStatus status = routeStatus.getState();
            switch (status) {
            case BROKEN:

                // the requested route cannot be started or is broken
                log.error("The camel route with ID {} is currently {}",
                        routeId, status);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("The camel route with ID {" + routeId
                                + "} is currently broken").build();

            case UNAVAILABLE:

                // the requested route cannot be started or is broken
                log.error("The camel route with ID {} is currently {}",
                        routeId, status);
                return Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("The camel route with ID {" + routeId
                                + "} is currently unavailable").build();

            case RUNNABLE:

                // TODO: here we should start the required extractors
                log.warn(
                        "The camel route with ID {} is currently {}, but the auto-startup is not implemented",
                        routeId, status);
                return Response
                        .status(Response.Status.NOT_IMPLEMENTED)
                        .entity("The camel route with ID {"
                                + routeId
                                + "} is runnable, but the auto-deployment is not implemented")
                        .build();
            case ONLINE:

                log.debug(
                        "The camel route with ID {} is currently {}, looking for compatible entry points ...",
                        routeId, status);
                // the route is up and running, proceed with the injection
                boolean compatibleEpFound = false;
                MICOJobStatus jobState = new MICOJobStatus(itemURI, routeId,
                        notificationURI);

                for (EntryPoint ep : route.getEntryPoints()) {

                    boolean epCompatible = false;

                    // check if the entry point is compatible or not
                    epCompatible = epCompatible || isCompatible(item, ep);
                    if (isCompatible(item, ep)) {
                        jobState.addCamelJob(new CamelJob(itemURI, ep
                                .getDirectUri(), camelContext));
                    }

                    for (Part p : item.getParts()) {
                        epCompatible = epCompatible || isCompatible(p, ep);

                        if (isCompatible(p, ep)) {
                            jobState.addCamelJob(new CamelJob(itemURI, p
                                    .getURI().stringValue(), ep.getDirectUri(),
                                    camelContext));
                        }
                    }

                    compatibleEpFound = compatibleEpFound || epCompatible;

                }
                if (compatibleEpFound) {
                    Thread thr = new Thread(jobState);
                    thr.start();
                    broker.addMICOCamelJobStatus(new MICOJob(routeId, itemURI),
                            jobState);

                    return Response.ok(
                            "Start process item with route " + routeId).build();
                }

                log.error("Unable to retrieve an entry point compatible with the input item");
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Unable to retrieve an entry point compatible with the input item")
                        .build();
            default: {
                // status is not between the known ones
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
            }
            }
        }

        
    }


    /**
     * Add a new content part to the item using the request body as content. Return the URI of the new part in
     * the "uri" field of the JSON response.
     *
     * @return
     */
    @POST
    @Path("/add")
    @Produces("application/json")
    public Response addPart(@QueryParam("itemUri")String itemURI, @QueryParam("mimeType") String mimeType, @QueryParam("type") String type, @QueryParam("existingAssetLocation") String existingAssetLocation, @Context HttpServletRequest request) throws RepositoryException, IOException {
        PersistenceService ps = eventManager.getPersistenceService();

        if(itemURI == null || itemURI.isEmpty()){
            //wrong item
            return Response.status(Response.Status.BAD_REQUEST).entity("item parameter not set").build();
        }

        URIImpl id = null;
        try{
            id = new URIImpl(itemURI);
        }catch(IllegalArgumentException ex){
            return Response.status(Response.Status.BAD_REQUEST).entity("Not a valid item uri: " + itemURI).build();
        }
        
        Item item = ps.getItem(id);
        if(item == null){
            //item not found in system
            return Response.status(Response.Status.BAD_REQUEST).entity("No item found with uri:" + itemURI).build();
        }
        InputStream in = null;

        try {
            in = new BufferedInputStream(request.getInputStream());
            if (mimeType == null || mimeType.trim().length() == 0) {
                mimeType = guessMimeType(in);
                if (mimeType == null) {
                    mimeType = type;
                }
            }
	    	int bytes = in.available();
	    	
	
	    	if(bytes > 0){
	    		
	    		log.info("Creating part with new asset");
	    		Asset asset = null;
	    		if(existingAssetLocation == null || existingAssetLocation.isEmpty()){	    			
	    			
	    			Part part = item.createPart(extratorID);
	    			asset = part.getAsset();
	
	    			OutputStream out = asset.getOutputStream();		        
	    			bytes = IOUtils.copy(in, out);
	    			out.close();
	    			asset.setFormat(mimeType);
	    			if(type == null || type.isEmpty()){
	    				type=guessSyntacticTypeFromMimeType(mimeType);
	    			}
	    			
	    			part.setSyntacticalType(type);
	    	    	part.setSemanticType("Part created by application/injection-webservice");

					log.info("item {}, part created {} : uploaded {} bytes", item.getURI(), part.getURI(), bytes);
					return Response.ok(
							ImmutableMap.of(
									"itemUri", item.getURI().stringValue(),
									"partUri", part.getURI().stringValue(),
									"assetLocation", asset.getLocation(),
									"created", part.getSerializedAt()
							)).build();

				}
	    		else {
	    			log.error("Overriding the content of {} is forbidden", existingAssetLocation);
	    			throw new IllegalArgumentException("Overriding pre-existing content stored in "+existingAssetLocation+" is forbidden");
	    		}
	
	
	    	}
	    	else{
	    		//if the user provided an existing asset location
	        	if(existingAssetLocation != null && ! existingAssetLocation.isEmpty()){
	        		
	        		InputStream assetIS = null;
	        		try{
	        			
                        if (mimeType == null || mimeType.trim().length() == 0) {
                            mimeType = guessMimeTypeFromRemoteLocation(ps,
                                    existingAssetLocation);
                            if (mimeType == null) {
                                mimeType = type;
                            }
                        }
	        			
	        			Part part = item.createPart(extratorID);
	        			Asset asset = part.getAssetWithLocation(new URIImpl(existingAssetLocation));

	        			asset.setFormat(mimeType);	 
		    			if(type == null || type.isEmpty()){
		    				type=guessSyntacticTypeFromMimeType(mimeType);
		    			}
	        			
	        			
	        			part.setSyntacticalType(type);
		    	    	part.setSemanticType("Part created by application/injection-webservice from a pre-existing asset");
		    	    	
		    	    	log.info("item {}, part created {}", item.getURI(), part.getURI());
		    	        return Response.ok(ImmutableMap.of("itemURI", item.getURI().stringValue(),"partURI", part.getURI().stringValue(), "assetLocation", asset.getLocation(), "created", part.getSerializedAt())).build();
	        			
	        		}
	        		catch( IOException | NullPointerException e) {
	        			//thrown from the persistence if the data does not exist / the url is malformed 
	        			throw new IllegalArgumentException("No data found at "+existingAssetLocation+" for the asset of the new part");
	        		}
	        		finally{
	        			if(assetIS!=null){
	        				assetIS.close();
	        			}
	        		}
	        		
	        	}
	        	else{
	        		
	        		//trying to create a part without an asset ?!
	        		throw new IllegalArgumentException("Adding a part without an asset with this service is forbidden");
	    	    	
	        	}
	    	}
    	}
    	finally{
    		if(in != null){
    			in.close();
    		}
    	}
    }


    private Map<String, Object> wrapItem(Item item, boolean showParts) throws RepositoryException {
        Map<String, Object> sprops = new HashMap<>();
        sprops.put("uri", item.getURI().stringValue());
        sprops.put("type", item.getSyntacticalType());
        sprops.put("created", item.getSerializedAt());

        if (item.hasAsset()) {
            sprops.put("assetUri", item.getAsset().getLocation());
        }

        if (showParts) {
            List<Map<String, Object>> parts = new ArrayList<>();
            for (Part part : item.getParts()) {
                parts.add(wrapPart(part));
            }
            sprops.put("parts", parts);
        }

        return sprops;
    }

    private Map<String, Object> wrapPart(Part part) throws RepositoryException {
        Map<String, Object> sprops = new HashMap<>();
        sprops.put("uri", part.getURI().stringValue());
        sprops.put("type", part.getSyntacticalType());
        sprops.put("creator", part.getSerializedBy().toString());
        sprops.put("created", part.getSerializedAt());

        if (part.hasAsset()) {
            sprops.put("source", part.getAsset().getLocation());
        }
        return sprops;
    }

    @GET
    @Path("/download")
    public Response downloadPart(@QueryParam("itemUri") String itemUri, @QueryParam("partUri") String partUri) throws RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();

        final Item item = ps.getItem(new URIImpl(itemUri));

        if (item == null) {
            throw new NotFoundException("Item with URI " + itemUri + " not found in system");
        }

        StreamingOutput entity;
        String type;
        if (partUri == null) {
            entity = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        IOUtils.copy(item.getAsset().getInputStream(), output);
                    } catch (RepositoryException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };

            type = item.getAsset().getFormat();
        } else {
            final Part part = item.getPart(new URIImpl(partUri));
            if (part == null) {
                throw new NotFoundException("Part with URI " + partUri + " not found in system");
            }
            if (part.hasAsset() == false) {
                throw new NotFoundException("Part with URI " + partUri + " has no binary asset");
            }

            entity = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        IOUtils.copy(part.getAsset().getInputStream(), output);
                    } catch (RepositoryException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };

            type = part.getAsset().getFormat();
        }

        try{
            MediaType.valueOf(type);
        }catch(IllegalArgumentException e){
            log.info("deliver unknown mediaType [{}] - set type to {}", type,
                    MediaType.APPLICATION_OCTET_STREAM);
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        
        return Response.ok(entity, type).build();
    }
    
    private String guessMimeTypeFromRemoteLocation(PersistenceService ps, String existingAssetLocation) throws IOException, RepositoryException{
		Item tmpItem=ps.createItem();
		InputStream assetIS = null;
		String mimeType = null;
		try{
			Asset tmpItemAsset=tmpItem.getAssetWithLocation(new URIImpl(existingAssetLocation));	        					
			
			assetIS = tmpItemAsset.getInputStream();
			mimeType = guessMimeType(assetIS);	        			
					
			//further check on data having few bytes inside
			if(assetIS.available() == 0){
				throw new IllegalArgumentException("No data found at "+existingAssetLocation+" for the asset of the new resource");
			}
		}
		finally{
			if(assetIS != null){
				assetIS.close();
			}
			ps.deleteItem(tmpItem.getURI());
		}
		return mimeType;
    }
    private String guessMimeType(InputStream in) throws IOException{
    	final Tika tika = new Tika();
        return tika.detect(in);
    }
    private String guessSyntacticTypeFromMimeType(String mimeType){
    	if(mimeType == null || mimeType.isEmpty() || mimeType.length()<2){
    		return null;
    	}
    	String out=null;
    	String[] tokens = mimeType.split("/");
    	if(tokens.length == 2){
    		out="mico:" + tokens[0].substring(0, 1).toUpperCase()+tokens[0].substring(1);
    	}
    	return out;
    }

    private boolean isCompatible(Resource r, EntryPoint ep) throws RepositoryException{
    	//becomes false if mimetype or syntactic type doesn't match
    	boolean isCompatible = true;
    	
    	if( ! ep.getMimeType().contentEquals(MICO_RDF_MIME_TYPE) ){
    		isCompatible = isCompatible &&
    				       r.hasAsset() &&
    				       r.getAsset().getFormat().contentEquals(ep.getMimeType());
    	}
    	isCompatible = isCompatible &&
    			       ep.getSyntacticType().contentEquals(r.getSyntacticalType());
    	
    	return isCompatible;
    }
}
