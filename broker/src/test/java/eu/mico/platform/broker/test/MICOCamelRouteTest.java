/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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