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
    
    @Test
    public void testServiceGraph() throws IOException, InterruptedException, URISyntaxException {

    	//---- normal graph ---- 
    	
    	MockService ad = new MockService("A","D");
    	MockService ab = new MockService("A","B");
    	MockService bc = new MockService("B","C");
    	MockService ac = new MockService("A","C");
    	
    	eventManager.registerService(ad);
    	eventManager.registerService(ab);
    	eventManager.registerService(bc);
    	eventManager.registerService(ac);
    	
		// wait for broker to finish with discovery ...
        Thread.sleep(500);
		// check dependency graph
        Assert.assertEquals(4, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(4, broker.getDependencies().vertexSet().size());

	    
    	teardownMockAnalyser(ab);
    	teardownMockAnalyser(bc);
    	teardownMockAnalyser(ac);
	    //wait for unregistration of (A-B, B-C, A-C)to finish 
	    Thread.sleep(500);
	    //check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());

        bc = new MockService("B","C");
        eventManager.registerService(bc);
        // wait for broker to finish with discovery ...
        Thread.sleep(500);
        //check dependency graph
        Assert.assertEquals(2, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(4, broker.getDependencies().vertexSet().size());

        teardownMockAnalyser(bc);
    	//wait for unregistration of A-D to finish
        Thread.sleep(500);
        // check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
    	
        teardownMockAnalyser(ad);
    	//wait for unregistration of A-D to finish
        Thread.sleep(500);
        // check dependency graph
        Assert.assertEquals(0, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(0, broker.getDependencies().vertexSet().size());
        
        
        
        
        
        //----- graph with loops and repeated edges----
        ab= new MockService("A","B");
        MockService ab2 = new MockService("A","B");
        MockService ba =  new MockService("B","A");
        
        eventManager.registerService(ab);
        eventManager.registerService(ba);
		// wait for broker to finish with discovery ...
        Thread.sleep(500);
		// check dependency graph
        Assert.assertEquals(2, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
        eventManager.registerService(ab2); //leaves everything unchanged so far, because the service does already exist
        //wait for broker to finish with discovery ...
        Thread.sleep(500);
		// check dependency graph
        Assert.assertEquals(2, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
        teardownMockAnalyser(ab);
    	//wait for unregistration of A-B to finish
        Thread.sleep(500);
        // check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
        teardownMockAnalyser(ab2);
    	//wait for unregistration of (second) A-B to finish
        Thread.sleep(500);
        // check dependency graph
        Assert.assertEquals(1, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(2, broker.getDependencies().vertexSet().size());
        
        teardownMockAnalyser(ba);
    	//wait for unregistration of B-A to finish
        Thread.sleep(500);
        // check dependency graph
        Assert.assertEquals(0, broker.getDependencies().edgeSet().size());
        Assert.assertEquals(0, broker.getDependencies().vertexSet().size());
        
    }

}
