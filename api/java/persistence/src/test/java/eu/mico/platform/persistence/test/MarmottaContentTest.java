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
import eu.mico.platform.storage.util.VFSUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.junit.Assert;
import org.junit.Ignore;
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
    @Ignore ("Just for debugging")
    @Test
    public void testFramework105FtpIndividualCalls() throws RepositoryException, UpdateExecutionException, MalformedQueryException, QueryEvaluationException, IOException {
        final ContentItem item = new MarmottaContentItem(baseUrl, contentUrlFtp, UUID.randomUUID().toString());
        final Content content = item.createContentPart();
        final String contentId = content.getID();
        final String contentPath = contentUrlFtp + "/" + item.getID() + "/" + content.getID() + ".bin";
        final String contentItemPath = contentUrlFtp + "/" + item.getID();
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
        //Without a dedicated buffer the transfer rate is lower by a factor of 10.
        IOUtils.copyLarge(in, out, new byte[1*1024*1024]);
        log.debug("writing content to the stream: {}ms", System.currentTimeMillis() - pivot);

    }

    @Test
    public void testFramework105Ftp() throws RepositoryException, IOException {
        Assert.assertTrue("Transfer took too long (when storage is local)", uploadFile(contentUrlFtp) < 2000);
    }

    @Test
    public void testFramework105Hdfs() throws RepositoryException, IOException {
        Assert.assertTrue("Transfer took too long (when storage is local)", uploadFile(contentUrlHdfs) < 2000);
    }

    private long uploadFile(java.net.URI storageURL) throws RepositoryException, IOException {
        final ContentItem item = new MarmottaContentItem(baseUrl, storageURL, UUID.randomUUID().toString());
        final Content content = item.createContentPart();

        log.debug("ID: {}/{}", item.getID(), content.getID());

        OutputStream out = content.getOutputStream();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("FRAMEWORK-105.mp4");

        long pivot = System.currentTimeMillis();
        //Without a dedicated buffer the transfer rate for FTP is lower by a factor of 10.
        IOUtils.copyLarge(in, out, new byte[1 * 1024 * 1024]);
        in.close();
        out.close();
        long duration = System.currentTimeMillis() - pivot;
        log.debug("Writing content to stream took {}ms", duration);
        return duration;
    }


}
