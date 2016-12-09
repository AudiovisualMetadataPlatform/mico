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

import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.path.json.JsonPath.from;

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

        Videos videoService = new Videos(mqh, "http://demo2.mico-project.eu:8080/marmotta/");

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



    @Test
    public void testGetRelated() throws Exception {

        final String fileName = "file1.mp4";
        String json = RestAssured.
                given().
                when().
                get(server.getUrl() + "videos/related/" + fileName)
                .body().asString();


        HashMap<String, List<String>> videoList = from(json).get("relatedItems");

        System.out.println(json);

        Assert.assertEquals("file2.mp4", videoList.get("http://dbpedia.org/resource/Emperor").get(0));

        //test for resource with parantheses
        Assert.assertEquals("file2.mp4", videoList.get("http://dbpedia.org/resource/Command_(military_formation)").get(0));


    }

    @Test
    public void testGetRelated_uuid() throws Exception {

        final String uuid = "c9e54133-56dc-4d2f-b6f6-6d60ad0828b5";
        String json = RestAssured.
                given().
                when().
                get(server.getUrl() + "videos/related/" + uuid)
                .body().asString();


        HashMap<String, List<String>> videoList = from(json).get("relatedItems");

        System.out.println(json);

        Assert.assertEquals("file2.mp4", videoList.get("http://dbpedia.org/resource/Emperor").get(0));

        //test for resource with parantheses
        Assert.assertEquals("file2.mp4", videoList.get("http://dbpedia.org/resource/Command_(military_formation)").get(0));


    }


}
