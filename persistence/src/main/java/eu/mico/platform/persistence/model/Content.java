package eu.mico.platform.persistence.model;

import org.openrdf.model.URI;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Binary content, offered through inputstreams and outputstreams. Can be used for representing any kind of non-RDF
 * content.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface Content {

    /**
     * Return the unique identifier identifying this content part. The UUID should be constructed so that it is globally
     * unique and optionally derived from the UUID of the content item it belongs to.
     * @return
     */
    public UUID getID();

    /**
     *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
     *  that it derives from the ContentItem this part belongs to.
     * @return
     */
    public URI getURI();

    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     * @return
     */
    public OutputStream getOutputStream();

    /**
     *  Return a new input stream for reading the content.
     * @return
     */
    public InputStream getInputStream();
}
