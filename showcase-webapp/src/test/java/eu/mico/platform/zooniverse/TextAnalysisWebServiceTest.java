package eu.mico.platform.zooniverse;

import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.zooniverse.testutils.TestServer;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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


/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisWebServiceTest {

    private static TestServer server;

    private static Repository repository;

    private static Part part;

    @BeforeClass
    public static void init() throws Exception {

        //init webservice with mocked environment
        TextAnalysisWebService textAnalysisWebService = new TextAnalysisWebService(
                mockEvenmanager(),
                mockBroker());

        //init in memory repository
        repository = initializeRepository();

        //init server
        server = new TestServer();

        server.addWebservice(textAnalysisWebService);

        server.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
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
                .body("id", Matchers.equalTo("d9347936-30ac-42f7-a0d5-4a2bfd908256"))
                .body("status", Matchers.equalTo("submitted"));

        //test content parts
        Assert.assertNotNull(part);
        Assert.assertEquals("This is a text", new String(((ByteArrayOutputStream) part.getAsset().getOutputStream()).toByteArray()));
    }

    @Test
    public void testGetResult() {
        String itemID = "d9347936-30ac-42f7-a0d5-4a2bfd908256";

        com.jayway.restassured.RestAssured.when().
                get(server.getUrl() + "zooniverse/textanalysis/" + itemID).
                then().
                assertThat()
                .body("id", Matchers.equalTo("d9347936-30ac-42f7-a0d5-4a2bfd908256"))
                .body("sentiment", Matchers.equalTo(-0.26978558F))
                .body("topics.size()", Matchers.equalTo(3))
                .body("entities.size()", Matchers.equalTo(20))
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
        Mockito.when(uri.stringValue()).thenReturn("http://localhost/item/d9347936-30ac-42f7-a0d5-4a2bfd908256");
        Item item = Mockito.mock(Item.class);
        Part part = mockContent();
        Mockito.when(item.createPart(new URIImpl("http://mico-project.eu/part-mock"))).thenReturn(part);
        Mockito.when(item.getURI()).thenReturn(uri);
        return item;
    }

    private static Item mockItem(URI uri) throws RepositoryException, IOException {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getURI()).thenReturn(uri);
        return item;
    }

    private static Part mockContent() throws IOException, RepositoryException {
        part = Mockito.mock(Part.class);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(part.getAsset().getOutputStream()).thenReturn(os);
        return part;
    }

    private static Repository initializeRepository() throws RepositoryException, IOException, RDFParseException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        //import file
        URL file = Resources.getResource("text_analysis/lmf-export-20151005-091437.ttl");

        RepositoryConnection c = repository.getConnection();
        repository.getConnection().add(file.openStream(), "http://mico-platform:8080/marmotta", RDFFormat.TURTLE);
        c.close();

        return repository;
    }

}
