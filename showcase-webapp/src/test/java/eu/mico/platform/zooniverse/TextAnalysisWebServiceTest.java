package eu.mico.platform.zooniverse;

import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.zooniverse.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;


/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisWebServiceTest {

    private static String itemUrlString = "http://localhost/mem/56b90661-14b5-4fe6-b2c8-af5357f729a9";

    private static TestServer server;

    private static Repository repository;

    private static RepositoryConnection connection;

    @BeforeClass
    public static void init() throws Exception {

        //init webservice with mocked environment
        TextAnalysisWebService textAnalysisWebService = new TextAnalysisWebService(
                mockEvenmanager(),
                mockBroker());

        //init in memory repository
        repository = initializeRepository();
        connection = repository.getConnection();

        //init server
        server = new TestServer();

        server.addWebservice(textAnalysisWebService);

        server.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
        connection.close();
        repository.shutDown();
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
                .body("id", Matchers.equalTo(itemUrlString))
                .body("status", Matchers.equalTo("submitted"));

    }

    @Test
    public void testGetResult() {
        com.jayway.restassured.RestAssured.when().
                get(server.getUrl() + "zooniverse/textanalysis/" + itemUrlString).
                then().
                assertThat()
                .body("id", Matchers.equalTo(itemUrlString))
                .body("sentiment", Matchers.equalTo(-0.19203712F))
                .body("topics.size()", Matchers.equalTo(3))
                .body("entities.size()", Matchers.equalTo(39))
                .body("status", Matchers.equalTo("finished"));
    }

    private static MICOBroker mockBroker() {
        MICOBroker broker = Mockito.mock(MICOBroker.class);
        Map states = Mockito.mock(Map.class);
        ItemState state = Mockito.mock(ItemState.class);
        ServiceGraph serviceGraph = Mockito.mock(ServiceGraph.class);
        Mockito.when(serviceGraph.getDescriptorURIs()).thenReturn(Collections.<URI>singleton(new URIImpl("http://www.mico-project.eu/services/ner-text")));
        Mockito.when(state.isFinalState()).thenReturn(true);
        Mockito.when(states.get(org.mockito.Matchers.any())).thenReturn(state);
        Mockito.when(broker.getStates()).thenReturn(states);
        Mockito.when(broker.getDependencies()).thenReturn(serviceGraph);
        return broker;
    }

    private static EventManager mockEvenmanager() throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {
        EventManager eventManager = Mockito.mock(EventManager.class);
        PersistenceService persistenceService = mockPersistenceService();
        Mockito.when(eventManager.getPersistenceService()).thenReturn(persistenceService);
        return eventManager;
    }

    private static PersistenceService mockPersistenceService() throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {
        PersistenceService persistenceService = Mockito.mock(PersistenceService.class);
        Item item = mockCreateItem();
        Mockito.when(persistenceService.createItem()).thenReturn(item);
        Mockito.when(persistenceService.getItem(org.mockito.Matchers.<URI>any())).thenAnswer(new Answer<Item>() {
            @Override
            public Item answer(InvocationOnMock invocationOnMock) throws Throwable {
                URI uri = (URI) invocationOnMock.getArguments()[0];
                return mockItem(uri);
            }
        });
        return persistenceService;
    }


    private static Item mockCreateItem() throws RepositoryException, IOException {
        URI uri = Mockito.mock(URI.class);
        Asset a = createAsset();
        Mockito.when(uri.stringValue()).thenReturn(itemUrlString);
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getURI()).thenReturn(uri);
        Mockito.when(item.getAsset()).thenReturn(a);
        return item;
    }

    public static Asset createAsset() throws IOException {
        OutputStream os = new ByteArrayOutputStream();
        Asset a = Mockito.mock(Asset.class);
        Mockito.when(a.getOutputStream()).thenReturn(os);
        return a;
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
        URL file = Resources.getResource("text_analysis/kiwi-export-20150429.ttl");

        RepositoryConnection c = repository.getConnection();

        repository.getConnection().add(file.openStream(), "http://mico-platform:8080/marmotta", RDFFormat.TURTLE);
        c.close();

        return repository;
    }

}
