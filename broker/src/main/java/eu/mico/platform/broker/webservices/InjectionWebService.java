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

import eu.mico.platform.anno4j.model.impl.bodymmm.MultiMediaBody;
import eu.mico.platform.anno4j.model.impl.targetmmm.InitialTarget;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.metadata.MICOProvenance;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A webservice for creating new  items and adding new  parts. Used by the inject.html frontend
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Path("/inject")
public class InjectionWebService {

    private static Logger log = LoggerFactory.getLogger(InjectionWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTC");


    private EventManager eventManager;

    public InjectionWebService(EventManager manager) {
        this.eventManager = manager;
    }


    /**
     * Create a new item and return its URI in the "uri" field of the JSON response.
     * @return
     */
    @POST
    @Path("/create")
    @Produces("application/json")
    public Map<String,String> contentItem() throws RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();

        Item item = ps.createItem();

        log.info("created item {}", item.getURI());

        return ImmutableMap.of("uri", item.getURI().stringValue());
    }


    /**
     * Add a new part to the item using the request body as content. Return the URI of the new part in
     * the "uri" field of the JSON response.
     *
     * @return
     */
    @POST
    @Path("/add")
    @Produces("application/json")
    public Response addContentPart(@QueryParam("ci")String itemString, @QueryParam("type") String type, @QueryParam("name") String fileName, @Context HttpServletRequest request) throws RepositoryException, IOException {
        PersistenceService ps = eventManager.getPersistenceService();

        Item item = ps.getItem(new URIImpl(itemString));

        if(item == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("part item "+itemString+" does not exist").build();
        }

        Part part = item.createPart();
        part.setType(type);
        // deprecated - The model should use haslocation on the target instead of dcterms:source
        part.setProperty(DCTERMS.SOURCE, fileName);

        MICOProvenance provenance = new MICOProvenance();
        provenance.setExtractorName("http://www.mico-project.eu/broker/injection-web-service");

        MultiMediaBody multiMediaBody = new MultiMediaBody();
        multiMediaBody.setFormat(type);

        InitialTarget target = new InitialTarget(fileName);

        part.createAnnotation(multiMediaBody, null, provenance, target);

        OutputStream out = part.getOutputStream();
        int bytes = IOUtils.copy(request.getInputStream(), out);
        out.close();

        log.info("item {}: uploaded {} bytes for new part {}", item.getURI(), bytes, part.getURI());

        return Response.ok(ImmutableMap.of("uri", part.getURI().stringValue())).build();
    }


    /**
     * Submit the item for analysis by notifying the broker about its parts.
     * @return
     */
    @POST
    @Path("/submit")
    public Response submitItem(@QueryParam("ci")String itemString) throws RepositoryException, IOException {

        PersistenceService ps = eventManager.getPersistenceService();
        Item item = ps.getItem(new URIImpl(itemString));
        eventManager.injectItem(item);

        log.info("submitted item {}", item.getURI());

        return Response.ok().build();
    }


    @GET
    @Path("/items")
    @Produces("application/json")
    public List<Map<String,Object>> getItems(@QueryParam("uri") String itemUri, @QueryParam("parts") boolean showParts) throws RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();

        List<Map<String,Object>> result = new ArrayList<>();
        if(itemUri == null) {
            // retrieve a list of all items
            for(Item ci : ps.getItems()) {
                result.add(wrapItem(ci, showParts));
            }
        } else if(ps.getItem(new URIImpl(itemUri)) != null) {
            result.add(wrapItem(ps.getItem(new URIImpl(itemUri)), showParts));
        } else {
            throw new NotFoundException("item with uri " + itemUri + " not found in broker");
        }
        return result;
    }

    private Map<String,Object> wrapItem(Item ci, boolean showParts) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", ci.getURI().stringValue());

        if(showParts) {
            List<Map<String, Object>> parts = new ArrayList<>();
            for (Part part : ci.getParts()) {
                parts.add(wrapPart(part));
            }
            sprops.put("parts", parts);
        }

        return sprops;
    }

    private Map<String,Object> wrapPart(Part part) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", part.getURI().stringValue());
        sprops.put("title", part.getProperty(DCTERMS.TITLE));
        sprops.put("type",  part.getType());
        sprops.put("creator",  stringValue(part.getRelation(DCTERMS.CREATOR)));
        sprops.put("created",  part.getProperty(DCTERMS.CREATED));
        sprops.put("source",  stringValue(part.getRelation(DCTERMS.SOURCE)));

        return sprops;
    }

    private static String stringValue(Value v) {
        return v != null ? v.stringValue() : null;
    }


    @GET
    @Path("/download")
    public Response downloadPart(@QueryParam("itemUri") String itemUri, @QueryParam("partUri") String partUri) throws RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();

        final Item item = ps.getItem(new URIImpl(itemUri));
        if(item == null) {
            throw new NotFoundException("Part Item with URI " + itemUri + " not found in system");
        }
        final Part part = item.getPart(new URIImpl(partUri));
        if(part == null) {
            throw new NotFoundException("Part Part with URI " + partUri + " not found in system");
        }

        StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                IOUtils.copy(part.getInputStream(), output);
            }
        };

        return Response.ok(entity, part.getType()).build();
    }




    private static SimpleDateFormat createDateFormat(String format, String timezone) {
        SimpleDateFormat sdf =
                new SimpleDateFormat(format, DateFormatSymbols.getInstance(Locale.US));
        if (timezone != null) {
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return sdf;
    }

}
