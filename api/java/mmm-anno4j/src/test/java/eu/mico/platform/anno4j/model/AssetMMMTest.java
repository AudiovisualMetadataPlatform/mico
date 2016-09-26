package eu.mico.platform.anno4j.model;

import com.github.anno4j.Anno4j;
import com.github.anno4j.querying.QueryService;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for the AssetMMM interface.
 */
public class AssetMMMTest {

    private Anno4j anno4j;
    private QueryService queryService;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");
    }

    @Test
    public void testAsset() throws RepositoryException, IllegalAccessException, InstantiationException, ParseException, MalformedQueryException, QueryEvaluationException {

        // Create an Item
        ItemMMM item = this.anno4j.createObject(ItemMMM.class);

        // Create a Part with an Asset containing the desired location and format
        PartMMM partWithFittingAsset = this.anno4j.createObject(PartMMM.class);
        AssetMMM goodAsset = this.anno4j.createObject(AssetMMM.class);
        goodAsset.setFormat("text/plain");
        goodAsset.setLocation("http://example.com/goodAsset");
        goodAsset.setName("goodAsset");
        partWithFittingAsset.setAsset(goodAsset);

        // Create a Part that does not fit requirements
        PartMMM partWithoutFittingAsset = this.anno4j.createObject(PartMMM.class);
        AssetMMM badAsset = this.anno4j.createObject(AssetMMM.class);
        badAsset.setFormat("video/mp4");
        badAsset.setLocation("http://example.com/badAsset");
        badAsset.setName("badAsset");
        partWithoutFittingAsset.setAsset(badAsset);

        item.addPart(partWithFittingAsset);
        item.addPart(partWithoutFittingAsset);

        queryService.addCriteria("^mmm:hasAsset/^mmm:hasPart", item.getResourceAsString());
        queryService.addCriteria("dc:format", "text/plain");

        List<AssetMMM> results = queryService.execute(AssetMMM.class);

        assertEquals(1, results.size());

        AssetMMM result = results.get(0);
        assertEquals("text/plain", result.getFormat());
        assertEquals("http://example.com/goodAsset", result.getLocation());
        assertEquals("goodAsset", result.getName());
    }
}