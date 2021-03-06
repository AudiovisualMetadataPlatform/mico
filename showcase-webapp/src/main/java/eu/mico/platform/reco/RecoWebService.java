/*
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

package eu.mico.platform.reco;


import com.google.common.collect.ImmutableMap;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.model.PioEventData;
import eu.mico.platform.reco.model.RequestBody;
import io.prediction.Event;
import io.prediction.EventClient;
import org.joda.time.DateTime;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.pojo.ApiVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Api(name = "Prediction IO Access", description = "Methods for querying prediction.io", group = "reco")
@Path("/reco")
public class RecoWebService {

    private static final Logger log = LoggerFactory.getLogger(RecoWebService.class);
    private MICOQueryHelperMMM mqh;

    public RecoWebService(MICOQueryHelperMMM micoQueryHelper) {
        this.mqh = micoQueryHelper;
    }


    @GET
    @Path("/testcall")
    @Produces("application/json")
    public Response getTestResponse() {
        return Response.ok("{\"status\":\"Trallala\"}").build();
    }


    @ApiMethod(
            path = "/reco/dockerstatus",
            verb = ApiVerb.GET,
            description = "Determines whether docker is running on the system",
            produces = {MediaType.APPLICATION_JSON}
    )
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


    @ApiMethod(
            path = "/reco/piostatus",
            verb = ApiVerb.GET,
            description = "Determines whether prediction.io is running on the system",
            produces = {MediaType.APPLICATION_JSON}
    )
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


    @ApiMethod(
            path = "/reco/pioevents",
            verb = ApiVerb.GET,
            description = "Outpúts all events stored by the current prediction.io instance",
            produces = {MediaType.APPLICATION_JSON}
    )

    @GET
    @Path("/pioevents")
    @Produces("text/plain")
    public Response getPioEvents() {

        try {
            URI eventspath = new URI(DockerConf.PIO_EVENT_API.toString() + "/events.json?accessKey=micoaccesskey&limit=300");
            String response = RecoUtils.forwardGET(eventspath);
            return Response.ok(response).build();

        } catch (IOException | URISyntaxException e) {
            return Response.ok(e.getMessage()).build();
        }
    }


    @ApiMethod(
            path = "/reco/piosimplereco",
            verb = ApiVerb.GET,
            description = "Returns $length recommendtion for give $itemId",
            produces = {MediaType.APPLICATION_JSON}
    )

    @GET
    @Path("/piosimplereco")
    @Produces("text/plain")
    public Response getSimpleReco(
            @QueryParam("itemId") final String itemId,
            @QueryParam("length") final String length
    ) {

        log.info("/piosimplereco called:");
        log.info("itemId", itemId);
        log.info("length", length);

        try {
            URI recopath = new URI(DockerConf.PIO_RECO_API.toString() + "/queries.json");
            String data = "{ \"item\": \"" + itemId + "\", \"num\": " + length + " }";
            String response = RecoUtils.forwardPOST(recopath, data);
            return Response.ok(response).build();

        } catch (IOException | URISyntaxException e) {
            return Response.ok(e.getMessage()).build();
        }
    }


    @ApiMethod(
            path = "/reco/createentity",
            verb = ApiVerb.POST,
            description = "Adds new event to prediction.io",
            produces = {MediaType.APPLICATION_JSON}
    )
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


    @ApiMethod(
            path = "/reco/zoo/{subject_id}/discussion/relatedsubjects",
            verb = ApiVerb.GET,
            description = "Returns relöated subjects for a given $subject_id",
            produces = {MediaType.APPLICATION_JSON}
    )
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


    @GET
    @Path("/zoo/{subject_id}/is_debated2")
    @Produces("application/json")
    public Response isDebatedSubjects(@PathParam("subject_id") String subject_id) {


        ZooReco zooReco = new ZooReco(mqh);


        double debatedScore = zooReco.getDebatedScore(subject_id);
        double threshold = 0.7;

        JsonObject returnValue = Json.createObjectBuilder()
                .add("reco_id", subject_id)
                .add("is_debated", debatedScore > threshold)
                .add("score", debatedScore)
                .add("comment", "Random Score")
                .build();

        return Response.ok(returnValue.toString()).build();
    }


    @ApiMethod(
            path = "/reco/zoo/{subject_item}/is_debated",
            verb = ApiVerb.GET,
            description = "Returns whether a given subject (represented as an Item) is debated by its users",
            produces = {MediaType.APPLICATION_JSON}
    )

    @GET
    @Path("/zoo/{subject_item}/is_debated")
    @Produces("application/json")
    public Response isDebatedSubjects2(
            @ApiPathParam(
                name = "subject_item",
                description = "Item-Uri corresponding with the analyzed subject image (please pseudo-escape the slashes with !S)"
            )
            @PathParam("subject_item") String subject_item,

            @ApiQueryParam(
                    name = "chatItem",
                    description = "Item-Uris  corresponding with talk entries to the queried subject. " +
                            "Several talk-entries are allowed by passing several chatItem=..."

            )
            @QueryParam("chatItem") List<String> chatItems) {

        // Lesson learned: tomcat (and apache) do not allow escaped forward slashes in the path for security reason.
        // we only expect Mico-ItemID's so this !S escaping is ugly, but shouldn't be critical.
        subject_item = subject_item.replace("%2F", "/");
        subject_item = subject_item.replace("!S", "/");
        List<String> escapedItems = new ArrayList<>();
        for (String item : chatItems) {
            escapedItems.add(item.replace("%2F", "/"));
        }


        ZooReco zooReco = new ZooReco(mqh);
        double debatedScore = zooReco.getDebatedScore(subject_item, escapedItems);

        double threshold = 0.7;

        JsonObject returnValue = Json.createObjectBuilder()
                .add("reco_id", subject_item)
                .add("is_debated", debatedScore > threshold)
                .add("score", debatedScore)
                .add("comment", "Calculated Score")
                .add("chat_size", chatItems.size())
                .build();
        return Response.ok(returnValue.toString()).build();
    }


}
