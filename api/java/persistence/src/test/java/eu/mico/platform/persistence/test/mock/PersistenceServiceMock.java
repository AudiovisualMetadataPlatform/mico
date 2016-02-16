package eu.mico.platform.persistence.test.mock;

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistenceServiceMock implements PersistenceService {

    private Map<URI, Item> contentItems;

    public PersistenceServiceMock() {
        contentItems = new HashMap<>();
    }

    @Override
    public Metadata getMetadata() throws RepositoryException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Item createItem() throws RepositoryException {
        final ItemMock ci = new ItemMock(UUID.randomUUID().toString());
        contentItems.put(ci.getURI(), ci);
        return ci;
    }

    @Override
    public Item createItem(URI id) throws RepositoryException {
        final ItemMock ci = new ItemMock(id);
        contentItems.put(ci.getURI(), ci);
        return ci;
    }

    @Override
    public Item getItem(URI id) throws RepositoryException {
        return contentItems.get(id);
    }

    @Override
    public void deleteItem(URI id) throws RepositoryException {
        contentItems.remove(id);
    }

    @Override
    public Iterable<Item> getItems() throws RepositoryException {
        return Collections.unmodifiableCollection(contentItems.values());
    }

}
