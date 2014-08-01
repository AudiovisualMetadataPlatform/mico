package eu.mico.platform.persistence.model;

import org.apache.commons.vfs2.FileSystemException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

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
     *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
     *  that it derives from the ContentItem this part belongs to.
     * @return
     */
    public URI getURI();

    /**
     * Set the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     *
     * @param type
     */
    public void setType(String type) throws RepositoryException;


    /**
     * Return the type of this content part using an arbitrary string identifier (e.g. a MIME type or another symbolic
     * representation). Ideally, the type comes from a controlled vocabulary.
     */
    public String getType() throws RepositoryException;

    /**
     * Set the property value for the given property of this content part using an arbitrary string identifier;
     *
     * @param value
     */
    void setProperty(URI property, String value) throws RepositoryException;

    /**
     * Return the property value of this content part for the given property.
     */
    String getProperty(URI property) throws RepositoryException;

    /**
     * Set the property relation for the given property of this content part using another resource.
     *
     * @param value
     */
    void setRelation(URI property, URI value) throws RepositoryException;

    /**
     * Return the property value of this content part for the given property.
     */
    Value getRelation(URI property) throws RepositoryException;

    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     * @return
     */
    public OutputStream getOutputStream() throws FileSystemException;

    /**
     *  Return a new input stream for reading the content.
     * @return
     */
    public InputStream getInputStream() throws FileSystemException;
}
