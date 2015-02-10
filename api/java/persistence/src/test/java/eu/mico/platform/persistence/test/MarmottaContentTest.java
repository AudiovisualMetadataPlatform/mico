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
import eu.mico.platform.persistence.util.VFSUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Marmotta Content test
 *
 * @author Sergio Fernández
 */
public class MarmottaContentTest extends BaseMarmottaTest {

    /**
     * Test for debugging FRAMEWORK-105
     *
     * @see <a href="https://issues.mico-project.eu/browse/FRAMEWORK-105">FRAMEWORK-105</a>
     */
    @Test
    public void testFramework105() throws RepositoryException, UpdateExecutionException, MalformedQueryException, QueryEvaluationException, IOException {
        final ContentItem item = new MarmottaContentItem(baseUrl, contentUrl, UUID.randomUUID());
        final Content content = item.createContentPart();
        final String id = content.getId();
        final String contentPath = contentUrl + "/" + id + ".bin";
        final String contentItemPath = contentUrl + "/" + id.substring(0, id.lastIndexOf('/'));
        log.info("Using {} as path for testing", contentPath);

        final FileSystemOptions opts = VFSUtils.configure();

        long pivot = System.currentTimeMillis();
        FileSystemManager fsmgr = VFS.getManager();
        log.debug("initializing VFS Manager: {}ms", System.currentTimeMillis() - pivot);

        pivot = System.currentTimeMillis();
        FileObject d = fsmgr.resolveFile(contentItemPath, opts);
        log.debug("resolving content item path: {}ms", System.currentTimeMillis() - pivot);

        pivot = System.currentTimeMillis();
        if(!d.exists()) {
            log.debug("checking parent directory: {}ms", System.currentTimeMillis() - pivot);

            pivot = System.currentTimeMillis();
            d.createFolder();
            log.debug("creating parent directory: {}ms", System.currentTimeMillis() - pivot);
        }

        pivot = System.currentTimeMillis();
        FileObject f = fsmgr.resolveFile(contentPath, opts);
        log.debug("resolving content part path: {}ms", System.currentTimeMillis() - pivot);

        pivot = System.currentTimeMillis();
        f.createFile();
        log.debug("creating content part file: {}ms", System.currentTimeMillis() - pivot);

        pivot = System.currentTimeMillis();
        OutputStream out = f.getContent().getOutputStream();
        log.debug("getting output stream: {}ms", System.currentTimeMillis() - pivot);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("FRAMEWORK-105.mp4");
        pivot = System.currentTimeMillis();
        IOUtils.copy(in, out);
        log.debug("writing content to the stream: {}ms", System.currentTimeMillis() - pivot);

    }
}
