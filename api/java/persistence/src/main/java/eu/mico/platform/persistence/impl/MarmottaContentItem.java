package eu.mico.platform.persistence.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.persistence.util.SPARQLUtil;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.UUID;

import static com.google.common.collect.ImmutableMap.of;
import static eu.mico.platform.persistence.util.SPARQLUtil.createNamed;

/**
 * Implementation of a ContentItem based on a backend contextual marmotta SPARQL webservice.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MarmottaContentItem implements ContentItem {


    public static final String SUFFIX_METADATA = "-metadata";
    public static final String SUFFIX_EXECUTION = "-execution";
    public static final String SUFFIX_RESULT = "-result";


    private static Logger log = LoggerFactory.getLogger(MarmottaContentItem.class);

    private String baseUrl;
    private String contentUrl;

    // the content item's unique ID
    private UUID uuid;

    public MarmottaContentItem(String baseUrl, String contentUrl, UUID uuid) {
        this.baseUrl = baseUrl;
        this.uuid = uuid;
        this.contentUrl = contentUrl;
    }


    protected MarmottaContentItem(String baseUrl, String contentUrl, URI uri) {
        Preconditions.checkArgument(uri.stringValue().startsWith(baseUrl), "the content part URI ("+uri.stringValue()+") must match the baseUrl ("+baseUrl+")");

        this.baseUrl = baseUrl;
        this.contentUrl = contentUrl;
        this.uuid    = UUID.fromString(uri.stringValue().substring(baseUrl.length() + 1));
    }

    /**
     * Return the unique identifier (UUID) for this content item. The UUID should be built in a way that it is globally
     * unique.
     *
     * @return
     */
    @Override
    public UUID getID() {
        return uuid;
    }

    /**
     * Return the identifier (a unique URI) for this content item. This URI will be based on the internal UUID of the
     * content item in the platform.
     *
     * @return
     */
    @Override
    public URI getURI() {
        return new URIImpl(baseUrl + "/" + uuid.toString());
    }

    /**
     * Return content item metadata part of the initial content item, e.g. provenance information etc. Particularly,
     * whenever a new content part is added to the content item, the system will introduce a triple to the metadata
     * relating the content part to the content item using the ldp:contains relation.
     * <p/>
     * TODO: could return a specialised Metadata object with fast access to commonly used properties once we know the
     * schema
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getMetadata() throws RepositoryException {
        return new MarmottaMetadata(baseUrl, uuid.toString()+ SUFFIX_METADATA);
    }

    /**
     * Return execution plan and metadata (e.g. dependencies, profiling information, execution information). Can be
     * updated by other components to add their execution information.
     * <p/>
     * TODO: could return a specialised Metadata object once we know the schema for execution metadata
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getExecution() throws RepositoryException {
        return new MarmottaMetadata(baseUrl, uuid.toString()+ SUFFIX_EXECUTION);
    }

    /**
     * Return the current state of the analysis result (as RDF metadata). Can be updated by other components to extend
     * the result with new information. This will hold the final analysis results.
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getResult() throws RepositoryException {
        return new MarmottaMetadata(baseUrl, uuid.toString()+ SUFFIX_RESULT);
    }

    /**
     * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * TODO: init file storage
     *
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    @Override
    public Content createContentPart() throws RepositoryException {

        UUID contentUUID = UUID.randomUUID();
        Content content = new MarmottaContent(this, baseUrl,contentUrl,uuid.toString() + "/" + contentUUID);

        Metadata m = getMetadata();
        try {
            m.update(createNamed("createContentPart", of("ci", getURI().stringValue(), "cp", content.getURI().stringValue())));

            return content;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }

    }

    /**
     * Create a new content part with the given URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * TODO: init file storage
     *
     * @param id the URI of the content part to create
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    @Override
    public Content createContentPart(URI id) throws RepositoryException {
        Content content = new MarmottaContent(this, baseUrl,contentUrl,id);

        Metadata m = getMetadata();
        try {
            m.update(createNamed("createContentPart", of("ci", getURI().stringValue(), "cp", content.getURI().stringValue())));

            return content;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }
    }

    /**
     * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
     * content part.
     *
     * TODO: init file storage
     *
     * @param id the URI of the content part to return
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    @Override
    public Content getContentPart(URI id) throws RepositoryException {
        // check if part exists
        Metadata m = getMetadata();

        try {
            if(m.ask(createNamed("askContentPart", of("ci", getURI().stringValue(), "cp", id.stringValue())))) {
                return new MarmottaContent(this, baseUrl, contentUrl, id);
            } else {
                return null;
            }
        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:",e);
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:",e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        }
    }

    /**
     * Remove the content part with the given URI in case it exists and is a part of this content item. Otherwise do
     * nothing.
     *
     * TODO: delete file storage
     *
     * @param id the URI of the content part to delete
     */
    @Override
    public void deleteContent(URI id) throws RepositoryException, FileSystemException {

        // delete file data
        String fileName =  id.stringValue().substring(baseUrl.length() + 1);
        FileSystemManager fsmgr = VFS.getManager();
        FileObject f = fsmgr.resolveFile(contentUrl + "/" + fileName + ".bin");
        if(f.getParent().exists() && f.exists()) {
            f.delete();
        }

        // delete metadata
        Metadata m = getMetadata();

        try {
            m.update(createNamed("deleteContentPart", of("ci", getURI().stringValue(), "cp", id.stringValue())));
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }

    }

    /**
     * Return an iterator over all content parts contained in this content item.
     *
     * @return an iterable that (lazily) iterates over the content parts
     */
    @Override
    public Iterable<Content> listContentParts() throws RepositoryException {

        Metadata m = getMetadata();

        try {
            final TupleQueryResult r = m.query(createNamed("listContentParts", "ci", getURI().stringValue()));

            return new Iterable<Content>() {
                @Override
                public Iterator<Content> iterator() {
                    return new Iterator<Content>() {
                        @Override
                        public boolean hasNext() {
                            try {
                                return r.hasNext();
                            } catch (QueryEvaluationException e) {
                                return false;
                            }
                        }

                        @Override
                        public Content next() {
                            try {
                                BindingSet b = r.next();
                                URI part = (URI) b.getValue("p");
                                return new MarmottaContent(MarmottaContentItem.this, baseUrl,contentUrl, part);
                            } catch (QueryEvaluationException e) {
                                return null;
                            }
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("removing elements not supported");
                        }
                    };
                }
            };

        } catch (MalformedQueryException e) {
            log.error("the SPARQL query was malformed:", e);
            throw new RepositoryException("the SPARQL query was malformed",e);
        } catch (QueryEvaluationException e) {
            log.error("the SPARQL query could not be executed:", e);
            throw new RepositoryException("the SPARQL query could not be executed",e);
        }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarmottaContentItem that = (MarmottaContentItem) o;

        if (!baseUrl.equals(that.baseUrl)) return false;
        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = baseUrl.hashCode();
        result = 31 * result + uuid.hashCode();
        return result;
    }
}
