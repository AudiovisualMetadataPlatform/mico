package eu.mico.platform.testutils;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.zooniverse.util.BrokerServices;
import eu.mico.platform.zooniverse.util.ItemData;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

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
        Mockito.when(brokerSvc.getItemData(org.mockito.Matchers.<String>any())).thenReturn(new ItemData(Collections.singletonMap("finished", (Object) "true")));
        Mockito.when(brokerSvc.getServices()).thenReturn(Collections.singletonList(Collections.singletonMap("uri", "http://www.mico-project.eu/services/ner-text")));
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


}
