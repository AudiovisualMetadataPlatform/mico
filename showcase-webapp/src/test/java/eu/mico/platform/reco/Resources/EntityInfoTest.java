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