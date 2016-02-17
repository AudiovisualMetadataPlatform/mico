/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.persistence.test;

import com.google.common.collect.Iterables;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PersistenceServiceAnno4jTest {

    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        persistenceService = new PersistenceServiceAnno4j();
    }

    @Test
    public void createItemTest() throws RepositoryException {
        Item item = persistenceService.createItem();
        assertNotNull(item.getURI());
        assertNotNull(item.getSerializedAt());

        item.setSyntacticalType("syntactical-type");
        item.setSemanticType("semantic-type");

        ItemMMM itemMMM = persistenceService.getAnno4j().findByID(ItemMMM.class, item.getURI());
        assertNotNull(itemMMM);
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
    public void getItemsTest() throws RepositoryException {
        int initialItemCount = persistenceService.getAnno4j().findAll(ItemMMM.class).size();

        persistenceService.createItem();
        persistenceService.createItem();

        int retrievedSize = Iterables.size(persistenceService.getItems());

        assertEquals(initialItemCount+2, retrievedSize);
    }

    @Test
    public void subGraphTest() throws RepositoryException {
        int initialItemCount = persistenceService.getAnno4j().findAll(ItemMMM.class).size();

        Item item1 = persistenceService.createItem();
        Item item2 = persistenceService.createItem();

        assertEquals(initialItemCount + 2, persistenceService.getAnno4j().findAll(ItemMMM.class).size());

        assertEquals(1, persistenceService.getAnno4j().findAll(ItemMMM.class, item1.getURI()).size());
        assertEquals(1, persistenceService.getAnno4j().findAll(ItemMMM.class, item2.getURI()).size());
    }

    @Test
    public void deleteItemTest() throws RepositoryException {

        int intialItemCount = Iterables.size(persistenceService.getItems());

        Item item1 = persistenceService.createItem();
        Item item2 = persistenceService.createItem();

        assertEquals(intialItemCount + 2, Iterables.size(persistenceService.getItems()));

        persistenceService.deleteItem(item1.getURI());

        assertEquals(intialItemCount + 1, Iterables.size(persistenceService.getItems()));

        assertEquals(0, persistenceService.getAnno4j().findAll(ItemMMM.class, item1.getURI()).size());
        assertEquals(1, persistenceService.getAnno4j().findAll(ItemMMM.class, item2.getURI()).size());
    }
}
