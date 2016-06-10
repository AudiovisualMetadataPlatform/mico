package eu.mico.platform.reco.Resources;

import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
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
public class NER {

    private static final Logger log = Logger.getAnonymousLogger();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/entities/{source}")
    public Response getRequest(@PathParam("source") String source) {

        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Processing request...");
        System.out.println(source);

//        String source = "p720 - Agricoltura ecologica Greenpeace in dirigibile sopra Milano.mp4";

        Map<String, EntityInfo> linkedEntitites = NERQuery.getLinkedEntities(source, DataField.SOURCE);

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
    @Path("availableFiles")
    public Response getAvailableFiles() {

        System.out.println("Processing request... availableFiles");

        ObjectMapper mapper = new ObjectMapper();

        //TODO: retrieve from Marmotta

        //set defaults
        String[] _availableFiles = {
                "zooniverse.txt",
                "1.txt",
                "2.txt",
                "3.txt",
                "desc.txt",
                "p360 - GREENPEACE THE OCEAN.mp4",
                "p720 - Agricoltura ecologica Greenpeace in dirigibile sopra Milano.mp4",
                "p720 - Detox presente par Michel Genet, directeur de Greenpeace Belgique.mp4",
                "p720 - p720 - Greenpeace Year in Pictures 2011.mp4",
                "p720 - p720 - HTCs Comma, Flash on Mobile Devices, Greenpeace, The Oona and much more iPhone Life Magazine.mp4",
                "p720 - GREENPEACE DEFORESTATION.mp4"};
        List<String> availableFiles = Arrays.asList(_availableFiles);


        java.nio.file.Path availableFilesPath = Paths.get("availableFiles.txt");

        if (!Files.isReadable(availableFilesPath)) {
            log.warning(availableFilesPath.toAbsolutePath().toString() + "  not readable, using defaults");
        } else {
            log.info("Proceeding with " + availableFilesPath.toAbsolutePath());
            try {
                availableFiles = Files.readAllLines(availableFilesPath, Charset.defaultCharset());

            } catch (IOException e) {
                log.warning(e.getMessage());
            }

        }


        String response = "";
        try {
            response = mapper.writeValueAsString(availableFiles);
            log.info(response);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.ok(response).build();

    }


    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("videofilenames")
    public Response postVideoFileNames(String postData) {
        log.info("videofilenames");
        log.info(postData);


        //TODO data sanity check

        java.nio.file.Path filePath = Paths.get("availableFiles.txt");
        try {


            Files.write(
                    filePath,
                    postData.getBytes(),
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }

        return Response.ok(filePath.toAbsolutePath().toString()).build();
    }


}
