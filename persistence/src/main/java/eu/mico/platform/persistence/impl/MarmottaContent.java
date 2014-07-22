package eu.mico.platform.persistence.impl;

import com.google.common.base.Preconditions;
import eu.mico.platform.persistence.model.Content;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaContent implements Content {


    private String baseUrl;
    private String contentUrl;
    private String id;


    public MarmottaContent(String baseUrl, String contentUrl, String id) {
        this.baseUrl    = baseUrl;
        this.contentUrl = contentUrl;
        this.id = id;
    }


    protected MarmottaContent(String baseUrl, String contentUrl, URI uri) {
        Preconditions.checkArgument(uri.stringValue().startsWith(baseUrl), "the content part URI must match the baseUrl");

        this.baseUrl    = baseUrl;
        this.contentUrl = contentUrl;
        this.id = uri.stringValue().substring(baseUrl.length() + 1);
    }


    /**
     * Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
     * that it derives from the ContentItem this part belongs to.
     *
     * @return
     */
    @Override
    public URI getURI() {
        return new URIImpl(baseUrl + "/" + id);
    }

    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     *
     * @return
     */
    @Override
    public OutputStream getOutputStream() throws FileSystemException {
        FileSystemManager fsmgr = VFS.getManager();
        FileObject f = fsmgr.resolveFile(contentUrl + "/" + id + ".bin");
        return f.getContent().getOutputStream();
    }

    /**
     * Return a new input stream for reading the content.
     *
     * @return
     */
    @Override
    public InputStream getInputStream() throws FileSystemException {
        FileSystemManager fsmgr = VFS.getManager();
        FileObject f = fsmgr.resolveFile(contentUrl + "/" + id + ".bin");
        return f.getContent().getInputStream();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarmottaContent that = (MarmottaContent) o;

        if (!baseUrl.equals(that.baseUrl)) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = baseUrl.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ContextualMarmottaContent{" +
                "baseUrl='" + baseUrl + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}