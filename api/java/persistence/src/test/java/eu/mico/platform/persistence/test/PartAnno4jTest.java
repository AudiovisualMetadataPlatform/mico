package eu.mico.platform.persistence.test;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.targets.SpecificResource;
import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.impl.bodymmm.SpeechToTextBodyMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.PersistenceServiceAnno4j;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PartAnno4jTest {

    private static Anno4j anno4j;
    private static Item item;
    private static Part part;
    private static PersistenceService persistenceService;

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException {
        persistenceService = new PersistenceServiceAnno4j();
        anno4j = persistenceService.getAnno4j();
        item = persistenceService.createItem();
        part = item.createPart(new URIImpl("http://www.example.com/extractor"));
    }

    @Test
    public void getItemTest() throws RepositoryException {
        assertEquals(item.getURI(), part.getItem().getURI());
    }

    @Test
    public void syntacticalTypeTest() throws RepositoryException {
        assertNull(part.getSemanticType());

        String syntacticalType = "syntactical-type";
        part.setSyntacticalType(syntacticalType);
        PartMMM partMMM = anno4j.findByID(PartMMM.class, part.getURI());
        assertEquals(syntacticalType, partMMM.getSyntacticalType());
    }

    @Test
    public void semanticTypeTest() throws RepositoryException {
        assertNull(part.getSemanticType());

        String semanticType = "semantic-type";
        part.setSemanticType(semanticType);
        PartMMM partMMM = anno4j.findByID(PartMMM.class, part.getURI());
        assertEquals(semanticType, partMMM.getSemanticType());
    }

    @Test
    public void bodyTest() throws RepositoryException, InstantiationException, IllegalAccessException {
        assertNull(part.getBody());
        SpeechToTextBodyMMM body = anno4j.createObject(SpeechToTextBodyMMM.class);
        body.getValue();
        int initialSpeechToTextBodiesDefaultGraph = anno4j.findAll(SpeechToTextBodyMMM.class).size();
        int initialSpeechToTextBodiesSubGraph = anno4j.findAll(SpeechToTextBodyMMM.class, item.getURI()).size();

        part.setBody(body);

        // check if body was created in default graph
        assertEquals(initialSpeechToTextBodiesDefaultGraph + 1, anno4j.findAll(SpeechToTextBodyMMM.class).size());
        // check if body was created in the correct sub graph
        assertEquals(initialSpeechToTextBodiesSubGraph + 1, anno4j.findAll(SpeechToTextBodyMMM.class, item.getURI()).size());

        PartMMM partMMM = anno4j.findByID(PartMMM.class, part.getURI());

        assertEquals(body, partMMM.getBody());

    }

    @Test
    public void targetTest() throws RepositoryException, IllegalAccessException, InstantiationException {

        int initialTargetCount = part.getTargets().size();

        Target target1 = anno4j.createObject(SpecificResource.class);
        Target target2 = anno4j.createObject(SpecificResource.class);

        Set<Target> targetSet = new HashSet<>();
        targetSet.add(target1);

        part.addTarget(target1);
        PartMMM partMMM = anno4j.findByID(PartMMM.class, part.getURI());
        assertEquals(initialTargetCount + 1, targetSet.size());

        part.addTarget(target2);
        assertEquals(initialTargetCount + 2, partMMM.getTarget().size());

        part.setTargets(targetSet);
        assertEquals(initialTargetCount + 1, partMMM.getTarget().size());

    }

    @Test
    public void getAssetTest() throws RepositoryException {
        String format = "image/png";

        assertEquals(0, anno4j.findAll(AssetMMM.class).size());

        Asset asset = part.getAsset();
        assertEquals(1, anno4j.findAll(AssetMMM.class).size());

        part.getAsset();
        assertEquals(1, anno4j.findAll(AssetMMM.class).size());

        assertNotNull(asset);
        assertNotNull(asset.getLocation());
        assertNull(asset.getFormat());

        asset.setFormat(format);

        AssetMMM assetMMM = anno4j.findByID(PartMMM.class, part.getURI()).getAsset();
        assertEquals(format, assetMMM.getFormat());
    }

    @Test
    public void subGraphTest() throws RepositoryException {
        int initialItemCount = persistenceService.getAnno4j().findAll(AssetMMM.class).size();

        final Item tmpItem1 = persistenceService.createItem();
        final Item tmpItem2 = persistenceService.createItem();

        final Part part1 = tmpItem1.createPart(new URIImpl("http://test-extractor-id.org/1"));
        final Part part2 = tmpItem2.createPart(new URIImpl("http://test-extractor-id.org/2"));

        part1.getAsset();
        part2.getAsset();

        assertEquals(initialItemCount + 2, anno4j.findAll(AssetMMM.class).size());

        assertEquals(1, persistenceService.getAnno4j().findAll(AssetMMM.class, tmpItem1.getURI()).size());
        assertEquals(1, persistenceService.getAnno4j().findAll(AssetMMM.class, tmpItem2.getURI()).size());
    }
}
