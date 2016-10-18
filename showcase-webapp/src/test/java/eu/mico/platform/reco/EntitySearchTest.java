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

import java.util.HashMap;
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
public class EntitySearchTest {

    static private TestServer server;
    private static MICOQueryHelperMMM mqh;


    private static Repository repository;

    private static RepositoryConnection connection;
    private static Anno4j anno4j;


    @BeforeClass
    public static void init() throws Exception {


        //init in memory repository
        repository = Mockups.initializeRepository("reco/twovideos.ttl");
        connection = repository.getConnection();

        anno4j = new Anno4j();
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
    public void testGetByEntity() throws Exception {

        String json = RestAssured.
                given().
                when().
                get(server.getUrl() + "videos/entities/World_war")
                .body().asString();

        List<HashMap> videoList = from(json).get("videos");

        Assert.assertNotNull(videoList);
        Assert.assertEquals(2, videoList.size());

    }


}
