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

package eu.mico.platform.zooniverse;

import com.jayway.restassured.RestAssured;
import eu.mico.platform.testutils.Mockups;
import eu.mico.platform.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static eu.mico.platform.testutils.Mockups.mockBroker;
import static eu.mico.platform.testutils.Mockups.mockEvenmanager;


/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisWebServiceTest {

    private static String localName = "c3cf9a33-88ae-428f-8eb1-985dca5c3b97";
    private static String itemUrlString = "http://mico-platform.salzburgresearch.at:8080/marmotta/" + localName;

    private static TestServer server;

    private static Repository repository;

    private static RepositoryConnection connection;

    @BeforeClass
    public static void init() throws Exception {

        //init in memory repository
        repository = Mockups.initializeRepository("text_analysis/mico-export-20160523.ttl");
        connection = repository.getConnection();


        //init webservice with mocked environment
        TextAnalysisWebService textAnalysisWebService = new TextAnalysisWebService(
                mockEvenmanager(connection),
                "http://mico-platform.salzburgresearch.at:8080/marmotta",
                mockBroker());


        //init server
        server = new TestServer();

        server.addWebservice(textAnalysisWebService);

        server.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
        connection.close();
        //repository.shutDown();
    }

    @Test
    public void testUpload() throws IOException, RepositoryException {
        RestAssured.given().
                contentType(MediaType.APPLICATION_JSON).
                content("{\"comment\":\"This is a text\"}").
                when().
                post(server.getUrl() + "zooniverse/textanalysis").
                then().
                assertThat()
                .body("id", Matchers.equalTo(localName))
                .body("status", Matchers.equalTo("submitted"));

    }

    @Test
    public void testCheckExtractorName() throws Exception {

        final String testName = "mico-extractor-named-entity-recognizer-3.1.0-RedlinkNER";
        final String testName_wrongversion = "mico-extractor-named-entity-recognizer-3.8.0-RedlinkNER";

        Assert.assertTrue(TextAnalysisWebService.checkExtractorName(testName, false));
        Assert.assertTrue(TextAnalysisWebService.checkExtractorName(testName_wrongversion, false));
        Assert.assertTrue(TextAnalysisWebService.checkExtractorName(testName, true));
        Assert.assertFalse(TextAnalysisWebService.checkExtractorName(testName_wrongversion, true));

    }

    @Test
    public void testGetResult() {
        com.jayway.restassured.RestAssured.when().
                get(server.getUrl() + "zooniverse/textanalysis/" + localName).
                then().
                assertThat()
                .body("id", Matchers.equalTo(localName))
                .body("sentiment", Matchers.equalTo(0.18698756F))
                .body("topics.size()", Matchers.equalTo(0))
                .body("entities.size()", Matchers.equalTo(5))
                .body("status", Matchers.equalTo("finished"));
    }


}
