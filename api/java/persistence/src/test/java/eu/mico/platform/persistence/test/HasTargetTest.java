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

import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.impl.targetmmm.SpecificResourceMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import java.net.URISyntaxException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by weissger on 08.09.16.
 */
public class HasTargetTest {

    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException, InterruptedException {
        persistenceService = new PersistenceServiceAnno4j();
    }

    @Test
    public void testCreate() throws RepositoryException, IllegalAccessException, InstantiationException, ParseException, MalformedQueryException, QueryEvaluationException, InterruptedException {
        Item item = persistenceService.createItem();
        Part part = item.createPart(new URIImpl("http://test.org"));

        ObjectConnection con = item.getObjectConnection();

        SpecificResource specificResource = createObject(con, SpecificResource.class);
        SpecificResourceMMM specificResourceMMM = createObject(con, SpecificResourceMMM.class);

        part.addTarget(specificResource);
        part.addTarget(specificResourceMMM);

        QueryService qs = persistenceService.createQuery(item.getURI());

        List<PartMMM> result = qs.addPrefix(MMM.PREFIX, MMM.NS).addCriteria("mmm:hasTarget").execute(PartMMM.class);

        // Should find a single part with 2 targets
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getTarget().size());

        List<PartMMM> result2 = qs.addPrefix(MMM.PREFIX, MMM.NS).addCriteria("oa:hasTarget").execute(PartMMM.class);

        // Shouldn't find annotation based hasTarget properties
        assertEquals(0, result2.size());
    }

    private <T> T createObject(ObjectConnection con, Class<T> type) throws RepositoryException {
        return createObject(con, type, null);
    }

    private <T> T createObject(ObjectConnection con, Class<T> type, org.openrdf.model.Resource resource) throws RepositoryException {
        return con.addDesignation(con.getObjectFactory().createObject(
                resource == null ? IDGenerator.BLANK_RESOURCE : resource, type), type);
    }
}
