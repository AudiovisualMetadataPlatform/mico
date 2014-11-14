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
    public UUID getID() {
        return UUID.fromString(id);
    }

    @Override
    public URI getURI() {
        return new URIImpl("http://example.org/contentitem/" + id);
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
        final ContentMock content = new ContentMock(this, id + "/" + UUID.randomUUID());
        contentParts.put(content.getURI(), content);
        return content;
    }

    @Override
    public Content createContentPart(URI id) throws RepositoryException {
        final ContentMock content = new ContentMock(this, id.stringValue());
        contentParts.put(content.getURI(), content);
        return content;
    }

    @Override
    public Content getContentPart(URI id) throws RepositoryException {
        return contentParts.get(id);
    }

    @Override
    public void deleteContent(URI id) throws RepositoryException, FileSystemException {
        contentParts.remove(id);
    }

    @Override
    public Iterable<Content> listContentParts() throws RepositoryException {
        return Collections.unmodifiableCollection(contentParts.values());
    }

}
