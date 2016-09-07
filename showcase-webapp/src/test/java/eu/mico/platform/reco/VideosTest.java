package eu.mico.platform.reco;

import com.jayway.restassured.RestAssured;
import eu.mico.platform.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.IOException;

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
public class VideosTest {


    static private TestServer server;


    @BeforeClass
    public static void init() throws Exception {


        Videos videoService = new Videos();


        //init server
        server = new TestServer();

        server.addWebservice(videoService);

        server.start();
    }

    @Test
    public void testTestcall() throws IOException, RepositoryException {
        RestAssured.given().log().all().

                when().
                get(server.getUrl() + "videos").
                then().log().body().
                assertThat().

                body("filename", Matchers.notNullValue());
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
