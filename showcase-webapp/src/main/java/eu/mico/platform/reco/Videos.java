package eu.mico.platform.reco;

import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.NERQuery;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

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
@Path("/videos")
@Produces(MediaType.APPLICATION_JSON)
public class Videos {
    private final MICOQueryHelperMMM mqh;

    public Videos(MICOQueryHelperMMM micoQueryHelperMMM) {
        this.mqh = micoQueryHelperMMM;
    }

    @GET
    @Path("/default")
    public Response getDefaultVideos() {


        //set defaults
        String[] _availableFiles = {
                "sep30.mp4",
                "history.mp4",
                "mohamed.mp4",
                "p360 - GREENPEACE THE OCEAN.mp4",
                "p720 - Agricoltura ecologica Greenpeace in dirigibile sopra Milano.mp4",
                "p720 - Detox presente par Michel Genet, directeur de Greenpeace Belgique.mp4",
                "p720 - p720 - Greenpeace Year in Pictures 2011.mp4",
                "p720 - p720 - HTCs Comma, Flash on Mobile Devices, Greenpeace, The Oona and much more iPhone Life Magazine.mp4",
                "p720 - GREENPEACE DEFORESTATION.mp4"};

        List<String> availableFiles = Arrays.asList(_availableFiles);


        JsonArrayBuilder responseFileListBuilder = Json.createArrayBuilder();
        for (String fileName : availableFiles) {

            responseFileListBuilder.add(fileName);
        }


        JsonObject response = Json.createObjectBuilder()
                .add("filenames", responseFileListBuilder.build())
                .build();


        return Response.ok(response.toString()).build();
    }


    @GET
    @Path("/analyzed")
    public Response getAnalyzedVideos() {


        JsonArrayBuilder responseFileListBuilder = Json.createArrayBuilder();



        List<String> fileNames_mp4 = NERQuery.getFileNamesByFormat("video/mp4", mqh);
        List<String> fileNames_quicktime = NERQuery.getFileNamesByFormat("video/quicktime", mqh);

        Set<String> fileNames = new LinkedHashSet<>();
        fileNames.addAll(fileNames_mp4);
        fileNames.addAll(fileNames_quicktime);


        for (String filename : fileNames) {
            if (filename != null) {
                responseFileListBuilder.add(filename);
            }

        }

        JsonObject response = Json.createObjectBuilder()
                .add("filenames", responseFileListBuilder.build())
                .build();


        return Response.ok(response.toString()).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setVideos() {
        return Response.created(null).build();
    }
}
