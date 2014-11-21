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

import eu.mico.platform.persistence.util.VFSUtils;
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
public abstract class BaseMarmottaTest {

    protected static Logger log = LoggerFactory.getLogger(BaseMarmottaTest.class);

    protected static String baseUrl;
    protected static String contentUrl;
    protected static String testHost;

    protected static Repository repository;

    @BeforeClass
    public static void setup() throws URISyntaxException, IOException, RDFParseException, RepositoryException {
        testHost = System.getenv("test.host");
        if(testHost == null) {
            testHost = System.getProperty("test.host");
            if(testHost == null) {
                testHost = "192.168.56.102";
                log.warn("test.host variable not defined, falling back to default one: {}", testHost);
            }
        }

        baseUrl    = "http://" + testHost + ":8080/marmotta";
        contentUrl = "ftp://mico:mico@" + testHost;

        VFSUtils.configure();

        repository = new SPARQLRepository(baseUrl + "/sparql/select", baseUrl+"/sparql/update");
        repository.initialize();

    }

    @AfterClass
    public static void teardown() throws RepositoryException {
        repository.shutDown();
    }

    /**
     * Resolve the URI of the context with the given ID.
     *
     * @param contextId
     * @return
     */
    protected static URI resolveContext(String contextId) {
        if (contextId.contains(",")) {
            //forcing to resolve just the first one
            contextId = contextId.split(",")[0];
        }

        return new URIImpl(baseUrl + "/" + contextId);
    }

    protected static RepositoryConnection getFullConnection() throws RepositoryException {
        return repository.getConnection();
    }

    protected static void assertAsk(String askQuery, final URI context) throws MalformedQueryException, QueryEvaluationException {
        try {
            RepositoryConnection conn = getFullConnection();
            try {
                conn.begin();

                BooleanQuery q = conn.prepareBooleanQuery(QueryLanguage.SPARQL,askQuery);

                Dataset d = new DatasetImpl() {
                    @Override
                    public Set<URI> getDefaultGraphs() {
                        return Collections.singleton(context);
                    }
                };

                q.setDataset(d);

                Assert.assertTrue(q.evaluate());

                conn.commit();
            } catch(RepositoryException ex) {
                conn.rollback();
            } finally {
                conn.close();
            }
        } catch(RepositoryException ex) {
            ex.printStackTrace(); // TODO: handle error
        }
    }

    protected static void assertAskNot(String askQuery, final URI context) throws MalformedQueryException, QueryEvaluationException {
        try {
            RepositoryConnection conn = getFullConnection();
            try {
                conn.begin();

                BooleanQuery q = conn.prepareBooleanQuery(QueryLanguage.SPARQL,askQuery);

                Dataset d = new DatasetImpl() {
                    @Override
                    public Set<URI> getDefaultGraphs() {
                        return Collections.singleton(context);
                    }
                };

                q.setDataset(d);

                Assert.assertTrue(!q.evaluate());

                conn.commit();
            } catch(RepositoryException ex) {
                conn.rollback();
            } finally {
                conn.close();
            }
        } catch(RepositoryException ex) {
            ex.printStackTrace(); // TODO: handle error
        }
    }

}
