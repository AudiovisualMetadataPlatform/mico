package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * An implementation of the persistence service using an HDFS file system and a Marmotta triple store for representing
 * content item data.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PersistenceServiceImpl implements PersistenceService {


    private String marmottaServerUrl;


    // TODO: HDFS connection


    /**
     * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
     * about existing content items.
     *
     * @return
     */
    @Override
    public Metadata getMetadata() {
        return null;
    }

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    @Override
    public ContentItem createContentItem() {
        return null;
    }

    /**
     * Create a new content item with the given URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the newly created ContentItem
     */
    @Override
    public ContentItem createContentItem(URI id) {
        return null;
    }

    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the ContentItem with the given URI, or null if it does not exist
     */
    @Override
    public ContentItem getContentItem(URI id) {
        return null;
    }

    /**
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    @Override
    public void deleteContentItem() {

    }

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    @Override
    public Iterable<ContentItem> getContentItems() {
        return null;
    }


}
