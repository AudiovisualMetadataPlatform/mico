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
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
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

    private static Logger log = LoggerFactory.getLogger(InjectionWebService.class);

    private EventManager eventManager;

    private final URI extratorID = new URIImpl("http://www.mico-project.eu/injection-webservice/");

    public InjectionWebService(EventManager manager) {
        this.eventManager = manager;
    }

    /**
     * Create a new item and return its URI in the "uri" field of the JSON response.
     *
     * @return
     */
    @POST
    @Path("/create")
    @Produces("application/json")
    public Response createItem(@QueryParam("type") String type, @QueryParam("existingAssetLocation") String existingAssetLocation, @Context HttpServletRequest request) throws RepositoryException, IOException {

    	PersistenceService ps = eventManager.getPersistenceService();
    	InputStream in = null;
    	
    	try {
	    	in = new BufferedInputStream(request.getInputStream());
			String mimeType=guessMimeType(in);    	
			if(mimeType == null ){
				mimeType=type;
			}
	    	int bytes = in.available();
	    	
	
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
	    			if(type == null || type.isEmpty()){
	    				type=guessSyntacticTypeFromMimeType(mimeType);
	    			}
	    			
	    			item.setSyntacticalType(type);
	    	    	item.setSemanticType("Item created by application/injection-webservice");
	
	    	    	log.info("item created {}: uploaded {} bytes", item.getURI(), bytes);
	    	    	return Response.ok(ImmutableMap.of("itemUri", item.getURI().stringValue(), "assetLocation", item.getAsset().getLocation(), "created", item.getSerializedAt())).build();
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
	        		//check if the location exists
	        		try{
	        			//if everything is ok, create the item
	        			Item item = ps.createItem();
	        			Asset asset = item.getAssetWithLocation(new URIImpl(existingAssetLocation));
	        			
	        			assetIS = asset.getInputStream();
	        			mimeType = guessMimeType(assetIS);	        			
	        			if(mimeType == null ){
	                		mimeType=type;
	                	}
	        			
	        			//further check on data having few bytes inside
	        			if(assetIS.available() == 0){
	        				throw new IllegalArgumentException("No data found at "+existingAssetLocation+" for the asset of the new item");
	        			}
	        			asset.setFormat(mimeType);	 
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
	        			throw new IllegalArgumentException("No data found at "+existingAssetLocation+" for the asset of the new item");
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
    public Response submitItem(@QueryParam("item") String itemURI) throws RepositoryException, IOException {

        PersistenceService ps = eventManager.getPersistenceService();
        Item item = ps.getItem(new URIImpl(itemURI));

        if (item != null) {
            eventManager.injectItem(item);
        }

        log.info("submitted item {}", item.getURI());

        return Response.ok().build();
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
    public Response addPart(@QueryParam("itemUri")String itemURI, @QueryParam("type") String type, @QueryParam("name") String fileName, @Context HttpServletRequest request) throws RepositoryException, IOException {
        PersistenceService ps = eventManager.getPersistenceService();

        Item item = ps.getItem(new URIImpl(itemURI));

        Part part = item.createPart(extratorID);
        part.setSyntacticalType(type);

        Asset partAsset = part.getAsset();
        partAsset.setFormat(type);

        OutputStream out = partAsset.getOutputStream();
        int bytes = IOUtils.copy(request.getInputStream(), out);
        out.close();

        log.info("item {}, part created {} : uploaded {} bytes", item.getURI(), part.getURI(), bytes);

        return Response.ok(ImmutableMap.of("itemURI", item.getURI().stringValue(),"partURI", part.getURI().stringValue(), "assetLocation", partAsset.getLocation(), "created", part.getSerializedAt())).build();
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


        return Response.ok(entity, type).build();
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

}
