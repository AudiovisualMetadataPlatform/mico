package eu.mico.marmotta.api;

import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;

import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface ContextualSparqlService {


    /**
     * Evaluate a SPARQL query on a contextual connection. Writes the query results to the stream passed in the format requested.
     *
     *
     * @param query query
     * @param output stream to write
     * @param format mimetype
     * @param timeoutInSeconds
     * @throws org.apache.marmotta.platform.core.exception.MarmottaException
     */
    void query(String query, OutputStream output, String format, int timeoutInSeconds, URI... contexts) throws MarmottaException, TimeoutException, MalformedQueryException;


    /**
     * Execute a SPARQL update on the KiWi TripleStore. Throws a KiWiException in case the update execution fails.
     *
     * see http://www.w3.org/TR/sparql11-update/
     *
     * @param query  a string representing the update query in SPARQL Update 1.1 syntax
     * @throws Exception
     */
    void update(URI context, String query) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException;

    QueryType getQueryType(String query) throws MalformedQueryException;
}
