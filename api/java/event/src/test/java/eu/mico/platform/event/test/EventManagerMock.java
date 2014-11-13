package eu.mico.platform.event.test;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.api.AnalysisService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.vfs2.FileSystemException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Simple Event Manager Mock for testing AnalysisService implementations
 *
 * @author Sergio Fernández
 */
public class EventManagerMock implements EventManager {

    private static Logger log = LoggerFactory.getLogger(EventManagerMock.class);

    private Set<AnalysisService> services;
    private PersistenceServiceMock persistenceService;
    private AnalysisResponseCollector responsesCollector;

    @Override
    public void registerService(AnalysisService service) throws IOException {
        services.add(service);
    }

    @Override
    public void unregisterService(AnalysisService service) throws IOException {
        services.remove(service);
    }

    @Override
    public void injectContentItem(ContentItem item) throws IOException {
        try {
            log.debug("Injecting content item {}...", item.getURI());
            for (Content content: item.listContentParts()) {
                for (AnalysisService service: services) {
                    if (service.getRequires().equals(content.getType())) {
                        try {
                            log.debug("calling service {} to analyze {}...", service.getServiceID(), content.getURI());
                            service.call(responsesCollector, item, content.getURI());
                        } catch (AnalysisException e) {
                            log.error("Analysis Exception processing {}: {}", content.getURI().stringValue(), e.getMessage());
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Repository Exception: {}", e.getMessage(), e);
        }
    }

    @Override
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public AnalysisResponseCollector getResponsesCollector() {
        return responsesCollector;
    }

    @Override
    public void init() throws IOException {
        services = new HashSet<>();
        persistenceService = new PersistenceServiceMock();
        responsesCollector = new AnalysisResponseCollector();
    }

    @Override
    public void shutdown() throws IOException {
        services = null;
        persistenceService = null;
        responsesCollector = null;
    }

    private class ContentItemMock implements ContentItem {

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

    private class ContentMock implements Content {

        private final ContentItem ci;
        private final String id;
        private final Map<URI, String> properties;
        private final Map<URI, URI> relations;
        private String type;
        private ByteArrayOutputStream outputstream;

        public ContentMock(ContentItem ci, String id) {
            this.ci = ci;
            this.id = id;
            this.properties = new HashMap<>();
            this.relations = new HashMap<>();
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

    }

    private class AnalysisResponseCollector implements AnalysisResponse {

        private Map<URI, String> responses;

        public AnalysisResponseCollector() {
            responses = new HashMap<>();
        }

        @Override
        public void sendMessage(ContentItem ci, URI object) throws IOException {
            try {
                System.out.println("foooooooooooooooo: " + object.stringValue());
                log.debug("sent message about {}", object.stringValue());
                final Content content = ci.getContentPart(object);
                responses.put(object, IOUtils.toString(content.getInputStream()));
            } catch (RepositoryException e) {
                log.error("Repository Exception: {}", e.getMessage(), e);
            }
        }

    }

    private class PersistenceServiceMock implements PersistenceService {

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

}
