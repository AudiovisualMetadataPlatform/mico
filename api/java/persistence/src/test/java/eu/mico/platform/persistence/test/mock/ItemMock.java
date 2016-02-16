package eu.mico.platform.persistence.test.mock;

import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Metadata;
import org.apache.commons.vfs2.FileSystemException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemMock implements Item {

    private final String BASE_URI = "http://example.org/contentitem/";
    private final String id;
    private final Map<URI, Part> contentParts;

    public ItemMock(String id) {
        this.id = id;
        contentParts = new HashMap<>();
    }

    public ItemMock(URI id) {
        this(id.getLocalName());
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public URI getURI() {
        return new URIImpl(BASE_URI + id);
    }

    @Override
    public Metadata getMetadata() throws RepositoryException {
        return null;
    }

    @Override
    public Metadata getExecution() throws RepositoryException {
        return null;
    }

    @Override
    public Metadata getResult() throws RepositoryException {
        return null;
    }

    @Override
    public Part createPart() throws RepositoryException {
        final PartMock content = new PartMock(this, UUID.randomUUID().toString());
        contentParts.put(content.getURI(), content);
        return content;
    }

    public Part createPart(String id) throws RepositoryException {
        final PartMock content = new PartMock(this, id);
        contentParts.put(content.getURI(), content);
        return content;
    }

    @Override
    public Part createPart(URI uri) throws RepositoryException {
        final PartMock content = new PartMock(this, uri.stringValue());
        contentParts.put(content.getURI(), content);
        return content;
    }

    @Override
    public Part getPart(URI uri) throws RepositoryException {
        return contentParts.get(uri);
    }

    /*@Override
    public Part getPart(String id) throws RepositoryException {
        return contentParts.get(new URIImpl(getURI().stringValue() + "/" + id));
    }*/

    @Override
    public void deletePart(URI uri) throws RepositoryException, FileSystemException {
        contentParts.remove(uri);
    }

    /*@Override
    public void deletePart(String id) throws RepositoryException, FileSystemException {
        contentParts.remove(new URIImpl(getURI().stringValue() + "/" + id));
    }*/

    @Override
    public Iterable<? extends Part> getParts() throws RepositoryException {
        return Collections.unmodifiableCollection(contentParts.values());
    }

    @Override
    public Asset getAsset() {
        return null;
    }
}
