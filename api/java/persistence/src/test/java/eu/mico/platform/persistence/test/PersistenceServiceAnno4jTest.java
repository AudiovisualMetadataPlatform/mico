/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.persistence.test;

import com.github.anno4j.querying.QueryService;
import com.google.common.collect.Iterables;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;

import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PersistenceServiceAnno4jTest {

    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        persistenceService = new PersistenceServiceAnno4j();
    }

    @Test
    public void createItemTest() throws RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException {
        Item item = persistenceService.createItem();
        assertNotNull(item.getURI());
        assertNotNull(item.getSerializedAt());

        item.setSyntacticalType("syntactical-type");
        item.setSemanticType("semantic-type");
        
        QueryService query = persistenceService.createQuery(item.getURI());
        List<ItemMMM> itemMMMs = query.execute(ItemMMM.class);
        assertEquals(1, itemMMMs.size());
        ItemMMM itemMMM = itemMMMs.get(0);
        assertEquals(item.getSerializedAt(), itemMMM.getSerializedAt());
        assertEquals(item.getSemanticType(), itemMMM.getSemanticType());
        assertEquals(item.getSyntacticalType(), itemMMM.getSyntacticalType());
    }

    @Test
    public void getItemTest() throws RepositoryException {
        Item initialItem = persistenceService.createItem();
        initialItem.setSyntacticalType("syntactical-type");
        initialItem.setSemanticType("semantic-type");

        Item retrievedItem = persistenceService.getItem(initialItem.getURI());
        assertEquals(initialItem.getURI(), retrievedItem.getURI());
        assertEquals(initialItem.getSerializedAt(), retrievedItem.getSerializedAt());
        assertEquals(initialItem.getSemanticType(), retrievedItem.getSemanticType());
        assertEquals(initialItem.getSyntacticalType(), retrievedItem.getSyntacticalType());
    }

    @Test
    public void getItemsTest() throws RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException {
        QueryService query = persistenceService.createQuery(null);
        int initialItemCount = query.execute(ItemMMM.class).size();

        persistenceService.createItem();
        persistenceService.createItem();

        int retrievedSize = Iterables.size(persistenceService.getItems());

        assertEquals(initialItemCount + 2, retrievedSize);
    }

    @Test
    public void subGraphTest() throws RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException {
        QueryService query = persistenceService.createQuery(null);
        int initialItemCount = query.execute(ItemMMM.class).size();

        Item item1 = persistenceService.createItem();
        Item item2 = persistenceService.createItem();

        query = persistenceService.createQuery(null);
        
        //the two new items are created in their own named graph so we should also
        //see them in the union graph
        assertEquals(initialItemCount + 2, query.execute(ItemMMM.class).size());

        query = persistenceService.createQuery(item1.getURI());
        assertEquals(1, query.execute(ItemMMM.class).size());

        query = persistenceService.createQuery(item2.getURI());
        assertEquals(1, query.execute(ItemMMM.class).size());
    }

    @Test
    public void deleteItemTest() throws RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException {
        QueryService query = persistenceService.createQuery(null);
        int initialItemCount = query.execute(ItemMMM.class).size();

        Item item1 = persistenceService.createItem();
        Item item2 = persistenceService.createItem();

        //check if we see the two now Items (in the union graph)
        query = persistenceService.createQuery(null);
        assertEquals(initialItemCount + 2, query.execute(ItemMMM.class).size());

        //now check for Item1 in its own graph
        query = persistenceService.createQuery(item1.getURI());
        assertEquals(1, query.execute(ItemMMM.class).size());

        //delete item1
        persistenceService.deleteItem(item1.getURI());

        //check that it is no longer present
        query = persistenceService.createQuery(item1.getURI());
        assertEquals(0, query.execute(ItemMMM.class).size());

        //check that item2 is still present
        query = persistenceService.createQuery(item2.getURI());
        assertEquals(1, query.execute(ItemMMM.class).size());

        //check that their is one additional item left in the union grpah
        query = persistenceService.createQuery(null);
        assertEquals(initialItemCount + 1, query.execute(ItemMMM.class).size());
    }
}
