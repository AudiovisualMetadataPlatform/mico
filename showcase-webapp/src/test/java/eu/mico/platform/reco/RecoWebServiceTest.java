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
import com.github.anno4j.model.namespaces.DCTERMS;
import com.github.anno4j.model.namespaces.OADM;
import com.github.anno4j.querying.QueryService;
import com.jayway.restassured.RestAssured;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.AnimalInfo;
import eu.mico.platform.reco.Resources.SentimentResult;
import eu.mico.platform.testutils.Mockups;
import eu.mico.platform.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.path.json.JsonPath.from;

public class RecoWebServiceTest {


    static private TestServer server;
    private static MICOQueryHelperMMM mqh;


    private static Repository repository;

    private static RepositoryConnection connection;
    private ZooReco zooReco;

    @BeforeClass
    public static void init() throws Exception {

        //init in memory repository
        repository = Mockups.initializeRepository("reco/zooniverse.ttl");
        connection = repository.getConnection();

        Anno4j anno4j = new Anno4j();
        anno4j.setRepository(repository);
        mqh = new MICOQueryHelperMMM(anno4j);


        RecoWebService recoWebService = new RecoWebService(mqh);


        //init server
        server = new TestServer();

        server.addWebservice(recoWebService);

        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();

    }

    @Test
    public void testTestcall() throws IOException, RepositoryException {
        RestAssured.

                when().
                get(server.getUrl() + "reco/testcall").
                then().
                assertThat().
                body("status", Matchers.equalTo("Trallala"));
    }

    @Test
    public void testTalkanalysis() throws Exception {

        RestAssured.
                when()
                .get(server.getUrl() + "reco/zoo/12345/discussion/relatedsubjects")
                .then()
                .assertThat()
                .statusCode(200)
                .body("reco_id", Matchers.equalTo("12345"))
                .body("talk_analysis", Matchers.equalTo("Lion"))
                .body("user_competence", Matchers.equalTo(0.8f))
                .body("image_analysis", Matchers.equalTo("Ostrich"))
                .body("related_subject", Matchers.equalTo("DSG0000111"))
                .body("confidence", Matchers.equalTo(0.3f));

    }

    @Test
    public void name() throws Exception {

        QueryService qs = mqh.getAnno4j().createQueryService()
                .addPrefix(MMM.PREFIX, MMM.NS)
                .addPrefix(MMMTERMS.PREFIX, MMMTERMS.NS)
                .addPrefix(DCTERMS.PREFIX, DCTERMS.NS)
                .addPrefix(OADM.PREFIX, OADM.NS)
                .addCriteria("mmm:hasPart/oa:hasBody[is-a mmmterms:AnimalDetectionBody]/rdf:value", "gazelle");


        List<ItemMMM> retList = qs.execute(ItemMMM.class);

        System.out.println(retList.size());

        for (ItemMMM item : retList) {
            System.out.println(item.toString());

        }


    }


    @Test
    public void testIsDebated_without_chat() throws Exception {

        String subject = "12345";

        String json = RestAssured.
                given().
                when()
                .get(server.getUrl() + "reco/zoo/" + subject + "/is_debated")
                .body().asString();

        Float score = from(json).get("score");
        Assert.assertEquals(0.0d, score.doubleValue(), 0.001d);
    }

    @Test
    public void testIsDebated_with_bogus_items() throws Exception {

        String subject = "12345";

        String json = RestAssured.
                given().
                when()
                .get(server.getUrl() + "reco/zoo/" + subject + "/is_debated?chatItem=uno&chatItem=due&chatItem=tres")
                .body().asString();

        String reco_id = from(json).get("reco_id");
        Float score = from(json).get("score");

        Assert.assertEquals(subject, reco_id);
        Assert.assertEquals(0.0d, score.doubleValue(), 0.001d);
    }


    @Test
    public void testIsDebated_valid_items() throws Exception {

        String subject = "http://demo1.mico-project.eu:8080/marmotta/61af22c9-a8e0-44b9-82c0-c3248f1aa046";
        String subject_escaped = subject.replace("/", "%2F");
        String chat_escaped = "http://demo1.mico-project.eu:8080/marmotta/bac38e61-257b-417e-b2aa-3e1835aa59d2".replace("/", "%2F");

        String json = RestAssured.
                given().
                when()
                .get(server.getUrl() + "reco/zoo/" + subject_escaped + "/is_debated?chatItem=" + chat_escaped + "&chatItem=" + chat_escaped)
                .body().asString();


        System.out.println(json);

        String reco_id = from(json).get("reco_id");
        Float score = from(json).get("score");

        Assert.assertEquals(subject, reco_id);
        Assert.assertEquals(0.2d, score.doubleValue(), 0.001d);
    }



    @Before
    public void setUp() throws Exception {

        zooReco = new ZooReco(mqh);

    }

    @Test
    public void testGetAnimal() throws Exception {

        String host = "http://demo1.mico-project.eu:8080/marmotta/";
        String item = "61af22c9-a8e0-44b9-82c0-c3248f1aa046";

        List<AnimalInfo> animalInfos = zooReco.getDetectedAnimals(host + item, mqh);

        Assert.assertEquals(1, animalInfos.size());
        Assert.assertEquals("gazelle", animalInfos.get(0).getSpecies());

    }


    @Test
    public void testGetSentiment() throws Exception {

        String host = "http://demo1.mico-project.eu:8080/marmotta/";
        String item = "23f2e15e-9d4c-4313-ade3-813ec0b48c0b";

        List<String> itemList = new ArrayList<>();
        itemList.add(host + item);

        SentimentResult sentiment = zooReco.getChatSentiment(itemList);
        Assert.assertEquals(SentimentResult.POSITIVE, sentiment);

    }


    @Test
    public void testGetSentiment_invalidItem() throws Exception {

        String host = "http://demo1.mico-project.eu:8080/marmotta/";
        String item = "23f2e15e-9d4c-1234-ade3-813ec0b48c0b";

        List<String> itemList = new ArrayList<>();
        itemList.add(host + item);

        SentimentResult sentiment = zooReco.getChatSentiment(itemList);
        Assert.assertEquals(SentimentResult.NEUTRAL, sentiment);

    }
}
