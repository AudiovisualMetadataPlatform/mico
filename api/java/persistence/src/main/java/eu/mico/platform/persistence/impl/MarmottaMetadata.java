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
package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.Model;
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

/**
 * An implementation of metadata accessing a contextual marmotta with its webservices.
 */
public class MarmottaMetadata implements Metadata {

    private static Logger log = LoggerFactory.getLogger(MarmottaMetadata.class);

    private java.net.URI baseURL;
    private String context;

    private Repository repository;

    /**
     * Create a new contextual marmotta metadata instance connecting to the Marmotta instance with the given base URL
     * using the main SPARQL endpoint.
     *
     * @param baseURL base URL of the marmotta server, without the trailing slash, e.g. http://localhost:8080/marmotta
     */
    public MarmottaMetadata(java.net.URI baseURL) throws RepositoryException {
        this.context = null;

        this.baseURL  = baseURL.normalize();
        repository    = new SPARQLRepository(this.baseURL.toString() + "/sparql/select", this.baseURL.toString() + "/sparql/update");
        repository.initialize();
    }

    /**
     * Create a new contextual marmotta metadata instance connecting to the Marmotta instance with the given base URL
     * and the context with the given UUID.
     *
     * @param baseURL base URL of the marmotta server, without the trailing slash, e.g. http://localhost:8080/marmotta
     * @param context UUID of the metadata object to access
     */
    public MarmottaMetadata(String baseURL, String context) throws RepositoryException, java.net.URISyntaxException {
        this.context = context;

        this.baseURL  = new java.net.URI(baseURL + "/" + context).normalize();
        repository    = new SPARQLRepository(this.baseURL.toString() + "/sparql/select", this.baseURL.toString() + "/sparql/update");
        repository.initialize();
    }


    public String getContext() {
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

                con.add(in, baseURL.toString(), format);

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

    @Override
    public void load(Model metadata) throws RepositoryException {
        try {
            RepositoryConnection conn = repository.getConnection();
            try {
                conn.begin();
                conn.add(metadata);
                conn.commit();
            } catch(RepositoryException ex) {
                conn.rollback();
            } finally {
                conn.close();
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

                Update u = con.prepareUpdate(QueryLanguage.SPARQL, sparqlUpdate, baseURL.toString());
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

                TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery, baseURL.toString());
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

                BooleanQuery q = con.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery, baseURL.toString());
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

    @Override
    public Repository getRepository() {
        return repository;
    }
}
