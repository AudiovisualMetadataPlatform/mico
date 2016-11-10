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

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.targets.SpecificResource;
import com.github.anno4j.querying.QueryService;

import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.impl.bodymmm.SpeechToTextBodyMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;

import org.apache.hadoop.util.IdGenerator;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PartAnno4jTest {

    private static final Logger log = LoggerFactory.getLogger(PartAnno4jTest.class);
    
    private static Item item;
    private static ObjectConnection con;
    private static Part part;
    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException {
        persistenceService = new PersistenceServiceAnno4j();
        item = persistenceService.createItem();
        con = item.getObjectConnection();
        con.begin();
        part = item.createPart(new URIImpl("http://www.example.com/extractor"));
        con.commit();
    }

    @AfterClass
    public static void close() throws RepositoryException{
        con.close();
    }
    
    @Test
    public void getItemTest() throws RepositoryException {
        assertEquals(item.getURI(), part.getItem().getURI());
    }

    @Test
    public void syntacticalTypeTest() throws RepositoryException, QueryEvaluationException {
        assertNull(part.getSemanticType());

        String syntacticalType = "syntactical-type";
        part.setSyntacticalType(syntacticalType);
        PartMMM partMMM = con.getObject(PartMMM.class, part.getURI());
        assertEquals(syntacticalType, partMMM.getSyntacticalType());
    }

    @Test
    public void semanticTypeTest() throws RepositoryException, QueryEvaluationException {
        assertNull(part.getSemanticType());

        String semanticType = "semantic-type";
        part.setSemanticType(semanticType);
        PartMMM partMMM = con.getObject(PartMMM.class, part.getURI());
        assertEquals(semanticType, partMMM.getSemanticType());
    }

    @Test
    public void bodyTest() throws RepositoryException, InstantiationException, IllegalAccessException, MalformedQueryException, QueryEvaluationException, ParseException {
        assertNull(part.getBody());
        ObjectFactory factory = con.getObjectFactory();
        //body.getValue();
        QueryService query = persistenceService.createQuery(null);
        int initialSpeechToTextBodiesDefaultGraph = query.execute(SpeechToTextBodyMMM.class).size();
        int initialSpeechToTextBodiesSubGraph = con.getObjects(SpeechToTextBodyMMM.class).asList().size();

        SpeechToTextBodyMMM body = con.addDesignation(
                factory.createObject(IDGenerator.BLANK_RESOURCE, SpeechToTextBodyMMM.class),
                SpeechToTextBodyMMM.class);
        part.setBody(body);

        
        TestUtils.debugRDF(log, con);
        // check if body was created in the correct sub graph
        query = persistenceService.createQuery(item.getURI());
        assertEquals(initialSpeechToTextBodiesSubGraph + 1, query.execute(SpeechToTextBodyMMM.class).size());
        // query for the new Annotation Body also in the union graph
        query = persistenceService.createQuery(null);
        assertEquals(initialSpeechToTextBodiesDefaultGraph + 1, query.execute(SpeechToTextBodyMMM.class).size());

        PartMMM partMMM = con.getObject(PartMMM.class, part.getURI());

        assertEquals(body, partMMM.getBody());

    }

    @Test
    public void targetTest() throws RepositoryException, IllegalAccessException, InstantiationException, QueryEvaluationException {

        ObjectFactory factory = con.getObjectFactory();

        int initialTargetCount = part.getTargets().size();

        Target target1 = factory.createObject(IDGenerator.BLANK_RESOURCE,SpecificResource.class);
        Target target2 = factory.createObject(IDGenerator.BLANK_RESOURCE,SpecificResource.class);

        Set<Target> targetSet = new HashSet<>();
        targetSet.add(target1);

        part.addTarget(target1);
        PartMMM partMMM = con.getObject(PartMMM.class, part.getURI());
        assertEquals(initialTargetCount + 1, targetSet.size());

        part.addTarget(target2);
        assertEquals(initialTargetCount + 2, partMMM.getTarget().size());

        part.setTargets(targetSet);
        assertEquals(initialTargetCount + 1, partMMM.getTarget().size());

    }

    @Test
    public void getAssetTest() throws RepositoryException, QueryEvaluationException {
        String format = "image/png";
        String name = "assetName";

        assertEquals(0, con.getObjects(AssetMMM.class).asList().size());

        Asset asset = part.getAsset();
        assertEquals(1, con.getObjects(AssetMMM.class).asList().size());

        part.getAsset();
        assertEquals(1, con.getObjects(AssetMMM.class).asList().size());

        assertNotNull(asset);
        assertNotNull(asset.getLocation());
        assertNull(asset.getFormat());
        assertNull(asset.getName());

        asset.setFormat(format);
        asset.setName(name);

        AssetMMM assetMMM = con.getObject(PartMMM.class, part.getURI()).getAsset();
        assertEquals(format, assetMMM.getFormat());
        assertEquals(name, assetMMM.getName());
    }
    
    @Test
    public void subGraphTest() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        int initialItemCount = con.getObjects(PartMMM.class).asList().size();

        final Item tmpItem1 = persistenceService.createItem();
        final ObjectConnection tmpItem1Con = tmpItem1.getObjectConnection();
        
        final Item tmpItem2 = persistenceService.createItem();
        final ObjectConnection tmpItem2Con = tmpItem1.getObjectConnection();
        
        tmpItem1.createPart(new URIImpl("http://test-extractor-id.org/1"));
        tmpItem2.createPart(new URIImpl("http://test-extractor-id.org/2"));

        Assert.assertEquals(1, tmpItem1Con.getObjects(PartMMM.class).asList().size());
        Assert.assertEquals(1, tmpItem2Con.getObjects(PartMMM.class).asList().size());
        Assert.assertEquals(initialItemCount, con.getObjects(PartMMM.class).asList().size());
        
        QueryService query = persistenceService.createQuery(null);
        Assert.assertEquals(initialItemCount + 2, query.execute(PartMMM.class).size());
        
        Iterable<? extends Part> parts = tmpItem1.getParts();
        int num = 0;
        for(Part part : parts){
            num++;
            Assert.assertEquals(tmpItem1,part.getItem());
        }
        Assert.assertEquals(1, num);
        parts = tmpItem2.getParts();
        num = 0;
        for(Part part : parts){
            num++;
            Assert.assertEquals(tmpItem2,part.getItem());
        }
        Assert.assertEquals(1, num);
    }

}
