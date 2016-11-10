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
package eu.mico.platform.event.test;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.storage.util.VFSUtils;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseCommunicationTest {

    private static Logger log = LoggerFactory.getLogger(BaseCommunicationTest.class);

    protected static String amqpHost;
    protected static String amqpUsr;
    protected static String amqpPwd;
    protected static String amqpVHost;

    @BeforeClass
    public static void setupBase() throws URISyntaxException, IOException, RDFParseException, RepositoryException {
        amqpHost = getConf("amqp.host","127.0.0.1");
        amqpVHost = getConf("amqp.vhost", "/");
        amqpUsr = getConf("amqp.usr", "mico");
        amqpPwd = getConf("amqp.pwd", "mico", false); //to not log the pwd
        VFSUtils.configure();
    }

    private static String getConf(String var, String defVal) {
        return getConf(var, defVal, true);
    }

    private static String getConf(String var, String defVal, boolean logVal) {
        String val = System.getenv(var);
        if(val == null) {
            val = System.getProperty(var);
            if(val == null) {
                val = defVal;
                log.warn("{} variable not defined, falling back to default one: {}", var, defVal);
            } else {
                log.info(" - {}: {} (from system property)", var, logVal ? val : "< not logged >");
            }
        } else {
            log.info(" - {}: {} (from ENV param)", var, logVal ? val : "< not logged >");
        }
        return val;
    }

}
