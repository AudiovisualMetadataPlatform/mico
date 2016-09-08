package eu.mico.platform.reco.Resources;

import com.github.anno4j.Anno4j;
import com.github.anno4j.querying.QueryService;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.impl.bodymmm.SpeechToTextBodyMMM;
import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.object.LangString;

import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Koellmer
 */
public class NERQueryTest {


    private MICOQueryHelperMMM mqh;
    private ItemMMM item;

    @Before
    public void setUp() throws Exception {
        Anno4j anno4j = new Anno4j();
        QueryService queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");

        mqh = new MICOQueryHelperMMM(anno4j);


        item = anno4j.createObject(ItemMMM.class);

        // Create expected stt part
        PartMMM part1 = anno4j.createObject(PartMMM.class);

        SpeechToTextBodyMMM sttBody = anno4j.createObject(SpeechToTextBodyMMM.class);
        sttBody.setValue(new LangString("Test", "en"));

        part1.setBody(sttBody);

        // Create expected stt
        PartMMM part2 = anno4j.createObject(PartMMM.class);

        SpeechToTextBodyMMM sttBody2 = anno4j.createObject(SpeechToTextBodyMMM.class);
        sttBody2.setValue(new LangString("Test", "de"));


        part2.setBody(sttBody2);

        item.addPart(part1);
        item.addPart(part2);

    }


    @Test
    public void getByFormat() throws Exception {

        MICOQueryHelperMMM mqh = NERQuery.getMicoQueryHelper();

        List<String> items = NERQuery.getItemsByFormat("video/mp4", mqh);

        for (String item: items)    {
            Transcript transcript = NERQuery.getTranscript(item, DataField.CONTENTITEM, mqh);

            System.out.println(item);
            System.out.println("---------");
            System.out.println(transcript);
            System.out.println();
            System.out.println();

        }
        Assert.assertTrue(items != null && items.size() > 0);

    }

    @Test
    public void getLive() throws Exception {

        MICOQueryHelperMMM mqh = NERQuery.getMicoQueryHelper();
        Transcript transcript = NERQuery.getTranscript(item.getResourceAsString(), DataField.CONTENTITEM, mqh);



    }

    @Test
    public void getTranscript() throws Exception {

        Transcript transcript = NERQuery.getTranscript(item.getResourceAsString(), DataField.CONTENTITEM, mqh);
        assert transcript != null;

        Assert.assertTrue(transcript.size() == 2);
        Assert.assertEquals("Test", transcript.getTranscript().get(1).langstring.toString());

    }
}