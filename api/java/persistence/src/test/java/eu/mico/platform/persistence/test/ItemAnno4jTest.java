package eu.mico.platform.persistence.test;

import com.github.anno4j.querying.QueryService;
import com.google.common.collect.Iterables;

import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;

import org.apache.marmotta.ldpath.parser.ParseException;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
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
        TestUtils.debugRDF(log, itemAnno4j.getObjectConnection());
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
    public void getAssetFromPartTest() throws RepositoryException {
        String format = "image/png";

        Part part = itemAnno4j.createPart(extractorID);
        try {
            part.getAsset().getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }

        Asset asset = part.getAsset();
        assertNotNull(asset);
        assertNotNull(asset.getLocation());

        assertNull(asset.getFormat());
        asset.setFormat(format);
        assertEquals(format, asset.getFormat());

        assertEquals(format, persistenceService.getItem(itemAnno4j.getURI()).getPart(part.getURI()).getAsset().getFormat());
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
    

    
    @Test
    public void getAssetWithLocation() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
    	
        final Item tmpItem = persistenceService.createItem();
        assertFalse(tmpItem.hasAsset());

        final URIImpl asset_location=new URIImpl(Asset.STORAGE_SERVICE_URN_PREFIX+"pre-existing/test_location");
        
        //verifies that exceptions are raised correctly against wrong input
        boolean getAssetThrows=false;
        try{
        	tmpItem.getAssetWithLocation(null);
        }
        catch(IllegalArgumentException e){
        	getAssetThrows=true;
        }
        assertTrue(getAssetThrows);
        getAssetThrows=false;
        
        try{
        	tmpItem.getAssetWithLocation(new URIImpl(""));
        }
        catch(IllegalArgumentException e){
        	getAssetThrows=true;
        }
        assertTrue(getAssetThrows);
        getAssetThrows=false;
        
        //verifies that we created an asset with the required location
        
        Asset asset = tmpItem.getAssetWithLocation(asset_location);
        assertNotNull(asset);
        assertNotNull(asset.getLocation());
        assertEquals(asset_location,asset.getLocation());
        
        //verifies that we can access the outputStream
        try {
            asset.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        //verify that we can set and retrieve the format correctly
        String format = "test/mimeType";
        assertNull(asset.getFormat());
        asset.setFormat(format);

        assertEquals(format, persistenceService.getItem(tmpItem.getURI()).getAsset().getFormat());

        //verifies that the normal get returns the asset correctly 
        assertEquals(asset.getFormat(),tmpItem.getAsset().getFormat());
        assertEquals(asset.getLocation(),tmpItem.getAsset().getLocation());
        
        assertEquals(asset.getFormat(),tmpItem.getAssetWithLocation(asset_location).getFormat());
        assertEquals(asset.getLocation(),tmpItem.getAssetWithLocation(asset_location).getLocation());
        
        
        //verifies that trying to retrieve the asset with an invalid location fails
        try{
        	tmpItem.getAssetWithLocation(new URIImpl(Asset.STORAGE_SERVICE_URN_PREFIX+"pre-existing/invalid_location"));
        }
        catch(IllegalArgumentException e){
        	getAssetThrows=true;
        }
        assertTrue(getAssetThrows);
        getAssetThrows=false;       
        TestUtils.debugRDF(log, tmpItem.getObjectConnection());
    }
}
