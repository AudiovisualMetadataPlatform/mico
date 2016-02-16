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
package eu.mico.platform.broker.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseBrokerTest {

    private static Logger log = LoggerFactory.getLogger(BaseBrokerTest.class);

    protected static String testHost;

    protected EventManager eventManager;
    protected Connection   connection;
    protected Channel      channel;

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

    @Before
    public void setupTestBase() throws IOException, URISyntaxException, TimeoutException {
        eventManager = new EventManagerImpl(testHost);
        eventManager.init();


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(testHost);
        factory.setUsername("mico");
        factory.setPassword("mico");

        connection = factory.newConnection();
        channel    = connection.createChannel();
    }


    @After
    public void teardownTestBase() throws IOException {
        eventManager.shutdown();

        if(channel.isOpen()) {
            channel.close();
        }
        if(connection.isOpen()) {
            connection.close();
        }
    }

    protected void setupMockAnalyser(String source, String target) throws IOException {
        eventManager.registerService(new MockService(source,target));
    }


    protected static class MockService implements AnalysisService {

        private boolean called = false;
        private String source, target;

        public MockService(String source, String target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public URI getServiceID() {
            return new URIImpl("http://example.org/services/" + StringUtils.capitalize(source) + StringUtils.capitalize(target) + "Service");
        }

        @Override
        public String getProvides() {
            return target;
        }

        @Override
        public String getRequires() {
            return source;
        }

        @Override
        public String getQueueName() {
            return source + "-" + target + "-queue";
        }

        @Override
        public void call(AnalysisResponse resp, ContentItem ci, java.util.List<URI> object, java.util.Map<String,String> params) throws AnalysisException ,IOException {
            log.info("mock analysis request for content item {}, object {}", ci.getURI(), object);
            Content c = null;
            try {
                c = ci.createContentPart();
                c.setType(getProvides());

                resp.sendNew(ci,c.getURI());
                called = true;
            } catch (RepositoryException e) {
                throw new AnalysisException("could not access triple store");
            }

        }

    }

}
