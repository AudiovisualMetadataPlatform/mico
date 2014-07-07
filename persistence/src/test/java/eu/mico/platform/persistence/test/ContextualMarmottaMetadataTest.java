package eu.mico.platform.persistence.test;

import com.google.common.collect.ImmutableSet;
import eu.mico.marmotta.api.ContextualConnectionService;
import eu.mico.marmotta.webservices.ContextualSparqlWebService;
import eu.mico.platform.persistence.impl.ContextualMarmottaMetadata;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.marmotta.platform.backend.kiwi.KiWiOptions;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextualMarmottaMetadataTest extends BaseMarmottaTest {

    private static UUID baseUuid;

    @BeforeClass
    public static void setupLocal() throws MarmottaImportException, URISyntaxException, IOException, RDFParseException, RepositoryException, MarmottaException {
        baseUuid = UUID.randomUUID();

        // pre-load data
        ContextualConnectionService sesameService = marmotta.getService(ContextualConnectionService.class);
        RepositoryConnection con = sesameService.getContextualConnection(resolveContext(baseUuid.toString()));
        try {
            con.begin();

            con.add(BaseMarmottaTest.class.getResourceAsStream("/demo-data.foaf"), baseUrl, RDFFormat.RDFXML);

            con.commit();
        } catch(RepositoryException ex) {
            con.rollback();
        } finally {
            con.close();
        }
    }

    @Test
    public void testUpdate() throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        ContextualMarmottaMetadata m = new ContextualMarmottaMetadata(baseUrl, UUID.randomUUID());

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
    public void testQuery() throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        ContextualMarmottaMetadata m = new ContextualMarmottaMetadata(baseUrl, baseUuid);

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
    public void testAsk() throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        ContextualMarmottaMetadata m = new ContextualMarmottaMetadata(baseUrl, baseUuid);

        Assert.assertTrue(m.ask("ASK { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.software_engineering> }"));
        Assert.assertFalse(m.ask("ASK { <http://localhost:8080/LMF/resource/hans_meier> <http://xmlns.com/foaf/0.1/interest> <http://rdf.freebase.com/ns/en.design> }"));
    }


    @Test
    public void testLoad() throws RepositoryException, IOException, RDFParseException, MarmottaException {
        ContextualMarmottaMetadata m = new ContextualMarmottaMetadata(baseUrl, UUID.randomUUID());
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
    public void testDump() throws RepositoryException, IOException, RDFParseException, MarmottaException, RDFHandlerException {
        ContextualMarmottaMetadata m = new ContextualMarmottaMetadata(baseUrl, baseUuid);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.dump(out, RDFFormat.TURTLE);


    }

}
