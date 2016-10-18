package eu.mico.platform.persistence.test;

import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test suite is used to generate a big data test set on an external marmotta database.
 * In general, this should be ignored.
 */
@Ignore
public class BigDataAgentTest {

    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException, InterruptedException {
        // Do something like this to connect the Test to a outside marmotta
        persistenceService = new PersistenceServiceAnno4j(new java.net.URI("http://mico-platform:8080/marmotta"), new java.net.URI("hdfs://mico-platform/"));
    }

    @Test
    public void createBigDataSet() throws RepositoryException, ParseException, MalformedQueryException, QueryEvaluationException {

        URI extractorID = new URIImpl("http://extractor.com/ID");

        for (int i = 0; i < 2000; ++i) {
            Item item = persistenceService.createItem();

            for (int j = 0; j < 50; ++j) {
                Part part = item.createPart(extractorID);
            }

            System.out.println("Item " + i + " created!");
        }

        QueryService qs = persistenceService.createQuery(null);

        List<ItemMMM> result = qs.execute(ItemMMM.class);

        assertEquals(2000, result.size());
    }

    @Test
    public void afterBigDatasetTest() throws RepositoryException {
        URI extractorID = new URIImpl("http://extractor.com/ID");

        Item item = persistenceService.createItem();

        Part part = item.createPart(extractorID);
        Part part2 = item.createPart(extractorID);

        for(Part partList : item.getParts()) {
            System.out.println(partList.toString());
            System.out.println(partList.getSerializedBy().getName());
        }
    }
}
