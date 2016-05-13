package eu.mico.platform.persistence.model;

import org.openrdf.model.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public interface Asset {

    String STORAGE_SERVICE_URN_PREFIX="urn:eu.mico-project:storage.location:";

    URI getLocation();

    String getFormat();

    void setFormat(String format);

    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     *
     * @return
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Return a new input stream for reading the content.
     *
     * @return
     */
    InputStream getInputStream() throws IOException;
}
