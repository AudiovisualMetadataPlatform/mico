package eu.mico.platform.persistence.test;

import com.github.anno4j.querying.QueryService;
import com.google.common.collect.Iterables;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class ItemAnno4jTest {

    private final static Logger log = LoggerFactory.getLogger(ItemAnno4jTest.class);
    
    private static PersistenceServiceAnno4j persistenceService;

    private static Item itemAnno4j;
    private static URIImpl extractorID;

    private static ObjectConnection con;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException {
        persistenceService = new PersistenceServiceAnno4j();
        itemAnno4j = persistenceService.createItem();
        con = itemAnno4j.getObjectConnection();
        extractorID = new URIImpl("http://test-extractor-id.org/");
    }

    @AfterClass
    public static void shutdown() throws RepositoryException {
        con.close();
    }
    
    @Test
    public void createPartTest() throws RepositoryException, QueryEvaluationException {
        Part initialPart = itemAnno4j.createPart(extractorID);
        assertNotNull(initialPart.getURI());
        assertNotNull(initialPart.getSerializedAt());
        assertNotNull(initialPart.getSerializedBy());

        initialPart.setSyntacticalType("syntactical-type");
        initialPart.setSemanticType("semantic-type");

        PartMMM retrievedPart = con.getObject(PartMMM.class,initialPart.getURI());

        assertNotNull(retrievedPart);
        assertEquals(initialPart.getURI(), retrievedPart.getResource());
        assertEquals(initialPart.getSemanticType(), retrievedPart.getSemanticType());
        assertEquals(initialPart.getSyntacticalType(), retrievedPart.getSyntacticalType());
        assertEquals(initialPart.getSerializedBy().getResource(), extractorID);
    }

    @Test
    public void subGraphTest() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        int initialItemCount = con.getObjects(PartMMM.class).asList().size();

        final Item tmpItem1 = persistenceService.createItem();
        final ObjectConnection tmpItem1Con = tmpItem1.getObjectConnection();
        
        final Item tmpItem2 = persistenceService.createItem();
        final ObjectConnection tmpItem2Con = tmpItem2.getObjectConnection();
        
        tmpItem1.createPart(new URIImpl("http://test-extractor-id.org/1"));
        Assert.assertEquals(1, tmpItem1Con.getObjects(PartMMM.class).asList().size());

        tmpItem2.createPart(new URIImpl("http://test-extractor-id.org/2"));
        Assert.assertEquals(1, tmpItem2Con.getObjects(PartMMM.class).asList().size());
        Assert.assertEquals(initialItemCount, con.getObjects(PartMMM.class).asList().size());

        QueryService query = persistenceService.createQuery(null);
        Assert.assertEquals(initialItemCount + 2, query.execute(PartMMM.class).size());
     }

    @Test
    public void getPartTest() throws RepositoryException {
        Part initialPart = itemAnno4j.createPart(extractorID);
        initialPart.setSyntacticalType("syntactical-type");
        initialPart.setSemanticType("semantic-type");

        Part retrievedPart = itemAnno4j.getPart(initialPart.getURI());
        assertEquals(initialPart.getURI(), retrievedPart.getURI());
        assertEquals(initialPart.getSerializedAt(), retrievedPart.getSerializedAt());
        assertEquals(initialPart.getSemanticType(), retrievedPart.getSemanticType());
        assertEquals(initialPart.getSyntacticalType(), retrievedPart.getSyntacticalType());
    }

    @Test
    public void getPartsTest() throws RepositoryException {
        int initialPartCount = Iterables.size(itemAnno4j.getParts());

        Part part1 = itemAnno4j.createPart(extractorID);
        Part part2 = itemAnno4j.createPart(extractorID);
        itemAnno4j.getObjectConnection().addObject(part1.getRDFObject());
        int retrievedPartCount = Iterables.size(itemAnno4j.getParts());

        assertEquals(initialPartCount + 2, retrievedPartCount);
    }

    @Test
    public void getAssetTest() throws RepositoryException {
        String format = "image/png";

        Asset asset = itemAnno4j.getAsset();
        assertNotNull(asset);
        assertNotNull(asset.getLocation());

        assertNull(asset.getFormat());
        asset.setFormat(format);

        assertEquals(format, persistenceService.getItem(itemAnno4j.getURI()).getAsset().getFormat());
    }

    @Test
    public void hasAssetTest() throws RepositoryException {
        final Item tmpItem = persistenceService.createItem();
        assertFalse(tmpItem.hasAsset());

        final Asset asset = tmpItem.getAsset();
        assertNotNull(asset);
        assertTrue(tmpItem.hasAsset());

        final Item queriedItem = persistenceService.getItem(new URIImpl(tmpItem.getURI().toString()));
        assertTrue(queriedItem.hasAsset());
    }
}
