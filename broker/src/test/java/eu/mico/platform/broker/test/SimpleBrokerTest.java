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

package eu.mico.platform.broker.test;

import eu.mico.platform.broker.webservices.SwingImageCreator;

import org.junit.*;

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

    // NOTE: Do not run this test against a platform with an active configuration,
    // as the dependency counts will be wrong

    private static final String TMP_FOLDER = "target/tmp/";
    private static final String TEST_PNG = TMP_FOLDER + "test.png";

    @BeforeClass
    public static void prepare() throws IOException{
        Files.createDirectories(new File(TMP_FOLDER).toPath());

        Assume.assumeTrue("Broker already contains nodes, " +
                "tests are probably run against a productive mico instance",
                broker.getDependencies().vertexSet().size() == 0);
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


        // check dependency graph
        Assert.assertEquals(3, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(3, broker.getDependencies().vertexSet().size());
    }
    
    @Test
    public void testServiceGraph() throws IOException, InterruptedException, URISyntaxException {

    	//---- normal graph ---- 
    	
    	MockService ad = new MockService("A","D");
    	MockService ab = new MockService("A","B");
    	MockService bc = new MockService("B","C");
    	MockService ac = new MockService("A","C");
    	
    	connectExtractor(ad);
    	connectExtractor(ab);
    	connectExtractor(bc);
    	connectExtractor(ac);
    	
		// check dependency graph
        Assert.assertEquals(4, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(4, broker.getDependencies().vertexSet().size());

	    
    	teardownMockAnalyser(ab);
    	teardownMockAnalyser(bc);
    	teardownMockAnalyser(ac);

	    //check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());

        
        bc = new MockService("B","C");
        connectExtractor(bc);

        //check dependency graph
        Assert.assertEquals(2, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(4, broker.getDependencies().vertexSet().size());

        
        teardownMockAnalyser(bc);

        // check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
    	
        teardownMockAnalyser(ad);

        // check dependency graph
        Assert.assertEquals(0, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(0, broker.getDependencies().vertexSet().size());
        
        
        
        
        
        //----- graph with loops and repeated edges----
        ab= new MockService("A","B");
        MockService ab2 = new MockService("A","B");
        MockService ba =  new MockService("B","A");
        
        connectExtractor(ab);
        connectExtractor(ba);

		// check dependency graph
        Assert.assertEquals(2, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
        
        connectExtractor(ab2); //leaves everything unchanged so far, because the service does already exist

		// check dependency graph
        Assert.assertEquals(2, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());

        
        teardownMockAnalyser(ab);

        // check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());

        
        teardownMockAnalyser(ab2);

        // check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        

        teardownMockAnalyser(ba);

        // check dependency graph
        Assert.assertEquals(0, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(0, broker.getDependencies().vertexSet().size());
        
    }

}
