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

package eu.mico.platform.persistence.test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Body;

import eu.mico.platform.anno4j.model.impl.bodymmm.ImageDimensionBodyMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.junit.Assert;

/**
 * A test that uses a executor service with multiple threads to perform 
 * operation on the persitence service.
 * @author Rupert Westenthaler
 *
 */
public class MultiThreadingTest {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static final int NUM_ITEMS = 10000;
    public static final int NUM_THREADS = 64;
    private static final URI EXTRACTOR_URI = new URIImpl("urn:test:extractor:1");
    
    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        persistenceService = new PersistenceServiceAnno4j();
    }

    ExecutorService execService;
    
    /**
     * The key is the item the value is the expected size for the 
     * {@link ImageDimensionBodyMMM}
     */
    Collection<Pair<Item,long[]>> testItems = new ArrayList<>();
    
    @Before
    public void initTest() throws RepositoryException {
        execService = Executors.newFixedThreadPool(NUM_THREADS);
        Random r = new Random(12345);
        for(int i = 0; i < NUM_ITEMS; i++){
            Item item = persistenceService.createItem();
            testItems.add(new ImmutablePair<Item, long[]>(item, new long[]{r.nextLong(),r.nextLong()}));
        }
    }
    
    @Test
    public void concurrencyTest() throws InterruptedException, ExecutionException, RepositoryException{
        
        log.info("> start processing ({} threads):",NUM_THREADS);
        List<Future<AnnotationWriter>> tasks = new LinkedList<>();
        for(Pair<Item, long[]> pair : testItems){
            tasks.add(execService.submit(new AnnotationWriter(EXTRACTOR_URI, pair.getKey(), pair.getValue())));
        }
        int i=0;
        while(!tasks.isEmpty()){
            tasks.remove(0).get();
            i++;
            if(i%1000 == 0){
                log.info("processed {}/{} items",i,NUM_ITEMS);
            }
        }
        i=0;
        for(Pair<Item, long[]> pair : testItems){
            //we need to retrieve the item here to read it from the repository
            Item item = persistenceService.getItem(pair.getKey().getURI());
            //log.info("> item: {}",item.getURI());
            for(Part part : item.getParts()){
                //log.info(" - part: {}",part.getURI());
                Assert.assertNotNull(part.getSerializedBy());
                Agent agent = part.getSerializedBy();
                if(EXTRACTOR_URI.equals(agent.getResource())){
                    Assert.assertNotNull(part);
                    Body body = part.getBody();
                    Assert.assertTrue(body instanceof ImageDimensionBodyMMM);
                    ImageDimensionBodyMMM imgDimensionAnno = (ImageDimensionBodyMMM)body;
                    Assert.assertNotNull(imgDimensionAnno.getWidth());
                    Assert.assertEquals(pair.getValue()[0], imgDimensionAnno.getWidth().longValue());
                    Assert.assertNotNull(imgDimensionAnno.getHeight());
                    Assert.assertEquals(pair.getValue()[1], imgDimensionAnno.getHeight().longValue());
                } else {
                    Assert.fail(" - unexpected part "+part.getURI()+" in Item "+item.getURI()+"!");
                }
            }
            item.getObjectConnection().close();
            i++;
            if(i%1000 == 0){
                log.info("validated {}/{} items",i,NUM_ITEMS);
            }
        }
    }
    
    @After
    public void cleanTest() {
        execService.shutdown();
    }
    
    
    private class AnnotationWriter implements Callable<AnnotationWriter> {

        
        private Item item;
        private ObjectConnection con;
        private long[] size;
        private URI extractorUri;

        AnnotationWriter(URI extractorUri, Item item, long[] size){
            this.extractorUri = extractorUri;
            this.item = item;
            this.size = size;
            this.con = item.getObjectConnection();
        }
        
        @Override
        public AnnotationWriter call() throws Exception {
            Part part = item.createPart(extractorUri);
            ImageDimensionBodyMMM imgDimensionAnno = con.addDesignation(con.getObjectFactory().createObject(
                    IDGenerator.BLANK_RESOURCE, ImageDimensionBodyMMM.class),ImageDimensionBodyMMM.class);
            imgDimensionAnno.setWidth(size[0]);
            imgDimensionAnno.setHeight(size[1]);
            part.setBody(imgDimensionAnno);
            return this;
        }
        
    }
    
    
    
}
