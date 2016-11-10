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
import com.github.anno4j.model.Body;
import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.annotations.Iri;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.LangString;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Suite to test the Part class.
 */
public class PartMMMTest {

    private Anno4j anno4j;
    private QueryService queryService;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");
    }

    @Test
    public void testPart() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException, ParseException, MalformedQueryException {
        Transaction transaction = anno4j.createTransaction();
        transaction.begin();
        PartMMM part = transaction.createObject(PartMMM.class);

        part.setAnnotatedAt(2015, 12, 17, 14, 51, 00);

        SpecificResource spec = transaction.createObject(SpecificResource.class);

        TestBody body = transaction.createObject(TestBody.class);

        part.setBody(body);
        part.addTarget(spec);

        // Query for no existing Part
        List<PartMMM> result = queryService.execute(PartMMM.class);

        assertEquals(0, result.size());

        // Persist the Part
        transaction.commit();

        // Query for one existing Part
        result = queryService.execute(PartMMM.class);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTarget() != null);

//        System.out.println(part.getTriples(RDFFormat.TURTLE));
    }

    @Iri("http://www.example.com/schema#bodyType")
    public static interface TestBody extends Body {
        @Iri("http://www.example.com/schema#doubleValue")
        Double getDoubleValue();

        @Iri("http://www.example.com/schema#doubleValue")
        void setDoubleValue(Double doubleValue);

        @Iri("http://www.example.com/schema#langValue")
        LangString getLangValue();

        @Iri("http://www.example.com/schema#langValue")
        void setLangValue(LangString langValue);

        @Iri("http://www.example.com/schema#value")
        String getValue();

        @Iri("http://www.example.com/schema#value")
        void setValue(String value);
    }
}
