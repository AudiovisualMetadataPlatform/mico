package eu.mico.platform.reco.Resources;

import eu.mico.platform.anno4j.model.fam.LinkedEntityBody;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.LangString;
import org.openrdf.repository.object.RDFObject;

import java.net.URI;
import java.util.Locale;


public class EntityInfoTest {

    private Resource resource;
    private LinkedEntityBody linkedEntityBody;
    private RDFObject entity;

    @Before
    public void setUp() throws Exception {

        linkedEntityBody = Mockito.mock(LinkedEntityBody.class);
        entity = Mockito.mock(RDFObject.class);
        resource = Mockito.mock(Resource.class);

        Mockito.when(linkedEntityBody.getLabel()).thenReturn(new LangString("Mock", Locale.GERMAN));
        Mockito.when(linkedEntityBody.getEntity()).thenReturn(entity);
        Mockito.when(entity.getResource()).thenReturn(resource);

    }

    @Test
    public void utf16uri() throws Exception {

        String unicodeUri = "http://dbpedia.org/resource/Winâ\u0080\u0093loss_record_(pitching)";
        String strippedUri = "http://dbpedia.org/resource/Winâloss_record_(pitching)";

        Mockito.when(resource.toString()).thenReturn(unicodeUri);
        EntityInfo ei = new EntityInfo(linkedEntityBody);

        Assert.assertEquals(URI.create(strippedUri), ei.getReference());

    }

    @Test
    public void utf8uri() throws Exception {

        String unicodeUri = "http://dbpedia.org/page/Te@€€st_metâhod";

        Mockito.when(resource.toString()).thenReturn(unicodeUri);
        EntityInfo ei = new EntityInfo(linkedEntityBody);

        Assert.assertEquals(URI.create(unicodeUri), ei.getReference());

    }


}