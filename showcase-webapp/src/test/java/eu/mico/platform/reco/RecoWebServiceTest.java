package eu.mico.platform.reco;

import com.jayway.restassured.RestAssured;
import eu.mico.platform.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;

import static eu.mico.platform.testutils.Mockups.mockEvenmanager;

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


    @BeforeClass
    public static void init() throws Exception {


        RecoWebService recoWebService = new RecoWebService(
                mockEvenmanager(null),
                "http://mico-platform:8080/marmotta"
        );

        //init server
        server = new TestServer();

        server.addWebservice(recoWebService);

        server.start();
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
                when().
                get(server.getUrl() + "reco/zoo/12345/discussion/relatedsubjects").then()
                .assertThat()
                .statusCode(200)
                .body("reco_id", Matchers.equalTo("12345"))
                .body("talk_analysis", Matchers.equalTo("Lion"))
                .body("user_competence", Matchers.equalTo(0.8f))
                .body("image_analysis", Matchers.equalTo("Ostrich"))
                .body("related_subject", Matchers.equalTo("DSG0000111"))
                .body("confidence", Matchers.equalTo(0.3f));

    }
}
