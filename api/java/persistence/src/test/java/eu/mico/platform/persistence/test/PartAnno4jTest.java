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

    @BeforeClass
    public static void setUp() throws URISyntaxException, RepositoryException {
        PersistenceService persistenceService = new PersistenceServiceAnno4j();
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

        int initialTargetCount = part.getTarget().size();

        Target target1 = anno4j.createObject(SpecificResource.class);
        Target target2 = anno4j.createObject(SpecificResource.class);

        Set<Target> targetSet = new HashSet<>();
        targetSet.add(target1);

        part.addTarget(target1);
        PartMMM partMMM = anno4j.findByID(PartMMM.class, part.getURI());
        assertEquals(initialTargetCount + 1, targetSet.size());

        part.addTarget(target2);
        assertEquals(initialTargetCount + 2, partMMM.getTarget().size());

        part.setTarget(targetSet);
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

}
