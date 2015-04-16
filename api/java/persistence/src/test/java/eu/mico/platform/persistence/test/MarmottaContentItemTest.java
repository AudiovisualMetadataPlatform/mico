/**
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
package eu.mico.platform.persistence.test;

import eu.mico.platform.persistence.impl.MarmottaContentItem;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaContentItemTest extends BaseMarmottaTest {

    @Test
    public void testContentItemMetadata() throws RepositoryException, UpdateExecutionException, MalformedQueryException, QueryEvaluationException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs, UUID.randomUUID().toString());

        Metadata m = item.getMetadata();

        m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
        Assert.assertTrue(m.ask("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }"));

        assertAsk("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }", new URIImpl(item.getURI().stringValue() + MarmottaContentItem.SUFFIX_METADATA));
    }

    @Test
    public void testContentItemExecution() throws RepositoryException, UpdateExecutionException, MalformedQueryException, QueryEvaluationException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs,UUID.randomUUID().toString());

        Metadata m = item.getExecution();

        m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
        Assert.assertTrue(m.ask("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }"));

        assertAsk("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }", new URIImpl(item.getURI().stringValue() + MarmottaContentItem.SUFFIX_EXECUTION));
    }

    @Test
    public void testContentItemResult() throws RepositoryException, UpdateExecutionException, MalformedQueryException, QueryEvaluationException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs,UUID.randomUUID().toString());

        Metadata m = item.getResult();

        m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");
        Assert.assertTrue(m.ask("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }"));

        assertAsk("ASK { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" }", new URIImpl(item.getURI().stringValue() + MarmottaContentItem.SUFFIX_RESULT));
    }


    @Test
    public void testCreateDeleteContentPart() throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs,UUID.randomUUID().toString());

        Assert.assertFalse(item.listContentParts().iterator().hasNext());

        Content content = item.createContentPart();

        // check if the URI of the created content part is a sub-URI of the current content item
        Assert.assertTrue(content.getURI().stringValue().startsWith(item.getURI().stringValue()));

        // check if content item entry has been added to the metadata repository
        assertAsk(String.format("ASK { <%s> <http://www.w3.org/ns/ldp#contains> <%s> } ", item.getURI().stringValue(), content.getURI().stringValue()), new URIImpl(item.getURI().stringValue() + MarmottaContentItem.SUFFIX_METADATA));

        // check if content item parts is now non-empty
        Assert.assertTrue(item.listContentParts().iterator().hasNext());

        item.deleteContent(content.getURI().stringValue());

        // check if content part has been removed from triple store
        assertAskNot(String.format("ASK { <%s> <http://www.w3.org/ns/ldp#contains> <%s> } ", item.getURI().stringValue(), content.getURI().stringValue()), new URIImpl(item.getURI().stringValue() + MarmottaContentItem.SUFFIX_METADATA));

        // check if content item parts is now again empty
        Assert.assertFalse(item.listContentParts().iterator().hasNext());

    }


    @Test
    public void testContentPartType() throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs,UUID.randomUUID().toString());

        Assert.assertFalse(item.listContentParts().iterator().hasNext());

        Content content = item.createContentPart();

        content.setType("text/plain");

        Assert.assertEquals("text/plain", content.getType());

        item.deleteContent(content.getURI().stringValue());


    }


    @Test
    public void testListContentParts() throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs, UUID.randomUUID().toString());

        Assert.assertFalse(item.listContentParts().iterator().hasNext());

        Content[] parts = new Content[5];
        for (int i = 0; i < 5; i++) {
            parts[i] = item.createContentPart();
        }

        for (Content c : item.listContentParts()) {
            Assert.assertThat(c, Matchers.isIn(parts));
        }

    }

    @Test
    public void testStreamContentPart() throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {
        ContentItem item = new MarmottaContentItem(baseUrl, contentUrlHdfs, UUID.randomUUID().toString());

        Content content = item.createContentPart();

        PrintWriter out = new PrintWriter(new OutputStreamWriter(content.getOutputStream()));
        out.println("Hello, World!");
        out.close();

        String result = IOUtils.toString(content.getInputStream());

        Assert.assertEquals("Hello, World!\n", result);


        item.deleteContent(content.getId());
    }


}
