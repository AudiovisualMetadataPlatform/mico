package eu.mico.platform.broker.webservices;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.ServiceDescriptor;
import eu.mico.platform.broker.model.Transition;
import eu.mico.platform.broker.model.TypeDescriptor;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Path("/status")
public class StatusWebService {

    private static Logger log = LoggerFactory.getLogger(StatusWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTC");


    private MICOBroker broker;

    public StatusWebService(MICOBroker broker) {
        this.broker = broker;
    }


    @GET
    @Path("/dependencies")
    @Produces("image/png")
    public Response getDependencyGraph(@QueryParam("width") final int width, @QueryParam("height") final int height) {
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                SwingImageCreator.createGraph(broker.getDependencies(), new Dimension(width != 0 ? width : 640, height != 0 ? height : 480), "png", output);
            }
        };
        return Response.ok(output).build();
    }


    @GET
    @Path("/services")
    @Produces("application/json")
    public List<Map<String,String>> getServices()  {
        List<Map<String,String>> result = new ArrayList<>();
        for(ServiceDescriptor svc : broker.getDependencies().edgeSet()) {
            Map<String,String> sprops = new HashMap<>();
            sprops.put("name", svc.toString());
            sprops.put("uri", svc.getUri().stringValue());
            sprops.put("provides", svc.getProvides());
            sprops.put("requires", svc.getRequires());
            sprops.put("language", svc.getLanguage());
            sprops.put("time", ISO8601FORMAT.format(svc.getRegistrationTime()));
            sprops.put("calls", ""+svc.getCalls());
            result.add(sprops);
        }
        return result;
    }


    @GET
    @Path("/items")
    @Produces("application/json")
    public List<Map<String,Object>> getItems(@QueryParam("uri") String itemUri, @QueryParam("parts") boolean showParts) throws RepositoryException {
        List<Map<String,Object>> result = new ArrayList<>();
        if(itemUri == null) {
            // retrieve a list of all items
            for(Map.Entry<String, ContentItemState> state : broker.getStates().entrySet()) {
                result.add(wrapContentItemStatus(state.getKey(),state.getValue(),showParts));
            }
        } else if(broker.getStates().containsKey(itemUri)) {
            result.add(wrapContentItemStatus(itemUri, broker.getStates().get(itemUri),showParts));
        } else {
            throw new NotFoundException("item with uri " + itemUri + " not found in broker");
        }
        return result;
    }

    private Map<String,Object> wrapContentItemStatus(String uri, ContentItemState state, boolean showParts) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", uri);
        sprops.put("finished", state.isFinalState() ? "true" : "false");
        sprops.put("time", ISO8601FORMAT.format(state.getCreated()));

        if(showParts) {
            List<Map<String, Object>> parts = new ArrayList<>();
            ContentItem item = broker.getPersistenceService().getContentItem(new URIImpl(uri));
            for (Content part : item.listContentParts()) {
                parts.add(wrapContentStatus(state, part));
            }
            sprops.put("parts", parts);
        }

        return sprops;
    }

    private Map<String,Object> wrapContentStatus(ContentItemState state, Content part) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", part.getURI().stringValue());
        sprops.put("title", part.getProperty(DCTERMS.TITLE));
        sprops.put("type",  part.getType());
        sprops.put("creator",  stringValue(part.getRelation(DCTERMS.CREATOR)));
        sprops.put("created",  part.getProperty(DCTERMS.CREATED));
        sprops.put("source",  stringValue(part.getRelation(DCTERMS.SOURCE)));

        if(state != null) {
            if(state.getStates().get(part.getURI()) != null) {
                sprops.put("state", state.getStates().get(part.getURI()).getSymbol());
            }

            List<Map<String, String>> transitions = new ArrayList<>();
            for (Map.Entry<String, Transition> t : state.getProgress().entrySet()) {
                if(t.getValue().getObject().equals(part.getURI())) {
                    Map<String, String> tprops = new HashMap<>();
                    tprops.put("correlation", t.getKey());
                    tprops.put("start_state", t.getValue().getStateStart().getSymbol());
                    tprops.put("end_state", t.getValue().getStateEnd().getSymbol());
                    tprops.put("service", t.getValue().getService().getUri().stringValue());
                    transitions.add(tprops);
                }
            }
            sprops.put("transitions", transitions);
        }

        return sprops;
    }

    private static String stringValue(Value v) {
        return v != null ? v.stringValue() : null;
    }


    @GET
    @Path("/download")
    public Response downloadPart(@QueryParam("itemUri") String itemUri, @QueryParam("partUri") String partUri) throws RepositoryException {
        final ContentItem item = broker.getPersistenceService().getContentItem(new URIImpl(itemUri));
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


    @GET
    public Response getStatus() {
        return Response.status(Response.Status.OK).build();
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
