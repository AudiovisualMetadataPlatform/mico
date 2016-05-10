package eu.mico.platform.reco;


import com.google.common.collect.ImmutableMap;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.zooniverse.model.TextAnalysisInput;
import io.prediction.Event;
import io.prediction.EventClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
            String data = "{ \"user\": \"" + userId + "\", \"num\": "+ length + " }";
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

}
