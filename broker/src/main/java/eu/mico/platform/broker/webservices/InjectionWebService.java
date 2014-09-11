package eu.mico.platform.broker.webservices;

import com.google.common.collect.ImmutableMap;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.Transition;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
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
 * A webservice for creating new content items and adding new content parts. Used by the inject.html frontend
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
     * Create a new content item and return its URI in the "uri" field of the JSON response.
     * @return
     */
    @POST
    @Path("/create")
    @Produces("application/json")
    public Map<String,String> createContentItem() throws RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();

        ContentItem item = ps.createContentItem();

        log.info("created content item {}", item.getURI());

        return ImmutableMap.of("uri", item.getURI().stringValue());
    }


    /**
     * Add a new content part to the content item using the request body as content. Return the URI of the new part in
     * the "uri" field of the JSON response.
     *
     * @return
     */
    @POST
    @Path("/add")
    @Produces("application/json")
    public Response addContentPart(@QueryParam("ci")String contentItem, @QueryParam("type") String type, @QueryParam("name") String fileName, @Context HttpServletRequest request) throws RepositoryException, IOException {
        PersistenceService ps = eventManager.getPersistenceService();

        ContentItem item = ps.getContentItem(new URIImpl(contentItem));

        if(item == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("content item "+contentItem+" does not exist").build();
        }

        Content content = item.createContentPart();
        content.setType(type);
        content.setRelation(DCTERMS.CREATOR, new URIImpl("http://www.mico-project.eu/broker/injection-web-service"));
        content.setProperty(DCTERMS.CREATED, ISO8601FORMAT.format(new Date()));
        content.setProperty(DCTERMS.SOURCE, fileName);

        OutputStream out = content.getOutputStream();
        int bytes = IOUtils.copy(request.getInputStream(), out);
        out.close();

        log.info("content item {}: uploaded {} bytes for new content part {}", item.getURI(), bytes, content.getURI());

        return Response.ok(ImmutableMap.of("uri", content.getURI().stringValue())).build();
    }


    /**
     * Submit the content item for analysis by notifying the broker about its parts.
     * @return
     */
    @POST
    @Path("/submit")
    public Response submitContentItem(@QueryParam("ci")String contentItem) throws RepositoryException, IOException {

        PersistenceService ps = eventManager.getPersistenceService();
        ContentItem item = ps.getContentItem(new URIImpl(contentItem));
        eventManager.injectContentItem(item);

        log.info("submitted content item {}", item.getURI());

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
            for(ContentItem ci : ps.getContentItems()) {
                result.add(wrapContentItem(ci, showParts));
            }
        } else if(ps.getContentItem(new URIImpl(itemUri)) != null) {
            result.add(wrapContentItem(ps.getContentItem(new URIImpl(itemUri)), showParts));
        } else {
            throw new NotFoundException("item with uri " + itemUri + " not found in broker");
        }
        return result;
    }

    private Map<String,Object> wrapContentItem(ContentItem ci, boolean showParts) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", ci.getURI().stringValue());

        if(showParts) {
            List<Map<String, Object>> parts = new ArrayList<>();
            for (Content part : ci.listContentParts()) {
                parts.add(wrapContent(part));
            }
            sprops.put("parts", parts);
        }

        return sprops;
    }

    private Map<String,Object> wrapContent(Content part) throws RepositoryException {
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

        final ContentItem item = ps.getContentItem(new URIImpl(itemUri));
        if(item == null) {
            throw new NotFoundException("Content Item with URI " + itemUri + " not found in system");
        }
        final Content     part = item.getContentPart(new URIImpl(partUri));
        if(part == null) {
            throw new NotFoundException("Content Part with URI " + partUri + " not found in system");
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
