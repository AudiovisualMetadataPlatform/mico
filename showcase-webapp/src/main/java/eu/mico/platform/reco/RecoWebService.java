package eu.mico.platform.reco;


import com.google.common.collect.ImmutableMap;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.reco.model.PioEventData;
import eu.mico.platform.reco.model.RequestBody;
import io.prediction.Event;
import io.prediction.EventClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Path("/reco")
public class RecoWebService {

    private static final Logger log = LoggerFactory.getLogger(RecoWebService.class);

    private final EventManager eventManager;
    private final MICOBroker broker;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;


    public RecoWebService(EventManager eventManager, MICOBroker broker, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.broker = broker;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
    }


    @GET
    @Path("/testcall")
    @Produces("text/plain")
    public Response getTestResponse() {
        return Response.ok("Trallala").build();
    }

    @GET
    @Path("/dockerstatus")
    @Produces("text/plain")
    public Response getDockerStatus() {

        String response = RecoUtils.getCmdOutput("ps aux");
        String docker_cmd = RecoUtils.getDockerCmd(response);

        boolean dockerRunning = true;
        if (docker_cmd == null) {
            docker_cmd = "<No WP5 Docker instance found>";
            dockerRunning = false;
        }


        return Response.status(Response.Status.CREATED)
                .entity(ImmutableMap.of("docker_running", dockerRunning, "calls", docker_cmd))
                .build();
    }

    @GET
    @Path("/piostatus")
    @Produces("text/plain")
    public Response getPioStatus() {
        try {
            String response = RecoUtils.forwardGET(DockerConf.PIO_EVENT_API);
            return Response.ok(response).build();

        } catch (IOException e) {
            return Response.ok(e.getMessage()).build();
        }
    }

    @GET
    @Path("/pioevents")
    @Produces("text/plain")
    public Response getPioEvents() {

        try {
            URI eventspath = new URI(DockerConf.PIO_EVENT_API.toString() + "/events.json?accessKey=micoaccesskey&limit=30000");
            String response = RecoUtils.forwardGET(eventspath);
            return Response.ok(response).build();

        } catch (IOException | URISyntaxException e) {
            return Response.ok(e.getMessage()).build();
        }
    }


    @GET
    @Path("/piosimplereco")
    @Produces("text/plain")
    public Response getSimpleReco(
            @QueryParam("userId") final String userId,
            @QueryParam("length") final String length
    ) {

        try {
            URI recopath = new URI(DockerConf.PIO_RECO_API.toString() + "/queries.json");
            String data = "{ \"user\": \"" + userId + "\", \"num\": "+ length + " }";
            String response = RecoUtils.forwardPOST(recopath, data);
            return Response.ok(response).build();

        } catch (IOException | URISyntaxException e) {
            return Response.ok(e.getMessage()).build();
        }
    }


    @POST
    @Path("/createentity")
    @Produces("text/plain")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEvent(RequestBody input) {

        PioEventData eventData;
        try {
            eventData = PioEventData.fromJSON(input.body);
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }

        Event pioEvent = new Event()
                .event(eventData.event)
                .eventTime(DateTime.now())
                .entityId(eventData.entityId)
                .entityType(eventData.entityType)
                .targetEntityId(eventData.targetEntityId)
                .targetEntityType(eventData.targetEntityType);



        EventClient ec = new EventClient("micoaccesskey", DockerConf.PIO_EVENT_API.toString());
        String eventId;
        try {
            eventId = ec.createEvent(pioEvent);
        } catch (ExecutionException | InterruptedException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }

        ec.close();


        return Response.status(Response.Status.CREATED)
                .entity(ImmutableMap.of("entity_id", eventId, "entity", pioEvent.toString(), "input", input.body))
                .build();
    }

}
