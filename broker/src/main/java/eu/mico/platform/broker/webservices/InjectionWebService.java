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
    public Response createItem(@QueryParam("type") String type, @QueryParam("assetLocation") String assetLocation, @Context HttpServletRequest request) throws RepositoryException, IOException {

    	PersistenceService ps = eventManager.getPersistenceService();
    	InputStream in = new BufferedInputStream(request.getInputStream());

        Item item = ps.createItem();
        item.setSyntacticalType(type);
        item.setSemanticType("application/injection-webservice");

        Asset asset = item.getAsset();
        asset.setFormat(type);
        OutputStream out = asset.getOutputStream();
        
        int bytes = IOUtils.copy(in, out);
        out.close();
        in.close();

        log.info("item created {}: uploaded {} bytes", item.getURI(), bytes);

        return Response.ok(ImmutableMap.of("itemUri", item.getURI().stringValue(), "assetLocation", item.getAsset().getLocation(), "created", item.getSerializedAt())).build();
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

}
