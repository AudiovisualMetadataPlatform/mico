package eu.mico.platform.persistence.test.mock;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Selector;
import eu.mico.platform.persistence.metadata.MICOProvenance;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.vfs2.FileSystemException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ContentMock implements Content {

    private final ContentItem ci;
    private final String id;
    private final Map<URI, String> properties;
    private final Map<URI, URI> relations;
    private String type;
    private ByteArrayOutputStream outputstream;
    private Model metadata;

    public ContentMock(ContentItem ci, String id) {
        this.ci = ci;
        this.id = id;
        this.properties = new HashMap<>();
        this.relations = new HashMap<>();
        this.metadata = new TreeModel();
    }

    @Override
    public String getId() {
        return  id;
    }

    @Override
    public URI getURI() {
        return new URIImpl("http://example.org/content/" + id);
    }

    @Override
    public void setType(String type) throws RepositoryException {
        this.type = type;
    }

    @Override
    public String getType() throws RepositoryException {
        return type;
    }

    @Override
    public void setProperty(URI property, String value) throws RepositoryException {
        properties.put(property, value);
    }

    @Override
    public String getProperty(URI property) throws RepositoryException {
        return properties.get(property);
    }

    @Override
    public void addMetadata(Model metadata) throws RepositoryException {
        this.metadata.addAll(metadata);
    }

    @Override
    public void setRelation(URI property, URI value) throws RepositoryException {
        relations.put(property, value);
    }

    @Override
    public Value getRelation(URI property) throws RepositoryException {
        return relations.get(property);
    }

    @Override
    public OutputStream getOutputStream() throws FileSystemException {
        outputstream = new ByteArrayOutputStream();
        return outputstream;
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        return new ByteArrayInputStream(outputstream.toByteArray());
    }

    @Override
    public ContentItem getContentItem() {
        return null;
    }

    @Override
    public Annotation createAnnotation(Body body, Content source, MICOProvenance provenance, Selector selection) throws RepositoryException {
        return null;
    }

    @Override
    public Annotation createAnnotation(Body body, Content source, MICOProvenance provenance) throws RepositoryException {
        return null;
    }

    public Model getMetadata() {
        return new TreeModel(metadata);
    }

}
