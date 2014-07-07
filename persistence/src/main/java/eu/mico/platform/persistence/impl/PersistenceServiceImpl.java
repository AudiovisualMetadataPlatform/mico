package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;

/**
 * An implementation of the persistence service using an HDFS file system and a Marmotta triple store for representing
 * content item data.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class PersistenceServiceImpl implements PersistenceService {

    private static Logger log = LoggerFactory.getLogger(PersistenceServiceImpl.class);

    private String marmottaServerUrl;


    private static final String sparqlCreateCI =
            "INSERT DATA { GRAPH <%s> { " +
                    "<%s> <http://www.w3.org/ns/ldp#contains> <%s> . " +
                    "<%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/ldp#BasicContainer> . " +
                    "<%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.mico-project.eu/ns/mico/ContentItem> } }";

    private final String sparqlAskCI =
            "ASK { <%s> <http://www.w3.org/ns/ldp#contains> <%s> } ";


    private static final String sparqlDeleteCI =
            "DELETE DATA { GRAPH <%s> { " +
                    "<%s> <http://www.w3.org/ns/ldp#contains> <%s> . " +
                    "<%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/ldp#BasicContainer> . " +
                    "<%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.mico-project.eu/ns/mico/ContentItem> } }";


    private static final String sparqlDropGraph =
            "DROP GRAPH <%s>";

    protected final String sparqlListCIs =
            "SELECT ?p WHERE { <%s> <http://www.w3.org/ns/ldp#contains> ?p } ";


    // TODO: HDFS connection


    public PersistenceServiceImpl(String marmottaServerUrl) {
        this.marmottaServerUrl = marmottaServerUrl;
    }

    /**
     * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
     * about existing content items.
     *
     * @return
     */
    @Override
    public Metadata getMetadata() throws RepositoryException {
        return new ContextualMarmottaMetadata(marmottaServerUrl, "global");

    }


    private String getContext() {
        return marmottaServerUrl + "/global";
    }

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    @Override
    public ContentItem createContentItem() throws RepositoryException {

        UUID uuid = UUID.randomUUID();

        ContentItem ci = new ContextualMarmottaContentItem(marmottaServerUrl,uuid);


        Metadata m = getMetadata();
        try {
            m.update(String.format(sparqlCreateCI, getContext(), marmottaServerUrl, ci.getURI().stringValue(), ci.getURI().stringValue(), ci.getURI().stringValue()));

            return ci;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }
    }

    /**
     * Create a new content item with the given URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the newly created ContentItem
     */
    @Override
    public ContentItem createContentItem(URI id) throws RepositoryException {
        ContentItem ci = new ContextualMarmottaContentItem(marmottaServerUrl,id);


        Metadata m = getMetadata();
        try {
            m.update(String.format(sparqlCreateCI, getContext(), marmottaServerUrl, ci.getURI().stringValue(), ci.getURI().stringValue(), ci.getURI().stringValue()));

            return ci;
        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:",e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:",e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }
    }

    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @param id
     * @return a handle to the ContentItem with the given URI, or null if it does not exist
     */
    @Override
    public ContentItem getContentItem(URI id) throws RepositoryException {
        // check if part exists
        Metadata m = getMetadata();

        try {
            if(m.ask(String.format(sparqlAskCI, marmottaServerUrl, id.stringValue()))) {
                return new ContextualMarmottaContentItem(marmottaServerUrl, id);
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
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    @Override
    public void deleteContentItem(URI id) throws RepositoryException {

        // delete the content parts binary data
        // TODO

        Metadata m = getMetadata();

        try {
            // delete from global metadata first
            m.update(String.format(sparqlDeleteCI, getContext(), marmottaServerUrl, id.stringValue(), id.stringValue(), id.stringValue()));

            // delete the metadata, execution, and results context for the content item
            m.update(String.format(sparqlDropGraph, id.stringValue() + ContextualMarmottaContentItem.SUFFIX_METADATA));
            m.update(String.format(sparqlDropGraph, id.stringValue() + ContextualMarmottaContentItem.SUFFIX_EXECUTION));
            m.update(String.format(sparqlDropGraph, id.stringValue() + ContextualMarmottaContentItem.SUFFIX_RESULT));

        } catch (MalformedQueryException e) {
            log.error("the SPARQL update was malformed:", e);
            throw new RepositoryException("the SPARQL update was malformed",e);
        } catch (UpdateExecutionException e) {
            log.error("the SPARQL update could not be executed:", e);
            throw new RepositoryException("the SPARQL update could not be executed",e);
        }

    }

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    @Override
    public Iterable<ContentItem> getContentItems() throws RepositoryException {
        Metadata m = getMetadata();

        try {
            final TupleQueryResult r = m.query(String.format(sparqlListCIs, marmottaServerUrl));


            return new Iterable<ContentItem>() {
                @Override
                public Iterator<ContentItem> iterator() {
                    return new Iterator<ContentItem>() {
                        @Override
                        public boolean hasNext() {
                            try {
                                return r.hasNext();
                            } catch (QueryEvaluationException e) {
                                return false;
                            }
                        }

                        @Override
                        public ContentItem next() {
                            try {
                                BindingSet s = r.next();

                                URI ci = (URI) s.getValue("p");

                                return new ContextualMarmottaContentItem(marmottaServerUrl,ci);

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
