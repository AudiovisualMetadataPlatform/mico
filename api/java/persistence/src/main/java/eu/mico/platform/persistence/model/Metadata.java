package eu.mico.platform.persistence.model;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ContentPart Metadata, in RDF format. Offers structured access through the Sesame Repository API (and later commons-rdf
 * once it becomes available)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface Metadata {

    /**
     * Load RDF data of the given format into the metadata dataset. Can be used for preloading existing metadata.
     *
     * @param in      InputStream to load the data from
     * @param format  data format the RDF data is using (e.g. Turtle)
     */
    public void load(InputStream in, RDFFormat format) throws IOException, RDFParseException, RepositoryException;


    /**
     * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
     * format. Can be used for exporting the metadata.
     *
     * @param out    OutputStream to export the data to
     * @param format data format the RDF data is using (e.g. Turtle)
     */
    public void dump(OutputStream out, RDFFormat format) throws RDFHandlerException, RepositoryException;



    /**
     * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
     * used for any kind of modification of the data.
     *
     * @param sparqlUpdate
     */
    public void update(String sparqlUpdate) throws MalformedQueryException, UpdateExecutionException, RepositoryException;


    /**
     * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    public TupleQueryResult query(String sparqlQuery) throws QueryEvaluationException, MalformedQueryException, RepositoryException;



    /**
     * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    public boolean ask(String sparqlQuery) throws MalformedQueryException, QueryEvaluationException, RepositoryException;


    /**
     * Close the metadata connection and clean up any open resources.
     */
    public void close() throws RepositoryException;
}
