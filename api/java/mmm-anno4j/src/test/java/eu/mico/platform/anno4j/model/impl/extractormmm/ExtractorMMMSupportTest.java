package eu.mico.platform.anno4j.model.impl.extractormmm;

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
 * Test suite to test whole extractor model.
 */
public class ExtractorMMMSupportTest {

    private Anno4j anno4j;
    private QueryService queryService;

    private final static String EXTRACTOR_NAME = "extractorname";
    private final static String EXTRACTOR_VERSION = "version1";
    private final static String EXTRACTOR_ID = "extractor1";

    private final static String MODE_CONFIG_SCHEMA_URI = "modeconfigschemauri";
    private final static String MODE_OUTPUT_SCHEMA_URI = "modeoutputschemauri";
    private final static String MODE_ID = "modeid";
    private final static String MODE_DESCRIPTION = "modedescription";

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");
    }

    @Test
    public void testExtractor() throws IllegalAccessException, MalformedQueryException, RepositoryException, InstantiationException, ParseException, QueryEvaluationException {
        ExtractorMMM extractor = this.createExtractorMMM();

        ModeMMM mode = this.createMode();

        assertEquals(0, extractor.getModes().size());

        extractor.addMode(mode);

        assertEquals(1, extractor.getModes().size());
    }

    private ExtractorMMM createExtractorMMM() throws RepositoryException, IllegalAccessException, InstantiationException, ParseException, MalformedQueryException, QueryEvaluationException {
        ExtractorMMM ex = anno4j.createObject(ExtractorMMM.class);

        ex.setName(EXTRACTOR_NAME);
        ex.setStringId(EXTRACTOR_ID);
        ex.setVersion(EXTRACTOR_VERSION);

        anno4j.persist(ex);

        List<ExtractorMMM> result = queryService.execute(ExtractorMMM.class);

        assertEquals(1, result.size());

        ExtractorMMM resultObject = result.get(0);
        assertEquals(EXTRACTOR_NAME, resultObject.getName());
        assertEquals(EXTRACTOR_VERSION, resultObject.getVersion());
        assertEquals(EXTRACTOR_ID, resultObject.getStringId());

        return ex;
    }

    private ModeMMM createMode() throws RepositoryException, IllegalAccessException, InstantiationException, ParseException, MalformedQueryException, QueryEvaluationException {
        ModeMMM mode = anno4j.createObject(ModeMMM.class);

        mode.setStringId(MODE_ID);
        mode.setConfigSchemaUri(MODE_CONFIG_SCHEMA_URI);
        mode.setOutputSchemaUri(MODE_OUTPUT_SCHEMA_URI);
        mode.setDescription(MODE_DESCRIPTION);

        anno4j.persist(mode);

        List<ModeMMM> result = queryService.execute(ModeMMM.class);

        assertEquals(1, result.size());

        ModeMMM resultObject = result.get(0);
        assertEquals(MODE_ID, resultObject.getStringId());
        assertEquals(MODE_CONFIG_SCHEMA_URI, resultObject.getConfigSchemaUri());
        assertEquals(MODE_OUTPUT_SCHEMA_URI, resultObject.getOutputSchemaUri());
        assertEquals(MODE_DESCRIPTION, resultObject.getDescription());

        return mode;
    }
}