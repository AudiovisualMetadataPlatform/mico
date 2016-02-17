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
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.ArrayList;
import java.util.List;

public class PersistenceServiceAnno4jTest extends BaseMarmottaTest {

    @Test
    public void testCreateDeleteContentItem() throws RepositoryException, QueryEvaluationException, MalformedQueryException, java.net.URISyntaxException {
        PersistenceService service = new PersistenceServiceAnno4j(testHost);

        int numberOfInitialItems = Iterables.size(service.getItems());

        Item item = service.createItem();

        assertAsk(String.format("ASK WHERE { graph <%s> { ?s ?p ?o } }", item.getURI()));

        Assert.assertEquals(numberOfInitialItems + 1, Iterables.size(service.getItems()));

        service.deleteItem(item.getURI());

        Assert.assertEquals(numberOfInitialItems, Iterables.size(service.getItems()));

        assertAskNot(String.format("ASK WHERE { graph <%s> { ?s ?p ?o } }", item.getURI()));
    }

    @Test
    public void testListContentItems() throws RepositoryException, java.net.URISyntaxException {
        PersistenceService service = new PersistenceServiceAnno4j(testHost);

        List<Item> items = new ArrayList<Item>();
        for (int i = 0; i < 5; i++) {
            items.add(service.createItem());
        }

        int itemsFound = 0;
        for (Item ci : service.getItems()) {
            if (items.contains(ci))
                itemsFound++;
        }

        Assert.assertTrue(items.size() == itemsFound);

        for (Item ci : items) {
            service.deleteItem(ci.getURI());
        }
    }
}
