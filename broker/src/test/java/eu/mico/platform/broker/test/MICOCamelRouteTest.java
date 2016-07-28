package eu.mico.platform.broker.test;

import eu.mico.platform.broker.model.MICOCamelRoute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;

/**
 * ...
 * <p>
 * Author: Thomas Koellmer
 */
public class MICOCamelRouteTest {

    private String xmlContent;

    @Before
    public void setUp() throws Exception {
        InputStream is = MICOCamelRouteTest.class.getClassLoader().getResourceAsStream("test-workflow.xml");
        Scanner s = new Scanner(is).useDelimiter("\\A");;
        xmlContent = s.hasNext() ? s.next() : "";

    }

    @Test
    public void testParseWorkflowDescription() throws Exception {
        String description = MICOCamelRoute.parseWorkflowDescription(xmlContent);

        Assert.assertEquals("Kaldi-Speech2Text-RedlinkNER", description);

    }

    @Test
    public void testInvalidWorkflowDescription() throws Exception {

        String stuff = "\n" +
                "    <route id='workflow-2-starting-point-for-pipeline-0-mimeType=video/mp4,syntacticType=mico:Video'>\n" +
                "        <from uri='direct:workflow-2,mimeType=video/mp4,syntacticType=mico:Video'/>\n" +
                "        <to uri='direct:workflow-2-pipeline-0'/>\n" +
                "    </route>";

        String description = MICOCamelRoute.parseWorkflowDescription(stuff);

        Assert.assertEquals(null, description);
    }
}