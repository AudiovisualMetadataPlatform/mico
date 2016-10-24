package eu.mico.platform.reco;

import com.github.anno4j.Anno4j;
import com.jayway.restassured.RestAssured;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.testutils.Mockups;
import eu.mico.platform.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

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
public class RecoWebServiceTest {


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
    @Ignore
    public void testIsDebated() throws Exception {

        RestAssured.
                when()
                .get(server.getUrl() + "reco/zoo/12345/is_debated")
                .then()
                .assertThat()
                .statusCode(200)
                .body("reco_id", Matchers.equalTo("12345"))
                .body("score", Matchers.greaterThanOrEqualTo(0f))
                .body("score", Matchers.lessThan(1f));

    }

}
