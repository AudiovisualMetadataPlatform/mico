package eu.mico.platform.reco.Resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.object.LangString;

import java.util.List;

import static com.jayway.restassured.path.json.JsonPath.from;

/**
 * ...
 * <p/>
 * Author: Thomas Koellmer
 */
public class TranscriptTest {

    private Transcript transcript;

    @Before
    public void setUp() throws Exception {
        transcript = new Transcript();

        transcript.add(new Line("123", LangString.valueOf("Welt", "de")));
        transcript.add(new Line("100", LangString.valueOf("Hallo", "de")));


    }


    @Test
    public void toStringRep() throws Exception {

        String repr = transcript.toString();

        // transcript is supposed to be sorted!
        Assert.assertEquals(
                "[100] - Hallo@de" + "\n" +
                        "[123] - Welt@de",
                repr);

    }

    @Test
    public void size() throws Exception {

        Assert.assertTrue(transcript.size() == 2);

    }

    @Test
    public void add() throws Exception {

        int oldSize = transcript.size();
        transcript.add(new Line("345", LangString.valueOf("bla", "de")));

        Assert.assertEquals(oldSize + 1, transcript.size());

    }

    @Test
    public void toJson() throws Exception {

        String json = transcript.toJson();

        List<String> lines = from(json).get("transcript");

        Assert.assertEquals(2, lines.size());

        Assert.assertEquals("100", from(json).get("transcript[0].timestamp"));
        Assert.assertEquals("Hallo", from(json).get("transcript[0].text"));
        Assert.assertEquals("de", from(json).get("transcript[0].language"));

        Assert.assertEquals("123", from(json).get("transcript[1].timestamp"));
        Assert.assertEquals("Welt", from(json).get("transcript[1].text"));
        Assert.assertEquals("de", from(json).get("transcript[1].language"));


    }


}