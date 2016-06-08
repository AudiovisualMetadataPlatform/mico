package eu.mico.platform.reco;


import com.google.common.collect.ImmutableMap;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.zooniverse.model.TextAnalysisInput;
import io.prediction.Event;
import io.prediction.EventClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Path("/reco")
public class RecoWebService {

    private static final Logger log = LoggerFactory.getLogger(RecoWebService.class);

    private final EventManager eventManager;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;


    public RecoWebService(EventManager eventManager, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
    }


    @GET
    @Path("/testcall")
    @Produces("application/json")
    public Response getTestResponse() {
        return Response.ok("{\"status\":\"Trallala\"}").build();
    }

    @GET
    @Path("/dockerstatus")
    @Produces("text/plain")
    public Response getDockerStatus() {

        String response = DockerUtils.getCmdOutput("ps aux");
        String docker_cmd = DockerUtils.getDockerCmd(response);

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
            String response = DockerUtils.forwardGET(DockerConf.PIO_EVENT_API);
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
            String response = DockerUtils.forwardGET(eventspath);
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
            String data = "{ \"user\": \"" + userId + "\", \"num\": " + length + " }";
            String response = DockerUtils.forwardPOST(recopath, data);
            return Response.ok(response).build();

        } catch (IOException | URISyntaxException e) {
            return Response.ok(e.getMessage()).build();
        }
    }


    @GET
    @Path("/createentity")
    @Produces("text/plain")
    public Response createEvent(TextAnalysisInput input) {

        //TODO!

        Event pioEvent = new Event()
                .event("buy")
                .eventTime(DateTime.now())
                .entityId("435345345")
                .entityType("user")
                .targetEntityId("TargetWebsite")
                .targetEntityType("item");


        System.out.println(pioEvent.toJsonString());

        EventClient ec = new EventClient("micoaccesskey", DockerConf.PIO_EVENT_API.toString());
        String eventId = null;
        try {
            eventId = ec.createEvent(pioEvent);
        } catch (ExecutionException | InterruptedException | IOException e) {
            return Response.ok(e.getMessage()).build();
        }

        ec.close();


        return Response.status(Response.Status.CREATED)
                .entity(ImmutableMap.of("entity_id", eventId, "entity", pioEvent.toString(), "input", input.comment))
                .build();

    }


    @GET
    @Path("/zoo/{subject_id}/discussion/relatedsubjects")
    @Produces("application/json")
    public Response getRelatedSubjects(@PathParam("subject_id") String subject_id) {

        JsonObject returnValue = Json.createObjectBuilder()
                .add("reco_id", subject_id)
                .add("talk_analysis", "Lion")
                .add("user_competence", 0.8)
                .add("image_analysis", "Ostrich")
                .add("related_subject", "DSG0000111")
                .add("confidence", 0.3)
                .build();


        return Response.ok(returnValue.toString()).build();
    }

}
