package eu.mico.platform.uc.zooniverse;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.testutils.TestServer;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.uc.zooniverse.webservices.TextAnalysisWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TextAnalysisWebServiceTest {

    private static TestServer server;

    private static ArrayList<Content> contents = new ArrayList<>();

    @BeforeClass
    public static void init() throws Exception {

        //mock environment


        //init webservice
        TextAnalysisWebService textAnalysisWebService = new TextAnalysisWebService(
                mockEvenmanager(),
                mockBroker(),
                "http://localhost:8080/marmotta");

        //init server
        server = new TestServer();

        server.addWebservice(textAnalysisWebService);

        server.start();
    }

    @AfterClass
    public static void shudown() throws Exception {
        server.stop();
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

    private static MICOBroker mockBroker() {
        MICOBroker broker = mock(MICOBroker.class);
        Map states = mock(Map.class);
        ContentItemState state = mock(ContentItemState.class);
        when(state.isFinalState()).thenReturn(true);
        when(states.get(any())).thenReturn(state);
        when(broker.getStates()).thenReturn(states);
        return broker;
    }

    private static EventManager mockEvenmanager() throws RepositoryException, IOException {
        EventManager eventManager = mock(EventManager.class);
        PersistenceService persistenceService = mockPersistenceService();
        when(eventManager.getPersistenceService()).thenReturn(persistenceService);
        return eventManager;
    }

    private static PersistenceService mockPersistenceService() throws RepositoryException, IOException {
        PersistenceService persistenceService = mock(PersistenceService.class);
        ContentItem contentItem = mockContentItem();
        when(persistenceService.createContentItem()).thenReturn(contentItem);
        return persistenceService;
    }

    private static ContentItem mockContentItem() throws RepositoryException, IOException {
        ContentItem contentItem = mock(ContentItem.class);
        URI uri = mock(URI.class);
        Content content1 = mockContent();
        Content content2 = mockContent();
        when(uri.stringValue()).thenReturn("http://localhost/contentitem/1");
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

}
