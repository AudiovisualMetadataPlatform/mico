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

package eu.mico.platform.anno4j.model;

import com.github.anno4j.Anno4j;
import com.github.anno4j.Transaction;
import com.github.anno4j.querying.QueryService;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Suite to test the ItemMMM class.
 */
public class ItemMMMTest {

    private Anno4j anno4j;
    private QueryService queryService;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");
    }

    @Test
    public void testItem() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, MalformedQueryException, ParseException {
        Transaction transaction = anno4j.createTransaction();
        transaction.begin();
        ItemMMM itemMMM = transaction.createObject(ItemMMM.class);

        AssetMMM asset = transaction.createObject(AssetMMM.class);
        String format = "someFormat";
        String location = "someLocation";
        asset.setFormat(format);
        asset.setLocation(location);

        PartMMM part1 = transaction.createObject(PartMMM.class);
        PartMMM part2 = transaction.createObject(PartMMM.class);
        PartMMM part3 = transaction.createObject(PartMMM.class);

        itemMMM.setAsset(asset);
        queryService.addCriteria("mmm:hasAsset[is-a mmm:Asset]");

        // Query for non existing Items
        List<ItemMMM> result = queryService.execute(ItemMMM.class);

        assertEquals(0, result.size());

        // Persist the ItemMMM
        transaction.commit();

        // Query for now one existing ItemMMM
        result = queryService.execute(ItemMMM.class);

        assertEquals(1, result.size());

        // The itemMMM does not have any parts yet
        assertEquals(0, result.get(0).getParts().size());

        transaction.begin();
        
        // Add two parts
        HashSet<PartMMM> parts = new HashSet<PartMMM>();
        parts.add(part1);
        parts.add(part2);
        itemMMM.setParts(parts);

        assertEquals(0, result.get(0).getParts().size());
        
        transaction.commit();

        assertEquals(2, result.get(0).getParts().size());

        transaction.begin();
        
        // Now add one additional part by the addPart method but rollback later on
        itemMMM.addPart(part3);

        transaction.rollback();
        
        assertEquals(2, result.get(0).getParts().size());
        
        //Now add the third part with autocommit
        itemMMM.addPart(part3);
        
        assertEquals(3, result.get(0).getParts().size());

    }
}
