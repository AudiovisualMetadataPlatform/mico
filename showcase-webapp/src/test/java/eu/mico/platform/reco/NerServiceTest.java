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