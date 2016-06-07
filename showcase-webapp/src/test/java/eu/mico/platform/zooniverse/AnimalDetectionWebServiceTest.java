package eu.mico.platform.zooniverse;

import com.google.common.io.Resources;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.zooniverse.testutils.TestServer;
import eu.mico.platform.zooniverse.util.BrokerServices;
import eu.mico.platform.zooniverse.util.ItemData;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;

import static org.mockito.Matchers.anyObject;

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

        //init webservice with mocked environment
        AnimalDetectionWebService animalDetectionWebService = new AnimalDetectionWebService(
                mockEvenmanager(),
                "http://micobox154:8080/marmotta",
                mockBroker()
        );

        //init in memory repository
        repository = initializeRepository();
        connection = repository.getConnection();

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

    private static BrokerServices mockBroker() throws IOException {
        BrokerServices brokerSvc = Mockito.mock(BrokerServices.class);
        //Mockito.when(brokerSvc.getItemData(org.mockito.Matchers.<String>any())).thenReturn(new ItemData(Collections.singletonMap("finished", (Object) "true")));
        //Mockito.when(brokerSvc.getServices()).thenReturn(Collections.singletonList(Collections.singletonMap("uri", "http://www.mico-project.eu/services/ner-text")));
        return brokerSvc;
    }

    private static EventManager mockEvenmanager() throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {
        EventManager eventManager = Mockito.mock(EventManager.class);
        PersistenceService persistenceService = mockPersistenceService();
        Mockito.when(eventManager.getPersistenceService()).thenReturn(persistenceService);
        return eventManager;
    }

    private static PersistenceService mockPersistenceService() throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {
        PersistenceService persistenceService = Mockito.mock(PersistenceService.class);
        //Mockito.when(persistenceService.createItem()).thenReturn(item);
        Mockito.when(persistenceService.getItem(org.mockito.Matchers.<URI>any())).thenAnswer(new Answer<Item>() {
            @Override
            public Item answer(InvocationOnMock invocationOnMock) throws Throwable {
                URI uri = (URI) invocationOnMock.getArguments()[0];
                return mockItem(uri);
            }
        });
        return persistenceService;
    }

    private static Item mockItem(URI uri) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getURI()).thenReturn(uri);
        ObjectConnection connection = mockObjectConnection();
        Mockito.when(item.getObjectConnection()).thenReturn(connection);
        return item;
    }

    private static ObjectConnection mockObjectConnection() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ObjectConnection rep = Mockito.mock(ObjectConnection.class);
        Mockito.when(rep.prepareTupleQuery(anyObject())).thenAnswer(new Answer<TupleQuery>() {
            @Override
            public TupleQuery answer(InvocationOnMock invocationOnMock) throws Throwable {
                return connection.prepareTupleQuery(QueryLanguage.SPARQL,(String)invocationOnMock.getArguments()[0]);
            }
        });

        return rep;
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
