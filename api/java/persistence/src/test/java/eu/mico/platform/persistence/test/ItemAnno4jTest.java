package eu.mico.platform.persistence.test;

import com.google.common.collect.Iterables;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class ItemAnno4jTest {

    private static PersistenceServiceAnno4j persistenceService;

    private static Item itemAnno4j;

    private static URIImpl extractorID;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException {
        persistenceService = new PersistenceServiceAnno4j();
        itemAnno4j = persistenceService.createItem();
        extractorID = new URIImpl("http://test-extractor-id.org/");
    }

    @Test
    public void createPartTest() throws RepositoryException {
        Part initialPart = itemAnno4j.createPart(extractorID);
        assertNotNull(initialPart.getURI());
        assertNotNull(initialPart.getSerializedAt());
        assertNotNull(initialPart.getSerializedBy());

        initialPart.setSyntacticalType("syntactical-type");
        initialPart.setSemanticType("semantic-type");

        PartMMM retrievedPart = persistenceService.getAnno4j().findByID(PartMMM.class, initialPart.getURI());

        assertNotNull(retrievedPart);
        assertEquals(initialPart.getURI(), retrievedPart.getResource());
        assertEquals(initialPart.getSemanticType(), retrievedPart.getSemanticType());
        assertEquals(initialPart.getSyntacticalType(), retrievedPart.getSyntacticalType());
        assertEquals(initialPart.getSerializedBy().getResource(), extractorID);
    }

    @Test
    public void subGraphTest() throws RepositoryException {
        int initialItemCount = persistenceService.getAnno4j().findAll(PartMMM.class).size();

        final Item tmpItem1 = persistenceService.createItem();
        final Item tmpItem2 = persistenceService.createItem();

        tmpItem1.createPart(new URIImpl("http://test-extractor-id.org/1"));
        tmpItem2.createPart(new URIImpl("http://test-extractor-id.org/2"));

        assertEquals(initialItemCount + 2, persistenceService.getAnno4j().findAll(PartMMM.class).size());

        assertEquals(1, persistenceService.getAnno4j().findAll(PartMMM.class, tmpItem1.getURI()).size());
        assertEquals(1, persistenceService.getAnno4j().findAll(PartMMM.class, tmpItem2.getURI()).size());
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

        itemAnno4j.createPart(extractorID);
        itemAnno4j.createPart(extractorID);

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
