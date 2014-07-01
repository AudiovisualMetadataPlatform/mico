package eu.mico.platform.persistence.model;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Binary content, offered through inputstreams and outputstreams. Can be used for representing any kind of non-RDF
 * content.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface Content {

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
