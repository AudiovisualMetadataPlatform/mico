package eu.mico.platform.persistence.impl;

import com.google.common.base.Preconditions;
import eu.mico.platform.persistence.model.Content;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextualMarmottaContent implements Content {


    private String baseUrl;
    private String id;


    public ContextualMarmottaContent(String baseUrl, String id) {
        this.baseUrl = baseUrl;
        this.id = id;
    }


    public ContextualMarmottaContent(String baseUrl, URI uri) {
        Preconditions.checkArgument(uri.stringValue().startsWith(baseUrl), "the content part URI must match the baseUrl");

        this.baseUrl = baseUrl;
        this.id = uri.stringValue().substring(baseUrl.length());
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
    public OutputStream getOutputStream() {
        return null;
    }

    /**
     * Return a new input stream for reading the content.
     *
     * @return
     */
    @Override
    public InputStream getInputStream() {
        return null;
    }
}
