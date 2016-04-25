package eu.mico.platform.anno4j.model.extension;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.impl.ResourceObject;
import com.github.anno4j.model.impl.selector.FragmentSelector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;
import com.github.tkurz.sparqlmm.Constants;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.annotations.Iri;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 17.03.16.
 */
public class SparqlMMTest {

    private Anno4j anno4j;
    private QueryService queryService;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("ex", "http://www.example.com/schema#");

        //create some annotations
        createAnnotation("1", getImageURI("1"),getFragment(0,0,1,1));
        createAnnotation("1",getImageURI("1"),getFragment(1, 0, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(2, 0, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(0, 1, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(1, 1, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(2, 1, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(0, 2, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(1, 2, 1, 1));
        createAnnotation("1",getImageURI("1"),getFragment(2, 2, 1, 1));
    }

    @Test
    public void testQueryEvalutation() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        List<Annotation> list = queryService.addCriteria("oa:hasBody/ex:value", "1")
                .addCriteria(".[fn:leftBeside()]")
                .execute();
        assertEquals(3, list.size());

    }

    private void createAnnotation(String bodyString, String source, String fragment) throws RepositoryException, IllegalAccessException, InstantiationException {
        // Create annotation
        Annotation annotation = anno4j.createObject(Annotation.class);

        SpecificResource specificResource = anno4j.createObject(SpecificResource.class);

        ResourceObject sourceResource = anno4j.createObject(ResourceObject.class);
        sourceResource.setResourceAsString(source);

        FragmentSelector fragmentSelector = anno4j.createObject(FragmentSelector.class);
        fragmentSelector.setValue(fragment);

        StringBody body = anno4j.createObject(StringBody.class);
        body.setValue(bodyString);

        specificResource.setSource(sourceResource);
        specificResource.setSelector(fragmentSelector);

        annotation.addTarget(specificResource);
        annotation.setBody(body);

    }

    private String getImageURI(String id) {
        return "http://example.org/image/" + id + ".png";
    }

    private String getFragment(Integer ... values) {
        Preconditions.checkArgument(values.length == 4);
        return "#xywh=" + Joiner.on(",").join(values);
    }

    @Iri("http://www.example.com/schema#StringBody")
    public static interface StringBody extends Body {
        @Iri("http://www.example.com/schema#value")
        String getValue();

        @Iri("http://www.example.com/schema#value")
        void setValue(String v);
    }

}
