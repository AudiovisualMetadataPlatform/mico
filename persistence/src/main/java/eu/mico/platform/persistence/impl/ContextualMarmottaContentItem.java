package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.UUID;

/**
 * Implementation of a ContentItem based on a backend contextual marmotta SPARQL webservice.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextualMarmottaContentItem implements ContentItem {


    public static final String SUFFIX_METADATA = "-metadata";
    public static final String SUFFIX_EXECUTION = "-execution";
    public static final String SUFFIX_RESULT = "-result";


    private static Logger log = LoggerFactory.getLogger(ContextualMarmottaContentItem.class);

    private String baseUrl;

    // the content item's unique ID
    private UUID uuid;

    private final String sparqlCreatePart    = "INSERT DATA { <%s> <http://www.w3.org/ns/ldp#contains> <%s> } ";
    private final String sparqlAskPart       = "ASK { <%s> <http://www.w3.org/ns/ldp#contains> <%s> } ";
    private final String sparqlDeletePart    = "DELETE DATA { GRAPH <%s> { <%s> <http://www.w3.org/ns/ldp#contains> <%s> } } ";
    private final String sparqlListParts     = "SELECT ?p WHERE { <%s> <http://www.w3.org/ns/ldp#contains> ?p } ";


    public ContextualMarmottaContentItem(String baseUrl, UUID uuid) {
        this.baseUrl = baseUrl;
        this.uuid = uuid;
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
        return new ContextualMarmottaMetadata(baseUrl, uuid.toString()+ SUFFIX_METADATA);
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
        return new ContextualMarmottaMetadata(baseUrl, uuid.toString()+ SUFFIX_EXECUTION);
    }

    /**
     * Return the current state of the analysis result (as RDF metadata). Can be updated by other components to extend
     * the result with new information. This will hold the final analysis results.
     *
     * @return a handle to a Metadata object that is suitable for reading and updating
     */
    @Override
    public Metadata getResult() throws RepositoryException {
        return new ContextualMarmottaMetadata(baseUrl, uuid.toString()+ SUFFIX_RESULT);
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
        Content content = new ContextualMarmottaContent(baseUrl,uuid.toString() + "/" + contentUUID);

        Metadata m = getMetadata();
        try {
            m.update(String.format(sparqlCreatePart, getURI().stringValue(), content.getURI().stringValue()));

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
        Content content = new ContextualMarmottaContent(baseUrl,id);

        Metadata m = getMetadata();
        try {
            m.update(String.format(sparqlCreatePart, getURI().stringValue(), content.getURI().stringValue()));

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
            if(m.ask(String.format(sparqlAskPart, getURI().stringValue(), id.stringValue()))) {
                return new ContextualMarmottaContent(baseUrl, id);
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
    public void deleteContent(URI id) throws RepositoryException {

        Metadata m = getMetadata();

        try {
            m.update(String.format(sparqlDeletePart, getURI().stringValue() + SUFFIX_METADATA, getURI().stringValue(), id.stringValue()));
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
            final TupleQueryResult r = m.query(String.format(sparqlListParts, getURI().stringValue()));

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
                                return new ContextualMarmottaContent(baseUrl, part);
                            } catch (QueryEvaluationException e) {
                                return null;
                            }
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
}
