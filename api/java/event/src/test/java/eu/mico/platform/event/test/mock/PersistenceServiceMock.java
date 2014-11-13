package eu.mico.platform.event.test.mock;

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistenceServiceMock implements PersistenceService {

    private Map<URI, ContentItem> contentItems;

    public PersistenceServiceMock() {
        contentItems = new HashMap<>();
    }

    @Override
    public Metadata getMetadata() throws RepositoryException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ContentItem createContentItem() throws RepositoryException {
        final ContentItemMock ci = new ContentItemMock(UUID.randomUUID().toString());
        contentItems.put(ci.getURI(), ci);
        return ci;
    }

    @Override
    public ContentItem createContentItem(URI id) throws RepositoryException {
        final ContentItemMock ci = new ContentItemMock(id);
        contentItems.put(ci.getURI(), ci);
        return ci;
    }

    @Override
    public ContentItem getContentItem(URI id) throws RepositoryException {
        return contentItems.get(id);
    }

    @Override
    public void deleteContentItem(URI id) throws RepositoryException {
        contentItems.remove(id);
    }

    @Override
    public Iterable<ContentItem> getContentItems() throws RepositoryException {
        return Collections.unmodifiableCollection(contentItems.values());
    }

}
