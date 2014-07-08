package eu.mico.platform.persistence.test;

import com.google.common.collect.ImmutableSet;
import eu.mico.marmotta.webservices.ContextualSparqlWebService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.platform.backend.kiwi.KiWiOptions;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.apache.marmotta.platform.sparql.webservices.SparqlWebService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseMarmottaTest {

    protected static JettyMarmotta marmotta;

    protected static String baseUrl;

    @BeforeClass
    public static void setup() throws MarmottaImportException, URISyntaxException, IOException, RDFParseException, RepositoryException {
        Configuration cfg = new MapConfiguration(new HashMap<String,Object>());
        cfg.setProperty(KiWiOptions.CLUSTERING_BACKEND,"HAZELCAST");
        cfg.setProperty(KiWiOptions.CLUSTERING_MODE,"LOCAL");

        marmotta = new JettyMarmotta(cfg, "/marmotta", ImmutableSet.<Class<?>>of(ContextualSparqlWebService.class, SparqlWebService.class));
        baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).build().toString();
    }

    @AfterClass
    public static void shutdown() {
        marmotta.shutdown();
        marmotta = null;
    }


    /**
     * Resolve the URI of the context with the given ID.
     *
     * @param contextId
     * @return
     */
    protected static URI resolveContext(String contextId) throws MarmottaException {
        if (contextId.contains(",")) {
            //forcing to resolve just the first one
            contextId = contextId.split(",")[0];
        }
        try {
            ContextService contextService = marmotta.getService(ContextService.class);
            ConfigurationService configurationService = marmotta.getService(ConfigurationService.class);
            final URI context = contextService.createContext(configurationService.getBaseUri() + contextId);
            if (context == null) {
                throw new MarmottaException("context not resolved");
            }
            return context;
        } catch (URISyntaxException e) {
            throw new MarmottaException(e.getMessage());
        }
    }


    protected static RepositoryConnection getFullConnection() throws RepositoryException {
        SesameService sesameService = marmotta.getService(SesameService.class);
        return sesameService.getConnection();
    }

    protected static void assertAsk(String askQuery, final URI context) throws MalformedQueryException, QueryEvaluationException {
        try {
            RepositoryConnection con = getFullConnection();
            try {
                con.begin();


                BooleanQuery q = con.prepareBooleanQuery(QueryLanguage.SPARQL,askQuery);

                Dataset d = new DatasetImpl() {
                    @Override
                    public Set<URI> getDefaultGraphs() {
                        return Collections.singleton(context);
                    }
                };


                q.setDataset(d);

                Assert.assertTrue(q.evaluate());

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            ex.printStackTrace(); // TODO: handle error
        }
    }

    protected static void assertAskNot(String askQuery, final URI context) throws MalformedQueryException, QueryEvaluationException {
        try {
            RepositoryConnection con = getFullConnection();
            try {
                con.begin();


                BooleanQuery q = con.prepareBooleanQuery(QueryLanguage.SPARQL,askQuery);

                Dataset d = new DatasetImpl() {
                    @Override
                    public Set<URI> getDefaultGraphs() {
                        return Collections.singleton(context);
                    }
                };


                q.setDataset(d);

                Assert.assertTrue(!q.evaluate());

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            ex.printStackTrace(); // TODO: handle error
        }
    }

}
