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

import com.github.anno4j.Anno4j;
import com.jayway.restassured.RestAssured;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.testutils.Mockups;
import eu.mico.platform.testutils.TestServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.path.json.JsonPath.from;

public class VideoServiceTest {


    static private TestServer server;
    private static MICOQueryHelperMMM mqh;


    private static Repository repository;

    private static RepositoryConnection connection;


    @BeforeClass
    public static void init() throws Exception {


        //init in memory repository
        repository = Mockups.initializeRepository("reco/videokaldiner.ttl");
        connection = repository.getConnection();

        Anno4j anno4j = new Anno4j();
        anno4j.setRepository(repository);
        mqh = new MICOQueryHelperMMM(anno4j);

        Videos videoService = new Videos(mqh);

        //init server
        server = new TestServer();
        server.addWebservice(videoService);
        server.start();


    }

    @AfterClass
    public static void tearDown() throws Exception {

        connection.close();

    }


    @Test
    public void testGetDefaultVideos() throws IOException, RepositoryException {

        String json = RestAssured.
                given().
                when().
                get(server.getUrl() + "videos/default")
                .body().asString();

        List<String> fileList = from(json).get("filenames");

        Assert.assertEquals(9, fileList.size());
    }


    @Test
    public void testGetAnalyzedVideos() throws IOException, RepositoryException {

        String json = RestAssured.
                given().
                when().
                get(server.getUrl() + "videos/analyzed")
                .body().asString();

        List<String> fileList = from(json).get("filenames");

        Assert.assertTrue(fileList.size() > 0);
    }


    @Test
    public void testGetAnalyzedVideosV2() throws IOException, RepositoryException {

        String json = RestAssured.
                given().
                when().
                get(server.getUrl() + "videos/v2/analyzed")
                .body().asString();

        List<HashMap> videoList = from(json).get("analyzedVideos");

        Assert.assertNotNull(videoList);

        Assert.assertEquals(1, videoList.size());

        HashMap videoItem = videoList.get(0);

        Assert.assertEquals("p360 - Today in History for September 22nd.webm.mp4", videoItem.get("filename"));
        Assert.assertEquals("http://demo2.mico-project.eu:8080/marmotta/", videoItem.get("prefix"));
        Assert.assertEquals("84fd3c97-3805-41de-9e26-f5fd87e68d50", videoItem.get("id"));
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
