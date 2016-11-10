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
package eu.mico.marmotta.webservices;

import com.jayway.restassured.RestAssured;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

/**
 * Contextual SPARQL Web Service Tests
 *
 * @author Sergio Fernández
 */
public class ContextualSparqlWebServiceTest {

    private static Logger log = LoggerFactory.getLogger(ContextualSparqlWebServiceTest.class);

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void setUp() throws MarmottaImportException, URISyntaxException {
        marmotta = new JettyMarmotta("/marmotta", ContextualSparqlWebService.class);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = marmotta.getPort();
        RestAssured.basePath = marmotta.getContext();
    }

    @AfterClass
    public static void tearDown() {
        try {
            marmotta.shutdown();
        } catch (Exception e) {
            log.error("Error shutting down marmotta: {}", e.getMessage());
        }
    }

    @Test
    public void testEndpoint() throws MarmottaException {
        RestAssured.expect().
            log().ifError().
            statusCode(200).
        given().
            queryParam("query", "SELECT * WHERE { ?s ?p ?o }").
        when().
            get("/foo/" + ContextualSparqlWebService.PATH + ContextualSparqlWebService.SELECT);
    }

}
