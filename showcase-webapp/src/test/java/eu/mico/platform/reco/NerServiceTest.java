package eu.mico.platform.reco;

import com.jayway.restassured.RestAssured;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.reco.Resources.NERQuery;
import eu.mico.platform.testutils.TestServer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ...
 * <p/>
 * Author: Thomas Koellmer
 */
public class NerServiceTest {

    static private TestServer server;

    @BeforeClass
    public static void init() throws Exception {

        MICOQueryHelperMMM mqh = NERQuery.getMicoQueryHelper();

        NerService nerTestService = new NerService(
                mqh
        );

        //init server
        server = new TestServer();

        server.addWebservice(nerTestService);

        server.start();

    }

    @Test
    public void getLinkedEntities() throws Exception {

    }

    @Test
    public void getTranscript() throws Exception {

        String filename = "p360 - GREENPEACE THE OCEAN.mp4";

        String json = RestAssured.
                given().log().all().
                when().
                get(server.getUrl() + "analyzed/" + filename + "/transcript")
                .body().asString();

        System.out.println(json);


    }

}