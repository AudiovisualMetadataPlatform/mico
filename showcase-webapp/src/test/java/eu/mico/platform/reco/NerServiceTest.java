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

import com.jayway.restassured.RestAssured;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.testutils.MqhMocks;
import eu.mico.platform.testutils.TestServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.path.json.JsonPath.from;

/**
 * ...
 * <p/>
 * Author: Thomas Koellmer
 */
public class NerServiceTest {

    static private TestServer server;

    @BeforeClass
    public static void init() throws Exception {

        MICOQueryHelperMMM mqh = MqhMocks.mockMicoQueryHelper();

        NerService nerTestService = new NerService(mqh);

        //init server
        server = new TestServer();

        server.addWebservice(nerTestService);

        server.start();

    }


    @Test
    public void getTranscript() throws Exception {

        String filename = "p360 - GREENPEACE THE OCEAN.mp4";

        String json = RestAssured.
                when().
                get(server.getUrl() + "ner/" + filename + "/transcript")
                .body().asString();

        Assert.assertEquals("label", from(json).get("transcript[0].text"));


    }

}