package eu.mico.platform.zooniverse;

import com.google.common.io.Resources;
import eu.mico.platform.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.net.URL;

import static eu.mico.platform.testutils.Mockups.mockBroker;
import static eu.mico.platform.testutils.Mockups.mockEvenmanager;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 27.05.16.
 */
public class AnimalDetectionWebServiceTest {

    private static TestServer server;

    private static Repository repository;

    private static RepositoryConnection connection;

    private static String localName = "f665b379-7782-488b-a950-5c054f5cb900";
    private static String itemUrlString = "http://micobox154:8080/marmotta/" + localName;

    @BeforeClass
    public static void init() throws Exception {

        //init in memory repository
        repository = initializeRepository();
        connection = repository.getConnection();

        //init webservice with mocked environment
        AnimalDetectionWebService animalDetectionWebService = new AnimalDetectionWebService(
                mockEvenmanager(connection),
                "http://micobox154:8080/marmotta",
                mockBroker()
        );



        //init server
        server = new TestServer();

        server.addWebservice(animalDetectionWebService);

        server.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
        connection.close();
        repository.shutDown();
    }

    @Test
    public void testGetResult() {
        com.jayway.restassured.RestAssured.when().
                get(server.getUrl() + "zooniverse/animaldetection/" + localName).
                then().
                assertThat()
                .body("id", Matchers.equalTo(localName))
                .body("objectsFound", Matchers.equalTo(11))
                .body("processingBegin", Matchers.equalTo("2016-05-27 16:10:19.194"))
                .body("status", Matchers.equalTo("finished"));
    }


    private static Repository initializeRepository() throws RepositoryException, IOException, RDFParseException {

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        //import file
        URL file = Resources.getResource("image_analysis/f665b379-7782-488b-a950-5c054f5cb900-export-20160527-161104.ttl");

        RepositoryConnection c = repository.getConnection();

        repository.getConnection().add(file.openStream(), "http://micobox154:8080/marmotta", RDFFormat.TURTLE);
        c.close();

        return repository;
    }
}
