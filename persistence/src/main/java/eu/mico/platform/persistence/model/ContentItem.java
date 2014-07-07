package eu.mico.platform.persistence.model;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import java.util.UUID;

/**
 * Representation of a ContentItem. A ContentItem is a collection of ContentParts, e.g. an HTML page together with
 * its embedded images. ContentParts can be either original content or created during analysis. For compatibility
 * with the Linked Data platform, its RDF type is ldp:BasicContainer
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface ContentItem {


    /**
     * Return the unique identifier (UUID) for this content item. The UUID should be built in a way that it is globally
     * unique.
     *
     * @return
     */
    public UUID getID();


    /**
     * Return the identifier (a unique URI) for this content item. This URI will be based on the internal UUID of the
     * content item in the platform.
     *
     * @return
     */
    public URI getURI();

    /**
     * Return content item metadata part of the initial content item, e.g. provenance information etc. Particularly,
     * whenever a new content part is added to the content item, the system will introduce a triple to the metadata
     * relating the content part to the content item using the ldp:contains relation.
     *
     * TODO: could return a specialised Metadata object with fast access to commonly used properties once we know the
     *       schema
     *
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    public Metadata getMetadata() throws RepositoryException;

    /**
     * Return execution plan and metadata (e.g. dependencies, profiling information, execution information). Can be
     * updated by other components to add their execution information.
     *
     * TODO: could return a specialised Metadata object once we know the schema for execution metadata
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    public Metadata getExecution() throws RepositoryException;

    /**
     * Return the current state of the analysis result (as RDF metadata). Can be updated by other components to extend
     * the result with new information. This will hold the final analysis results.
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    public Metadata getResult() throws RepositoryException;


    /**
     * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    public Content createContentPart() throws RepositoryException;

    /**
     * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * @param id the URI of the content part to create
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    public Content createContentPart(URI id) throws RepositoryException;

    /**
     * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
     * content part.
     *
     * @param id the URI of the content part to return
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    public Content getContentPart(URI id) throws RepositoryException;

    /**
     * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
     * nothing.
     *
     * @param id the URI of the content part to delete
     */
    public void deleteContent(URI id) throws RepositoryException;

    /**
     * Return an iterator over all content parts contained in this content item.
     *
     * @return an iterable that (lazily) iterates over the content parts
     */
    public Iterable<Content> listContentParts() throws RepositoryException;

}
