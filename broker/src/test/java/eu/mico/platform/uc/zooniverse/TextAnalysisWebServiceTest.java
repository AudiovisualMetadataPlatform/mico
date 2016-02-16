package eu.mico.platform.uc.zooniverse;

import com.google.common.io.Resources;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.broker.testutils.TestServer;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.uc.zooniverse.webservices.TextAnalysisWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                mockBroker(),
                "http://mico-platform:8080/marmotta");

        //init in memory repository
        repository = initializeRepository();

        //init server
        server = new TestServer();

        server.addWebservice(textAnalysisWebService);

        server.start();
    }

    @AfterClass
    public static void shudown() throws Exception {
        server.stop();
        repository.shutDown();
    }

    @Test
    public void testUpload() throws IOException {
        given().
                contentType(MediaType.APPLICATION_JSON).
                content("{\"comment\":\"This is a text\"}").
                when().
                post(server.getUrl() + "zooniverse/textanalysis").
                then().
                assertThat()
                .body("id", equalTo("d9347936-30ac-42f7-a0d5-4a2bfd908256"))
                    .body("status", equalTo("submitted"));

        //test part
        Assert.assertNotNull(part);
        Assert.assertEquals("This is a text", new String(((ByteArrayOutputStream) part.getOutputStream()).toByteArray()));
    }

    @Test
    public void testGetResult() {
        String itemId = "d9347936-30ac-42f7-a0d5-4a2bfd908256";

        com.jayway.restassured.RestAssured.when().
                get(server.getUrl() + "zooniverse/textanalysis/" + itemId).
        then().
                assertThat()
                .body("id", equalTo("d9347936-30ac-42f7-a0d5-4a2bfd908256"))
                .body("sentiment", equalTo(-0.26978558F))
                .body("topics.size()", equalTo(3))
                .body("entities.size()", equalTo(20))
                .body("status", equalTo("finished"));
    }

    private static MICOBroker mockBroker() {
        MICOBroker broker = mock(MICOBroker.class);
        Map states = mock(Map.class);
        ItemState state = mock(ItemState.class);
        ServiceGraph serviceGraph = mock(ServiceGraph.class);
        when(serviceGraph.getDescriptorURIs()).thenReturn(Collections.<URI>singleton(new URIImpl("http://www.mico-project.eu/services/ner-text")));
        when(state.isFinalState()).thenReturn(true);
        when(states.get(any())).thenReturn(state);
        when(broker.getStates()).thenReturn(states);
        when(broker.getDependencies()).thenReturn(serviceGraph);
        return broker;
    }

    private static EventManager mockEvenmanager() throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {
        EventManager eventManager = mock(EventManager.class);
        PersistenceService persistenceService = mockPersistenceService();
        when(eventManager.getPersistenceService()).thenReturn(persistenceService);
        return eventManager;
    }

    private static PersistenceService mockPersistenceService() throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {
        PersistenceService persistenceService = mock(PersistenceService.class);
        Item item = mockCreateItem();
        Metadata metadata = mockMetadata();
        when(persistenceService.createItem()).thenReturn(item);
        when(persistenceService.getItem(Matchers.<URI>any())).thenAnswer(new Answer<Item>(){
            @Override
            public Item answer(InvocationOnMock invocationOnMock) throws Throwable {
                URI uri = (URI) invocationOnMock.getArguments()[0];
                return mockItem(uri);
            }
        });
        when(persistenceService.getMetadata()).thenReturn(metadata);
        return persistenceService;
    }

    private static Metadata mockMetadata() throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        Metadata metadata = mock(Metadata.class);
        when(metadata.query(anyString())).thenAnswer(new Answer<TupleQueryResult>() {
            @Override
            public TupleQueryResult answer(InvocationOnMock invocation) throws Throwable {
                String query = (String) invocation.getArguments()[0];

                RepositoryConnection connection = repository.getConnection();
                TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                TupleQueryResult result = q.evaluate();

                return result;
            }
        });
        return metadata;
    }

    private static Item mockCreateItem() throws RepositoryException, IOException {
        URI uri = mock(URI.class);
        when(uri.stringValue()).thenReturn("http://localhost/contentitem/d9347936-30ac-42f7-a0d5-4a2bfd908256");
        Item item = mock(Item.class);
        Part part = mockPart();
        when(item.createPart()).thenReturn(part);
        when(item.getID()).thenReturn("d9347936-30ac-42f7-a0d5-4a2bfd908256");
        when(item.getURI()).thenReturn(uri);
        return item;
    }

    private static Item mockItem(URI uri) throws RepositoryException, IOException {
        Item item = mock(Item.class);
        when(item.getID()).thenReturn("d9347936-30ac-42f7-a0d5-4a2bfd908256");
        when(item.getURI()).thenReturn(uri);
        return item;
    }

    private static Part mockPart() throws IOException {
        part = mock(Part.class);
        OutputStream os = new ByteArrayOutputStream();
        when(part.getOutputStream()).thenReturn(os);
        return part;
    }

    private static Repository initializeRepository() throws RepositoryException, IOException, RDFParseException {
        Repository repository = new SailRepository( new MemoryStore());
        repository.initialize();

        //import file
        URL file = Resources.getResource("text_analysis/lmf-export-20151005-091437.ttl");

        RepositoryConnection c = repository.getConnection();
        repository.getConnection().add(file.openStream(),"http://mico-platform:8080/marmotta", RDFFormat.TURTLE);
        c.close();

        return repository;
    }

}
