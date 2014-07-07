package eu.mico.platform.persistence.test;

import com.google.common.collect.ImmutableSet;
import eu.mico.marmotta.webservices.ContextualSparqlWebService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.platform.backend.kiwi.KiWiOptions;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseMarmottaTest {

    protected static JettyMarmotta marmotta;

    protected static String baseUrl;

    @BeforeClass
    public static void setup() throws MarmottaImportException, URISyntaxException, IOException {
        Configuration cfg = new MapConfiguration(new HashMap<String,Object>());
        cfg.setProperty(KiWiOptions.CLUSTERING_BACKEND,"HAZELCAST");
        cfg.setProperty(KiWiOptions.CLUSTERING_MODE,"LOCAL");

        marmotta = new JettyMarmotta(cfg, "/marmotta", ImmutableSet.<Class<?>>of(ContextualSparqlWebService.class));
        baseUrl = UriBuilder.fromUri("http://localhost").port(marmotta.getPort()).path(marmotta.getContext()).build().toString();
    }

    @AfterClass
    public static void shutdown() {
        marmotta.shutdown();
        marmotta = null;
    }

}
