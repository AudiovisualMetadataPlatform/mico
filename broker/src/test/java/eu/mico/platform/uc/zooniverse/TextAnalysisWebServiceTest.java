package eu.mico.platform.uc.zooniverse;

import com.google.common.io.Resources;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.testutils.TestServer;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.uc.zooniverse.webservices.TextAnalysisWebService;
import org.eclipse.jetty.webapp.MetaData;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.URI;
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
import java.util.ArrayList;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
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

    private static ArrayList<Content> contents = new ArrayList<>();

    @BeforeClass
    public static void init() throws Exception {

        //init webservice with mocked environment
        TextAnalysisWebService textAnalysisWebService = new TextAnalysisWebService(
                mockEvenmanager(),
                mockBroker(),
                "http://localhost:8080/marmotta");

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
                content("{\"id\":\"1\",\"comments\":[\"This is text 1\",\"This is text 2\"]}").
        when().
                post(server.getUrl() + "zooniverse/textanalysis").
        then().
                assertThat()
                    .body("id", equalTo("1"))
                    .body("link", equalTo("http://localhost/contentitem/1"))
                    .body("status", equalTo("submitted"));

        //test content parts
        Assert.assertEquals(2, contents.size());
        Assert.assertEquals("This is text 1", new String(((ByteArrayOutputStream)contents.get(0).getOutputStream()).toByteArray()));
        Assert.assertEquals("This is text 2", new String(((ByteArrayOutputStream) contents.get(1).getOutputStream()).toByteArray()));
    }

    @Test
    public void testGetResult() {
        String contentItemId = "http://mico-platform:8080/marmotta/d9347936-30ac-42f7-a0d5-4a2bfd908256";

        given().
                param("contentItemID", contentItemId).
        when().
                get(server.getUrl() + "zooniverse/textanalysis").
        then().
                assertThat()
                .body("id", equalTo("1"))
                .body("sentiment", equalTo(-0.26978558F))
                .body("topics.size()", equalTo(3))
                .body("entities.size()", equalTo(20))
                .body("status", equalTo("finished"));
    }

    private static MICOBroker mockBroker() {
        MICOBroker broker = mock(MICOBroker.class);
        Map states = mock(Map.class);
        ContentItemState state = mock(ContentItemState.class);
        when(state.isFinalState()).thenReturn(true);
        when(states.get(any())).thenReturn(state);
        when(broker.getStates()).thenReturn(states);
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
        ContentItem contentItem = mockContentItem();
        Metadata metadata = mockMetadata();
        when(persistenceService.createContentItem()).thenReturn(contentItem);
        when(persistenceService.getContentItem(Matchers.<URI>any())).thenAnswer(new Answer<ContentItem>(){
            @Override
            public ContentItem answer(InvocationOnMock invocationOnMock) throws Throwable {
                URI uri = (URI) invocationOnMock.getArguments()[0];
                return mockContentItem(uri);
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

    private static ContentItem mockContentItem() throws RepositoryException, IOException {
        URI uri = mock(URI.class);
        when(uri.stringValue()).thenReturn("http://localhost/contentitem/1");
        return mockContentItem(uri);
    }

    private static ContentItem mockContentItem(URI uri) throws RepositoryException, IOException {
        ContentItem contentItem = mock(ContentItem.class);
        Content content1 = mockContent();
        Content content2 = mockContent();
        when(contentItem.createContentPart()).thenReturn(content1).thenReturn(content2);
        when(contentItem.getID()).thenReturn("1");
        when(contentItem.getURI()).thenReturn(uri);
        return contentItem;
    }

    private static Content mockContent() throws IOException {
        Content content = mock(Content.class);
        OutputStream os = new ByteArrayOutputStream();
        when(content.getOutputStream()).thenReturn(os);
        contents.add(content);
        return content;
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
