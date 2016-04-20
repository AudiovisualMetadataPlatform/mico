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

import eu.mico.platform.broker.webservices.SwingImageCreator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SimpleBrokerTest extends BaseBrokerTest {

    private static final String TMP_FOLDER = "target/tmp/";
    private static final String TEST_PNG = TMP_FOLDER + "test.png";

    @BeforeClass
    public static void prepare() throws IOException{
        Files.createDirectories(new File(TMP_FOLDER).toPath());
    }

    @AfterClass
    public static void cleanup() throws IOException{
        File file = new File(TEST_PNG);
        File folder = file.getParentFile();
        if (file.exists()){
            file.delete();
        }
        if (folder.exists()){
            folder.delete();
        }
    }

    @Test
    public void testInit() throws IOException, InterruptedException, URISyntaxException {

        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        // wait for broker to finish
        synchronized (broker) {
            broker.wait(500);
        }

        // check dependency graph
        Assert.assertEquals(3, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(3, broker.getDependencies().vertexSet().size());

        FileOutputStream out = new FileOutputStream(new File(TEST_PNG));
        SwingImageCreator.createGraph(broker.getDependencies(), new Dimension(640,480), "png", out);
        out.close();
    }


    @Test
    public void testDiscovery() throws IOException, InterruptedException, URISyntaxException {
        setupMockAnalyser("A","B");
        setupMockAnalyser("B","C");
        setupMockAnalyser("A","C");

        // wait for broker to finish with discovery ...
        Thread.sleep(500);


        // check dependency graph
        Assert.assertEquals(3, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(3, broker.getDependencies().vertexSet().size());
    }
}
