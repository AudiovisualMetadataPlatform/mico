package eu.mico.platform.persistence.api;

import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;

/**
 * A service for creating, retrieving and deleting content items in the MICO platform.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface PersistenceService {


    /**
     * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
     * about existing content items.
     *
     * @return
     */
    public Metadata getMetadata();

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    public ContentItem createContentItem();

    /**
     * Create a new content item with the given URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    public ContentItem createContentItem(URI id);


    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the ContentItem with the given URI, or null if it does not exist
     */
    public ContentItem getContentItem(URI id);

    /**
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    public void deleteContentItem();

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    public Iterable<ContentItem> getContentItems();

}
