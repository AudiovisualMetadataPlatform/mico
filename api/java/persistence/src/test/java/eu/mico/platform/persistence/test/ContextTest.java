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
 * Created by Manu on 15/09/16.
 */
public class ContextTest {

    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException, InterruptedException {
        persistenceService = new PersistenceServiceAnno4j();

        // Do something like this to connect the Test to a outside marmotta
//        persistenceService = new PersistenceServiceAnno4j(new java.net.URI("http://mico-platform:8080/marmotta"), new java.net.URI("hdfs://mico-platform/"));
    }

    @Test
    public void testQueryContext() throws RepositoryException, ParseException, MalformedQueryException, QueryEvaluationException {

        Item item = persistenceService.createItem();
        Part part = item.createPart(new URIImpl("http://test.org"));

        Item item2 = persistenceService.createItem();
        Part part2 = item2.createPart(new URIImpl("http://test.org2"));

        QueryService qs = persistenceService.createQuery(item.getURI());

        List<PartMMM> result = qs.execute(PartMMM.class);

        assertEquals(1, result.size());

        QueryService qs2 = persistenceService.createQuery(null);

        List<PartMMM> result2 = qs2.execute(PartMMM.class);

        assertEquals(2, result2.size());
    }
}

