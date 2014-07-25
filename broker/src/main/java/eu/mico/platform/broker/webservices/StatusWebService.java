package eu.mico.platform.broker.webservices;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.ServiceDescriptor;
import eu.mico.platform.broker.model.Transition;
import eu.mico.platform.broker.model.TypeDescriptor;
import org.openrdf.model.URI;
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
    public List<Map<String,Object>> getItems()  {
        List<Map<String,Object>> result = new ArrayList<>();
        for(Map.Entry<String, ContentItemState> state : broker.getStates().entrySet()) {
            Map<String,Object> sprops = new HashMap<>();
            sprops.put("uri", state.getKey());

            List<Map<String,String>> transitions = new ArrayList<>();
            for(Map.Entry<String,Transition> t : state.getValue().getProgress().entrySet()) {
                Map<String,String> tprops = new HashMap<>();
                tprops.put("correlation", t.getKey());
                tprops.put("start_state", t.getValue().getStateStart().getSymbol());
                tprops.put("end_state", t.getValue().getStateEnd().getSymbol());
                tprops.put("service", t.getValue().getService().getUri().stringValue());
                tprops.put("object", t.getValue().getObject().stringValue());
                transitions.add(tprops);
            }
            sprops.put("transitions", transitions);

            List<Map<String,String>> states = new ArrayList<>();
            for(Map.Entry<URI,TypeDescriptor> s : state.getValue().getStates().entrySet()) {
                Map<String,String> tprops = new HashMap<>();
                tprops.put("state", s.getValue().getSymbol());
                tprops.put("object", s.getKey().stringValue());
                states.add(tprops);
            }
            sprops.put("states", states);


            result.add(sprops);
        }
        return result;
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
