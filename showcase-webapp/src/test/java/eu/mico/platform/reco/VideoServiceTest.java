package eu.mico.platform.reco;

import com.jayway.restassured.RestAssured;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.NERQuery;
import eu.mico.platform.testutils.TestServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.List;

import static com.jayway.restassured.path.json.JsonPath.from;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class VideoServiceTest {


    static private TestServer server;
    private static MICOQueryHelperMMM mqh;




    @BeforeClass
    public static void init() throws Exception {

        mqh = NERQuery.getMicoQueryHelper();
        Videos videoService = new Videos(mqh);


        //init server
        server = new TestServer();

        server.addWebservice(videoService);

        server.start();



    }

    @Test
    public void testGetDefaultVideos() throws IOException, RepositoryException {

        String json = RestAssured.
                given().log().all().
                when().
                get(server.getUrl() + "videos/default")
                .body().asString();

        List<String> fileList = from(json).get("filenames");

        Assert.assertTrue(fileList.size() == 6);
    }


    @Test
    public void testGetAnalyzedVideos() throws IOException, RepositoryException {

        String json = RestAssured.
                given().log().all().
                when().
                get(server.getUrl() + "videos/analyzed")
                .body().asString();

        List<String> fileList = from(json).get("filenames");



        Assert.assertTrue(fileList.size() > 0);
    }



    @Test
    public void postStuff() throws Exception {


        JsonArrayBuilder responseFileListBuilder = Json.createArrayBuilder();

        JsonObject videoFileObject = Json.createObjectBuilder()
                .add("filename", "testfile")
                .build();

        responseFileListBuilder.add(videoFileObject);


        RestAssured.given()
                .contentType("application/json")
                .body(responseFileListBuilder.build())
                .when().post(server.getUrl() + "videos").then()
                .statusCode(201);


    }


}
