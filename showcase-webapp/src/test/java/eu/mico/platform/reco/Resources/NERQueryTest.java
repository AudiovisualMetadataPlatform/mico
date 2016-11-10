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

package eu.mico.platform.reco.Resources;

import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.testutils.MqhMocks;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * ...
 * <p/>
 * Author: Thomas Koellmer
 */
public class NERQueryTest {


    private MICOQueryHelperMMM mqh;

    @Before
    public void setUp() throws Exception {


        mqh = MqhMocks.mockMicoQueryHelper();



    }


    @Test
    public void getByFormat() throws Exception {


        List<String> items = NERQuery.getItemsByFormat("video/mp4", mqh);

        for (String item : items) {
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


        Transcript transcript = NERQuery.getTranscript("ci", DataField.CONTENTITEM, mqh);


    }

    @Test
    public void getTranscript() throws Exception {

        Transcript transcript = NERQuery.getTranscript("ci", DataField.CONTENTITEM, mqh);
        assert transcript != null;

        Assert.assertTrue(transcript.size() == 2);
        Assert.assertEquals("label", transcript.getTranscript().get(1).langstring.toString());

    }

    @Test
    public void testGetVideos() throws Exception {


        List<String> item_uris = NERQuery.getFileNamesByFormat("video/mp4", mqh);


        Assert.assertNotNull(item_uris);
        Assert.assertTrue(item_uris.size() > 0);

        for (String s : item_uris) {
            System.out.println(s);
        }

    }
}