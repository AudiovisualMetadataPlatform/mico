package eu.mico.platform.persistence.test.mock;

import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.apache.commons.vfs2.FileSystemException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContentItemMock implements ContentItem {

    private final String BASE_URI = "http://example.org/contentitem/";
    private final String id;
    private final Map<URI, Content> contentParts;

    public ContentItemMock(String id) {
        this.id = id;
        contentParts = new HashMap<>();
    }

    public ContentItemMock(URI id) {
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
    public Content createContentPart() throws RepositoryException {
        final ContentMock content = new ContentMock(this, UUID.randomUUID().toString());
        contentParts.put(content.getURI(), content);
        return content;
    }

    public Content createContentPart(String id) throws RepositoryException {
        final ContentMock content = new ContentMock(this, id);
        contentParts.put(content.getURI(), content);
        return content;
    }

    @Override
    public Content createContentPart(URI uri) throws RepositoryException {
        final ContentMock content = new ContentMock(this, uri.stringValue());
        contentParts.put(content.getURI(), content);
        return content;
    }

    @Override
    public Content getContentPart(URI uri) throws RepositoryException {
        return contentParts.get(uri);
    }

    /*@Override
    public Content getContentPart(String id) throws RepositoryException {
        return contentParts.get(new URIImpl(getURI().stringValue() + "/" + id));
    }*/

    @Override
    public void deleteContent(URI uri) throws RepositoryException, FileSystemException {
        contentParts.remove(uri);
    }

    /*@Override
    public void deleteContent(String id) throws RepositoryException, FileSystemException {
        contentParts.remove(new URIImpl(getURI().stringValue() + "/" + id));
    }*/

    @Override
    public Iterable<Content> listContentParts() throws RepositoryException {
        return Collections.unmodifiableCollection(contentParts.values());
    }

}
