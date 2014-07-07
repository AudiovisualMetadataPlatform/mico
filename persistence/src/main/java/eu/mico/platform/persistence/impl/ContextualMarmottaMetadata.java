package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * An implementation of metadata accessing a contextual marmotta with its webservices.
 */
public class ContextualMarmottaMetadata implements Metadata {

    private static Logger log = LoggerFactory.getLogger(ContextualMarmottaMetadata.class);


    private String baseUri;
    private UUID context;

    private Repository repository;

    /**
     * Create a new contextual marmotta metadata instance connecting to the Marmotta instance with the given base URI
     * and the context with the given UUID.
     *
     * @param baseUri base URI of the marmotta server, without the trailing slash, e.g. http://localhost:8080/marmotta
     * @param context UUID of the metadata object to access
     */
    public ContextualMarmottaMetadata(String baseUri, UUID context) throws RepositoryException {
        this.context = context;

        this.baseUri  = baseUri + "/" + context.toString();
        repository    = new SPARQLRepository(this.baseUri + "/sparql/select", this.baseUri+"/sparql/update");
        repository.initialize();
    }


    public UUID getContext() {
        return context;
    }

    /**
     * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
     *
     * @param in     InputStream to load the data from
     * @param format data format the RDF data is using (e.g. Turtle)
     */
    @Override
    public void load(InputStream in, RDFFormat format) throws IOException, RDFParseException, RepositoryException {
        try {
            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                con.add(in, baseUri, format);

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while loading data into repository",ex);
            throw ex;
        }
    }

    /**
     * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
     * format. Can be used for exporting the metadata.
     *
     * @param out    OutputStream to export the data to
     * @param format data format the RDF data is using (e.g. Turtle)
     */
    @Override
    public void dump(OutputStream out, RDFFormat format) throws RDFHandlerException, RepositoryException {
        try {
            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                RDFHandler h = Rio.createWriter(format, out);

                con.export(h);

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while reading data from repository",ex);
            throw ex;
        }

    }

    /**
     * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
     * used for any kind of modification of the data.
     *
     * @param sparqlUpdate
     */
    @Override
    public void update(String sparqlUpdate) throws MalformedQueryException, UpdateExecutionException, RepositoryException {
        try {
            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();


                Update u = con.prepareUpdate(QueryLanguage.SPARQL, sparqlUpdate, baseUri);
                u.execute();

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while updating repository", ex);
            throw ex;
        }
    }

    /**
     * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    @Override
    public TupleQueryResult query(String sparqlQuery) throws QueryEvaluationException, MalformedQueryException, RepositoryException {

        try {
            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery, baseUri);
                return q.evaluate();
            } catch(RepositoryException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while querying repository", ex);
            throw ex;
        }


    }

    /**
     * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    @Override
    public boolean ask(String sparqlQuery) throws MalformedQueryException, QueryEvaluationException, RepositoryException {
        try {
            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                BooleanQuery q = con.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery, baseUri);
                return q.evaluate();

            } catch(RepositoryException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while querying repository", ex);
            throw ex;
        }


    }

    /**
     * Close the metadata connection and clean up any open resources.
     */
    @Override
    public void close() throws RepositoryException {
        repository.shutDown();
    }
}
