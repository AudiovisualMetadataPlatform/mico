/**
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
package eu.mico.platform.persistence.test;

import eu.mico.platform.persistence.impl.MarmottaMetadata;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaMetadataTest extends BaseMarmottaTest {

    private static UUID baseUuid;

    @BeforeClass
    public static void setupLocal() throws URISyntaxException, IOException, RDFParseException, RepositoryException {
        baseUuid = UUID.randomUUID();

        // pre-load data
        RepositoryConnection con = repository.getConnection();
        try {
            con.begin();

            con.add(BaseMarmottaTest.class.getResourceAsStream("/demo-data.foaf"), baseUrl.toString(), RDFFormat.RDFXML, resolveContext(baseUuid.toString()));

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }

    @Test
    public void testUpdate() throws RepositoryException, MalformedQueryException, UpdateExecutionException, URISyntaxException {

        MarmottaMetadata m = new MarmottaMetadata(baseUrl.toString(), UUID.randomUUID().toString());

        m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");

        RepositoryConnection con = getFullConnection();
        try {
            con.begin();

            URI s = con.getValueFactory().createURI("http://example.org/resource/R1");
            URI p = con.getValueFactory().createURI("http://example.org/property/P1");
            Literal o = con.getValueFactory().createLiteral("Value 1");

            Assert.assertTrue(con.hasStatement(s,p,o,true));

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }



    @Test
    public void testQuery() throws RepositoryException, QueryEvaluationException, MalformedQueryException, URISyntaxException {
        MarmottaMetadata m = new MarmottaMetadata(baseUrl.toString(), baseUuid.toString());

        TupleQueryResult r = m.query("SELECT ?i WHERE { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> ?i }");
        Assert.assertEquals(1, r.getBindingNames().size());
        for(int i=0; i<4; i++) {
            Assert.assertTrue(r.hasNext());
            BindingSet s = r.next();
            Assert.assertThat(s.getValue("i").stringValue(), Matchers.isOneOf(
                    "http://rdf.freebase.com/ns/en.software_engineering","http://rdf.freebase.com/ns/en.linux",
                    "http://dbpedia.org/resource/Java","http://dbpedia.org/resource/Climbing"));
        }

    }


    @Test
    public void testAsk() throws RepositoryException, QueryEvaluationException, MalformedQueryException, URISyntaxException {
        MarmottaMetadata m = new MarmottaMetadata(baseUrl.toString(), baseUuid.toString());

        Assert.assertTrue(m.ask("ASK { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.software_engineering> }"));
        Assert.assertFalse(m.ask("ASK { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.design> }"));
    }


    @Test
    public void testLoad() throws RepositoryException, IOException, RDFParseException, URISyntaxException {
        MarmottaMetadata m = new MarmottaMetadata(baseUrl.toString(), UUID.randomUUID().toString());
        m.load(this.getClass().getResourceAsStream("/version-base.rdf"), RDFFormat.RDFXML);

        RepositoryConnection con = getFullConnection();
        try {
            con.begin();

            URI s = con.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/R1");
            URI p = con.getValueFactory().createURI("http://marmotta.apache.org/testing/ns1/P1");
            Literal o = con.getValueFactory().createLiteral("property 1 value 1");
            URI c = resolveContext(m.getContext().toString());

            Assert.assertTrue(con.hasStatement(s,p,o,true,c));

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }

    }


    @Test
    public void testDump() throws RepositoryException, IOException, RDFParseException, RDFHandlerException, URISyntaxException {
        MarmottaMetadata m = new MarmottaMetadata(baseUrl.toString(), baseUuid.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.dump(out, RDFFormat.TURTLE);


    }

}
