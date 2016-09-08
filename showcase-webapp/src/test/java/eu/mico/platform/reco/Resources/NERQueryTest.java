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

/**
 * ...
 * <p/>
 * Author: Thomas Koellmer
 */
public class NERQueryTest {


    private final static String TEST_BODY_IRI = "http://example.org/testbody";
    private Anno4j anno4j;
    private MICOQueryHelperMMM mqh;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        QueryService queryService = anno4j.createQueryService();
        queryService.addPrefix("mmm", "http://www.mico-project.eu/ns/mmm/2.0/schema#");

        mqh = new MICOQueryHelperMMM(this.anno4j);
    }


    @Test
    public void getTranscript() throws Exception {


        ItemMMM item = this.anno4j.createObject(ItemMMM.class);

        // Create expected stt part without timecode
        PartMMM part1 = this.anno4j.createObject(PartMMM.class);

        SpeechToTextBodyMMM sttBody = this.anno4j.createObject(SpeechToTextBodyMMM.class);
        sttBody.setValue(new LangString("Test", "en"));

        part1.setBody(sttBody);

        // Create expected stt
        PartMMM part2 = this.anno4j.createObject(PartMMM.class);

        SpeechToTextBodyMMM sttBody2 = this.anno4j.createObject(SpeechToTextBodyMMM.class);
        sttBody2.setValue(new LangString("Test", "de"));


        part2.setBody(sttBody2);

        item.addPart(part1);
        item.addPart(part2);

        Transcript transcript = NERQuery.getTranscript(item.getResourceAsString(), mqh);

        Assert.assertTrue(transcript.size() == 2);
        Assert.assertEquals("Test", transcript.getTranscript().get(1).langstring.toString());

    }
}