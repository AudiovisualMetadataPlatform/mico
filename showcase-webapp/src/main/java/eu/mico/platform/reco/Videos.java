package eu.mico.platform.reco;

import com.github.anno4j.model.namespaces.DCTERMS;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.NERQuery;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.openrdf.OpenRDFException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    @Path("/v2/analyzed/")
    public Response getAnalyzedVideos2() {

        JsonArrayBuilder itemDescriptionArray = Json.createArrayBuilder();

        List<NERQuery.ItemDescription> fileNames_mp4 = NERQuery.getItemDescriptionByFormat("video/mp4", mqh);
        List<NERQuery.ItemDescription> fileNames_quicktime = NERQuery.getItemDescriptionByFormat("video/quicktime", mqh);

        Set<NERQuery.ItemDescription> itemDescriptions = new LinkedHashSet<>();
        itemDescriptions.addAll(fileNames_mp4);
        itemDescriptions.addAll(fileNames_quicktime);

        for (NERQuery.ItemDescription itemDescription : itemDescriptions) {

            JsonObjectBuilder descriptionObject = Json.createObjectBuilder();

            if (itemDescription != null) {
                descriptionObject.add("filename", itemDescription.getFilename());
                descriptionObject.add("prefix", itemDescription.getPrefix());
                descriptionObject.add("id", itemDescription.getId());
            }

            itemDescriptionArray.add(descriptionObject);

        }

        JsonObject response = Json.createObjectBuilder()
                .add("analyzedVideos", itemDescriptionArray.build())
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


    @GET
    @Path("/entities/{entity}")
    public Response getEntities(@PathParam("entity") String entity) {


        final String searchEntity = "http://dbpedia.org/resource/" + entity;


        List<ItemMMM> items;
        try {
            QueryService qs = mqh.getAnno4j().createQueryService()
                    .addPrefix(MMM.PREFIX, MMM.NS)
                    .addPrefix("fusepool", "http://vocab.fusepool.info/fam#")
                    .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                    .addCriteria("mmm:hasPart/mmm:hasBody/fusepool:entity-reference", searchEntity);


            items = qs.execute(ItemMMM.class);

        } catch (OpenRDFException | ParseException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        JsonArrayBuilder resultList = Json.createArrayBuilder();

        for (ItemMMM item : items) {

            JsonObject resultItem = Json.createObjectBuilder()
                    .add("sourceName", item.getAsset().getName())
                    .add("itemId", item.toString())
                    .build();

            resultList.add(resultItem);

        }


        JsonObject response = Json.createObjectBuilder()
                .add("videos", resultList.build())
                .build();


        return Response.ok(response.toString()).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setVideos() {
        return Response.created(null).build();
    }
}
