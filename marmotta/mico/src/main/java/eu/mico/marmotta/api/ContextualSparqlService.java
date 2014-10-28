/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
