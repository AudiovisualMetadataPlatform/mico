package eu.mico.platform.anno4j.model;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test Suite builds up multiple Items and Parts that are interconnected.
 */
public class WorkflowTest {

    private Anno4j anno4j;
    private QueryService queryService;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");
    }

    @Test
    /**
     * Bigger ItemMMM and Part Test, building the example of the MMM spec of Figure 9.
     *
     * Shallow test.
     */
    public void workflowTest() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, ParseException, MalformedQueryException {
        // Create the ItemMMM
        ItemMMM itemMMM = anno4j.createObject(ItemMMM.class);

        AssetMMM asset = anno4j.createObject(AssetMMM.class);
        String format = "someItemFormat";
        String location = "someItemLocation";
        asset.setFormat(format);
        asset.setLocation(location);

        itemMMM.setAsset(asset);
        itemMMM.setSerializedAt(2015, 18, 12, 10, 52, 00);

        // Create Part 1
        PartMMM part1 = anno4j.createObject(PartMMM.class);
        part1.addInput(itemMMM);

        PartMMMTest.TestBody body1 = anno4j.createObject(PartMMMTest.TestBody.class);
        body1.setValue("someBodyValue1");
        part1.setBody(body1);

        SpecificResource spec1 = anno4j.createObject(SpecificResource.class);
        spec1.setSource(itemMMM);
        part1.addTarget(spec1);

        itemMMM.addPart(part1);

        // Create Part 2
        PartMMM part2 = anno4j.createObject(PartMMM.class);
        part2.addInput(part1);

        PartMMMTest.TestBody body2 = anno4j.createObject(PartMMMTest.TestBody.class);
        body2.setValue("someBodyValue2");
        part2.setBody(body2);

        SpecificResource spec2 = anno4j.createObject(SpecificResource.class);
        spec2.setSource(itemMMM);
        part2.addTarget(spec2);

        AssetMMM asset2 = anno4j.createObject(AssetMMM.class);
        asset2.setFormat("someFormat2");
        asset2.setLocation("someLocation2");

        part2.setAsset(asset2);
        itemMMM.addPart(part2);

        // Create Part 3
        PartMMM part3 = anno4j.createObject(PartMMM.class);
        part3.addInput(part2);

        PartMMMTest.TestBody body3 = anno4j.createObject(PartMMMTest.TestBody.class);
        body3.setValue("someBodyValue3");
        part3.setBody(body3);

        SpecificResource spec3 = anno4j.createObject(SpecificResource.class);
        spec3.setSource(part2);
        part3.addTarget(spec3);

        itemMMM.addPart(part3);

        List<ItemMMM> result = queryService.execute(ItemMMM.class);

        ItemMMM resultItemMMM = result.get(0);

        // Test
        assertEquals(3, resultItemMMM.getParts().size());

//        System.out.println(resultItemMMM.getTriples(RDFFormat.TURTLE));

        for(PartMMM part: resultItemMMM.getParts()) {
//            System.out.println(part.getTriples(RDFFormat.TURTLE));

            String partResource = part.getResourceAsString();
            if(partResource.equals(part1.getResourceAsString())) {
                assertEquals(itemMMM.getResource(), part.getInputs().iterator().next().getResource());

            } else if(partResource.equals(part2.getResourceAsString())) {
                assertEquals(part1.getResource(), part.getInputs().iterator().next().getResource());

            } else {
                assertEquals(part2.getResource(), part.getInputs().iterator().next().getResource());
            }
        }
    }
}
