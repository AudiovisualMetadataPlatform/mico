package eu.mico.platform.anno4j.querying;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.namespaces.RDF;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.annotations.Iri;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for the MICOQueryHelperMMM class.
 */
public class MICOQueryHelperMMMTest {

    private Anno4j anno4j;
    private QueryService queryService;

    private final static String TEST_BODY_IRI = "http://example.org/testbody";

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");
    }

    @Test
    public void testGetPartsOfItem() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, MalformedQueryException, ParseException {
        ItemMMM item = this.anno4j.createObject(ItemMMM.class);

        // Create part 1
        PartMMM part1 = this.anno4j.createObject(PartMMM.class);
        TestBody body1 = this.anno4j.createObject(TestBody.class);
        body1.setValue("body1");
        part1.setBody(body1);

        // Create part 2
        PartMMM part2 = this.anno4j.createObject(PartMMM.class);
        TestBody body2 = this.anno4j.createObject(TestBody.class);
        body2.setValue("body2");
        part2.setBody(body2);

        item.addPart(part1);
        item.addPart(part2);

        MICOQueryHelperMMM helper = new MICOQueryHelperMMM(this.anno4j);

        List<PartMMM> result = helper.getPartsOfItem(item.getResourceAsString());

        assertEquals(2, result.size());

        String body1Value = ((TestBody)result.get(0).getBody()).getValue();
        String body2Value = ((TestBody)result.get(1).getBody()).getValue();
        assertTrue(body1Value.equals("body1") && body2Value.equals("body2") || body1Value.equals("body2") && body2Value.equals("body1"));
    }

    @Test
    public void testGetPartsBySourceName() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, MalformedQueryException, ParseException {
        ItemMMM item = this.anno4j.createObject(ItemMMM.class);

        AssetMMM asset = this.anno4j.createObject(AssetMMM.class);
        asset.setFormat("mp3");
        asset.setLocation("someLocation");

        item.setAsset(asset);

        // Create part 1
        PartMMM part1 = this.anno4j.createObject(PartMMM.class);
        TestBody body1 = this.anno4j.createObject(TestBody.class);
        body1.setValue("body1");
        part1.setBody(body1);

        // Create part 2
        PartMMM part2 = this.anno4j.createObject(PartMMM.class);
        TestBody body2 = this.anno4j.createObject(TestBody.class);
        body2.setValue("body2");
        part2.setBody(body2);

        item.addPart(part1);
        item.addPart(part2);

        MICOQueryHelperMMM helper = new MICOQueryHelperMMM(this.anno4j);

        List<PartMMM> result = helper.getPartsBySourceNameOfAsset(asset.getResourceAsString());

        assertEquals(2, result.size());

        String body1Value = ((TestBody)result.get(0).getBody()).getValue();
        String body2Value = ((TestBody)result.get(1).getBody()).getValue();
        assertTrue(body1Value.equals("body1") && body2Value.equals("body2") || body1Value.equals("body2") && body2Value.equals("body1"));
    }

    @Test
    public void testGetPartsBySourceLocation() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, MalformedQueryException, ParseException {
        ItemMMM item = this.anno4j.createObject(ItemMMM.class);

        AssetMMM asset = this.anno4j.createObject(AssetMMM.class);
        asset.setFormat("mp3");
        asset.setLocation("someLocation");

        item.setAsset(asset);

        // Create part 1
        PartMMM part1 = this.anno4j.createObject(PartMMM.class);
        TestBody body1 = this.anno4j.createObject(TestBody.class);
        body1.setValue("body1");
        part1.setBody(body1);

        // Create part 2
        PartMMM part2 = this.anno4j.createObject(PartMMM.class);
        TestBody body2 = this.anno4j.createObject(TestBody.class);
        body2.setValue("body2");
        part2.setBody(body2);

        item.addPart(part1);
        item.addPart(part2);

        MICOQueryHelperMMM helper = new MICOQueryHelperMMM(this.anno4j);

        List<PartMMM> result = helper.getPartsBySourceLocationOfAsset(asset.getLocation());

        assertEquals(2, result.size());

        String body1Value = ((TestBody)result.get(0).getBody()).getValue();
        String body2Value = ((TestBody)result.get(1).getBody()).getValue();
        assertTrue(body1Value.equals("body1") && body2Value.equals("body2") || body1Value.equals("body2") && body2Value.equals("body1"));
    }

    @Iri(TEST_BODY_IRI)
    public interface TestBody extends BodyMMM {

        @Iri(RDF.VALUE)
        void setValue(String value);

        @Iri(RDF.VALUE)
        String getValue();
    }
}