package eu.mico.platform.event.test;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseCommunicationTest {

    private static Logger log = LoggerFactory.getLogger(BaseCommunicationTest.class);

    protected static String testHost;


    @BeforeClass
    public static void setupBase() throws URISyntaxException, IOException, RDFParseException, RepositoryException {
        testHost = System.getenv("test.host");
        if(testHost == null) {
            log.warn("test.host environment variable not defined, using default of 192.168.56.102");
            testHost = "192.168.56.102";
        }

        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts,true);


    }

}
