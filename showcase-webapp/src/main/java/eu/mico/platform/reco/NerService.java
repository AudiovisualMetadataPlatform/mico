package eu.mico.platform.reco;

import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.DataField;
import eu.mico.platform.reco.Resources.EntityInfo;
import eu.mico.platform.reco.Resources.NERQuery;
import eu.mico.platform.reco.Resources.Transcript;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;


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


//Under the pillow: https://jersey.java.net/documentation/latest/user-guide.html

@Path("ner")
public class NerService {


    private static final Logger log = Logger.getAnonymousLogger();
    private static MICOQueryHelperMMM mqh;

    public NerService(MICOQueryHelperMMM micoQueryHelperMMM) {
        mqh = micoQueryHelperMMM;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{source}/entities")
    public Response getLinkedEntities(@PathParam("source") String source, @QueryParam("querytype") String queryType) {

        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Processing request...");
        System.out.println(source);

//        String source = "p720 - Agricoltura ecologica Greenpeace in dirigibile sopra Milano.mp4";

        Map<String, EntityInfo> linkedEntitites = null;
        if (queryType != null && queryType.equals("id")) {
            linkedEntitites = NERQuery.getLinkedEntities(source, DataField.CONTENTITEM, mqh);
        }
        else {
            linkedEntitites = NERQuery.getLinkedEntities(source, DataField.NAME, mqh);
        }
        assert linkedEntitites != null;

        String response = "";
        try {
            response = mapper.writeValueAsString(linkedEntitites.values());
        } catch (IOException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.ok(response).build();

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{source}/transcript")
    public Response getTranscript(@PathParam("source") String source) {

        Transcript transcript = NERQuery.getTranscript(source, DataField.NAME, mqh);
        Response response;

        if (transcript == null) {
            response = Response.serverError().build();
        } else {
            response = Response.ok(transcript.toJson()).build();
        }

        return response;

    }


}
