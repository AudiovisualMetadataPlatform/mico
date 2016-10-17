package eu.mico.platform.testutils;

import com.google.common.io.Resources;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.zooniverse.util.BrokerServices;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyObject;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Mockups {

    private static RepositoryConnection connection;

    private static String localName = "c3cf9a33-88ae-428f-8eb1-985dca5c3b97";
    private static String itemUrlString = "http://mico-platform.salzburgresearch.at:8080/marmotta/" + localName;


    public static EventManager mockEvenmanager(RepositoryConnection repositoryConnection) throws RepositoryException, IOException, QueryEvaluationException, MalformedQueryException {

        connection = repositoryConnection;

        EventManager eventManager = Mockito.mock(EventManager.class);
        PersistenceService persistenceService = mockPersistenceService();
        Mockito.when(eventManager.getPersistenceService()).thenReturn(persistenceService);
        return eventManager;
    }

    public static BrokerServices mockBroker() throws IOException {
        BrokerServices brokerSvc = Mockito.mock(BrokerServices.class);
        eu.mico.platform.zooniverse.util.Item itemStatus = new eu.mico.platform.zooniverse.util.Item();
        itemStatus.setFinished("true");


        Map<String, String> serviceMap = new HashMap<>();
        serviceMap.put("calls", "0");
        serviceMap.put("provides", "application/x-mico-rdf");
        serviceMap.put("name", "mico-extractor-named-entity-recognizer-3.1.0-RedlinkNER");
        serviceMap.put("language", "Java");
        serviceMap.put("time", "2016-09-08T15:07:46.922Z");
        serviceMap.put("uri", "http://www.mico-project.org/services/mico-extractor-named-entity-recognizer-3.1.0-RedlinkNER");
        serviceMap.put("requires", "text/plain");


        Mockito.when(brokerSvc.getItem(org.mockito.Matchers.<String>any())).thenReturn(itemStatus);
        Mockito.when(brokerSvc.getServices()).thenReturn(
                Collections.singletonList(
                        Collections.unmodifiableMap(serviceMap)
                ));
        return brokerSvc;
    }

    private static Asset mockAsset() throws IOException {
        OutputStream os = new ByteArrayOutputStream();
        Asset a = Mockito.mock(Asset.class);
        Mockito.when(a.getOutputStream()).thenReturn(os);
        return a;
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

    private static Item mockItem(URI uri) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getURI()).thenReturn(uri);
        ObjectConnection connection = mockObjectConnection();
        Mockito.when(item.getObjectConnection()).thenReturn(connection);
        return item;
    }

    private static Item mockCreateItem() throws RepositoryException, IOException {
        URI uri = Mockito.mock(URI.class);
        Asset a = mockAsset();
        Mockito.when(uri.stringValue()).thenReturn(itemUrlString);
        Mockito.when(uri.getLocalName()).thenReturn(localName);
        Item item = Mockito.mock(Item.class);
        Mockito.when(item.getURI()).thenReturn(uri);
        Mockito.when(item.getAsset()).thenReturn(a);
        return item;
    }


    private static ObjectConnection mockObjectConnection() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ObjectConnection rep = Mockito.mock(ObjectConnection.class);
        Mockito.when(rep.prepareTupleQuery(anyObject())).thenAnswer(new Answer<TupleQuery>() {
            @Override
            public TupleQuery answer(InvocationOnMock invocationOnMock) throws Throwable {
                return connection.prepareTupleQuery(QueryLanguage.SPARQL, (String) invocationOnMock.getArguments()[0]);
            }
        });

        return rep;
    }


    public static Repository initializeRepository(String turtleFile) throws RepositoryException, IOException, RDFParseException {

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        //import file
        URL file = Resources.getResource(turtleFile);

        RepositoryConnection c = repository.getConnection();

        repository.getConnection().add(file.openStream(), "http://mico-platform:8080/marmotta", RDFFormat.TURTLE);
        c.close();

        return repository;
    }
}
