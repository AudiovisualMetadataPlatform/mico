package eu.mico.platform.zooniverse;

import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
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


/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisWebServiceTest {

    private static TestServer server;

    private static Repository repository;

    private static Content content;

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
        Assert.assertNotNull(content);
        Assert.assertEquals("This is a text", new String(((ByteArrayOutputStream)content.getOutputStream()).toByteArray()));
    }

    @Test
    public void testGetResult() {
        String contentItemId = "d9347936-30ac-42f7-a0d5-4a2bfd908256";

        com.jayway.restassured.RestAssured.when().
                get(server.getUrl() + "zooniverse/textanalysis/" + contentItemId).
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
        ContentItemState state = Mockito.mock(ContentItemState.class);
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
        ContentItem contentItem = mockCreateContentItem();
        Metadata metadata = mockMetadata();
        Mockito.when(persistenceService.createContentItem()).thenReturn(contentItem);
        Mockito.when(persistenceService.getContentItem(org.mockito.Matchers.<URI>any())).thenAnswer(new Answer<ContentItem>() {
            @Override
            public ContentItem answer(InvocationOnMock invocationOnMock) throws Throwable {
                URI uri = (URI) invocationOnMock.getArguments()[0];
                return mockContentItem(uri);
            }
        });
        Mockito.when(persistenceService.getMetadata()).thenReturn(metadata);
        return persistenceService;
    }

    private static Metadata mockMetadata() throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        Metadata metadata = Mockito.mock(Metadata.class);
        Mockito.when(metadata.query(org.mockito.Matchers.anyString())).thenAnswer(new Answer<TupleQueryResult>() {
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

    private static ContentItem mockCreateContentItem() throws RepositoryException, IOException {
        URI uri = Mockito.mock(URI.class);
        Mockito.when(uri.stringValue()).thenReturn("http://localhost/contentitem/d9347936-30ac-42f7-a0d5-4a2bfd908256");
        ContentItem contentItem = Mockito.mock(ContentItem.class);
        Content content = mockContent();
        Mockito.when(contentItem.createContentPart()).thenReturn(content);
        Mockito.when(contentItem.getID()).thenReturn("d9347936-30ac-42f7-a0d5-4a2bfd908256");
        Mockito.when(contentItem.getURI()).thenReturn(uri);
        return contentItem;
    }

    private static ContentItem mockContentItem(URI uri) throws RepositoryException, IOException {
        ContentItem contentItem = Mockito.mock(ContentItem.class);
        Mockito.when(contentItem.getID()).thenReturn("d9347936-30ac-42f7-a0d5-4a2bfd908256");
        Mockito.when(contentItem.getURI()).thenReturn(uri);
        return contentItem;
    }

    private static Content mockContent() throws IOException {
        content = Mockito.mock(Content.class);
        OutputStream os = new ByteArrayOutputStream();
        Mockito.when(content.getOutputStream()).thenReturn(os);
        return content;
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
