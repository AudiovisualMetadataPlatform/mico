package eu.mico.platform.anno4j.model;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.namespaces.DCTERMS;
import com.github.anno4j.querying.QueryService;

import eu.mico.platform.anno4j.model.fam.FAMBody;
import eu.mico.platform.anno4j.model.fam.LanguageBody;
import eu.mico.platform.anno4j.model.fam.SentimentBody;
import eu.mico.platform.anno4j.model.namespaces.FAM;

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
public class FAMTest {

    private Anno4j anno4j;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
    }

    @Test
    public void testItem() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, MalformedQueryException, ParseException {
        LanguageBody langAnno = anno4j.createObject(LanguageBody.class);
        langAnno.setLanguage("en");
        langAnno.setConfidence(0.99d);

        SentimentBody sentAnno = anno4j.createObject(SentimentBody.class);
        sentAnno.setSentiment(0.0d);
        sentAnno.setConfidence(0.78d);
        
        // Query for non existing Items
        List<FAMBody> result = anno4j.findAll(FAMBody.class);

        assertEquals(2, result.size());
    }
}
