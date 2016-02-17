package eu.mico.platform.anno4j.model;

import com.github.anno4j.Anno4j;
import com.github.anno4j.querying.QueryService;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

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
        ItemMMM itemMMM = anno4j.createObject(ItemMMM.class);

        AssetMMM asset = anno4j.createObject(AssetMMM.class);
        String format = "someFormat";
        String location = "someLocation";
        asset.setFormat(format);
        asset.setLocation(location);

        PartMMM part1 = anno4j.createObject(PartMMM.class);
        PartMMM part2 = anno4j.createObject(PartMMM.class);
        PartMMM part3 = anno4j.createObject(PartMMM.class);

        itemMMM.setAsset(asset);
        queryService.addCriteria("mmm:hasAsset[is-a mmm:Asset]");

        // Query for non existing Items
        List<ItemMMM> result = queryService.execute(ItemMMM.class);

        assertEquals(0, result.size());

        // Persist the ItemMMM
        anno4j.persist(itemMMM);

        // Query for now one existing ItemMMM
        result = queryService.execute(ItemMMM.class);

        assertEquals(1, result.size());

        // The itemMMM does not have any parts yet
        assertEquals(0, result.get(0).getPartsMMM().size());

        // Add two parts
        HashSet<PartMMM> parts = new HashSet<PartMMM>();
        parts.add(part1);
        parts.add(part2);
        itemMMM.setPartsMMM(parts);

        assertEquals(2, result.get(0).getPartsMMM().size());

        // Now add one additional part by the addPart method
        itemMMM.addPartMMM(part3);

        assertEquals(3, result.get(0).getPartsMMM().size());
    }
}
