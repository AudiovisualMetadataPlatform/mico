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
package eu.mico.platform.persistence.model;

import org.openrdf.model.Model;
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
    void load(InputStream in, RDFFormat format) throws IOException, RDFParseException, RepositoryException;

    /**
     * Load RDF data into the metadata dataset. Can be used for preloading existing metadata.
     *
     * @param metadata
     * @throws RepositoryException
     */
    void load(Model metadata) throws RepositoryException;

    /**
     * Dump the RDF data contained in the metadata dataset into the given output stream using the given serialization
     * format. Can be used for exporting the metadata.
     *
     * @param out    OutputStream to export the data to
     * @param format data format the RDF data is using (e.g. Turtle)
     */
    void dump(OutputStream out, RDFFormat format) throws RDFHandlerException, RepositoryException;

    /**
     * Execute a SPARQL update query on the metadata (see   http://www.w3.org/TR/sparql11-update/). This method can be
     * used for any kind of modification of the data.
     *
     * @param sparqlUpdate
     */
    void update(String sparqlUpdate) throws MalformedQueryException, UpdateExecutionException, RepositoryException;

    /**
     * Execute a SPARQL SELECT query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    TupleQueryResult query(String sparqlQuery) throws QueryEvaluationException, MalformedQueryException, RepositoryException;

    /**
     * Execute a SPARQL ASK query on the metadata (see http://www.w3.org/TR/sparql11-query/). This method can be used for
     * any kind of data access.
     *
     * @param sparqlQuery
     * @return
     */
    boolean ask(String sparqlQuery) throws MalformedQueryException, QueryEvaluationException, RepositoryException;

    /**
     * Close the metadata connection and clean up any open resources.
     */
    void close() throws RepositoryException;

}
