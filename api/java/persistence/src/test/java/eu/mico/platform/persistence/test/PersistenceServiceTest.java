/**
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

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceImpl;
import eu.mico.platform.persistence.model.ContentItem;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PersistenceServiceTest extends BaseMarmottaTest {

    @Test
    public void testCreateDeleteContentItem() throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        PersistenceService db = new PersistenceServiceImpl(testHost);

        Assert.assertFalse(db.getContentItems().iterator().hasNext());

        ContentItem ci = db.createContentItem();

        assertAsk(String.format("ASK { <%s> <http://www.w3.org/ns/ldp#contains> <%s> }", baseUrl, ci.getURI()), new URIImpl(baseUrl));

        Assert.assertTrue(db.getContentItems().iterator().hasNext());

        db.deleteContentItem(ci.getURI());

        assertAskNot(String.format("ASK { <%s> <http://www.w3.org/ns/ldp#contains> <%s> }", baseUrl, ci.getURI()), new URIImpl(baseUrl));

        Assert.assertFalse(db.getContentItems().iterator().hasNext());


    }

    @Test
    public void testListContentItems() throws RepositoryException {
        PersistenceService db = new PersistenceServiceImpl(testHost);

        ContentItem[] items = new ContentItem[5];
        for(int i=0; i<5; i++) {
            items[i] = db.createContentItem();
        }

        for(ContentItem ci : db.getContentItems()) {
            Assert.assertThat(ci, Matchers.isIn(items));
        }

        for(ContentItem ci : items) {
            db.deleteContentItem(ci.getURI());
        }
    }
}
