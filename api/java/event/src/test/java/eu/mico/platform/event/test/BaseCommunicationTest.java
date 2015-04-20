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
package eu.mico.platform.event.test;

import eu.mico.platform.storage.util.VFSUtils;
import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

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
            testHost = System.getProperty("test.host");
            if(testHost == null) {
                testHost = "127.0.0.1";
                log.warn("test.host variable not defined, falling back to default one: {}", testHost);
            }
        }

        VFSUtils.configure();
    }

}
