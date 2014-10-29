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
package eu.mico.marmotta.services;

import eu.mico.marmotta.api.ContextualConnectionService;
import eu.mico.marmotta.api.ContextualSparqlService;
import eu.mico.marmotta.api.QueryType;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.*;
import org.openrdf.query.resultio.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.*;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ContextualSparqlServiceImpl implements ContextualSparqlService {

    @Inject
    private Logger log;

    @Inject
    private ContextualConnectionService connectionService;

    @Inject
    private SesameService sesameService;

    @Inject
    private ConfigurationService configurationService;


    private ExecutorService executorService;

    private long queryId = 0;

    @PostConstruct
    public void initialize() {
        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "SPARQL Query Thread " + (++queryId));
            }
        });
    }

    @Override
    public QueryType getQueryType(String query) throws MalformedQueryException {
        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
        ParsedQuery parsedQuery = parser.parseQuery(query, configurationService.getServerUri());
        if (parsedQuery instanceof ParsedTupleQuery) {
            return QueryType.TUPLE;
        } else if (parsedQuery instanceof ParsedBooleanQuery) {
            return QueryType.BOOL;
        } else if (parsedQuery instanceof ParsedGraphQuery) {
            return QueryType.GRAPH;
        } else {
            return null;
        }
    }

    /**
     * Evaluate a SPARQL query on a contextual connection. Writes the query results to the stream passed in the format requested.
     *
     * @param query            query
     * @param output           stream to write
     * @param format           mimetype
     * @param timeoutInSeconds
     * @throws org.apache.marmotta.platform.core.exception.MarmottaException
     *
     */
    @Override
    public void query(final String query, final OutputStream output, final String format, int timeoutInSeconds, final URI... contexts) throws MarmottaException, TimeoutException, MalformedQueryException {
        log.debug("executing SPARQL query:\n{}", query);
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
                try {
                    RepositoryConnection connection = connectionService.getContextualConnection(contexts);
                    try {
                        connection.begin();
                        Query sparqlQuery = connection.prepareQuery(QueryLanguage.SPARQL, query);

                        // workaround for a Sesame bug: we explicitly set the context for the query in the dataset
                        DatasetImpl ds = new DatasetImpl();
                        ds.addDefaultGraph(contexts[0]);
                        for (URI context: contexts) {
                            ds.addNamedGraph(context);
                            ds.addDefaultGraph(context);
                        }
                        ds.addDefaultRemoveGraph(contexts[0]);
                        ds.setDefaultInsertGraph(contexts[0]);
                        sparqlQuery.setDataset(ds);

                        if (sparqlQuery instanceof TupleQuery) {
                            query((TupleQuery)sparqlQuery, output, format);
                        } else if (sparqlQuery instanceof BooleanQuery) {
                            query((BooleanQuery)sparqlQuery, output, format);
                        } else if (sparqlQuery instanceof GraphQuery) {
                            query((GraphQuery)sparqlQuery, output, format);
                        } else {
                            throw new InvalidArgumentException("SPARQL query type " + sparqlQuery.getClass() + " not supported!");
                        }

                        connection.commit();
                    } catch (Exception ex) {
                        connection.rollback();
                        throw ex;
                    } finally {
                        connection.close();
                    }
                } catch(RepositoryException e) {
                    log.error("error while getting repository connection: {}", e);
                    throw new MarmottaException("error while getting repository connection", e);
                } catch (QueryEvaluationException e) {
                    log.error("error while evaluating query: {}", e);
                    throw new MarmottaException("error while evaluating query ", e);
                } catch (MalformedQueryException e) {
                    log.error("error because malformed query: {}", e);
                    throw new MarmottaException("error because malformed query", e);
                }

                log.debug("SPARQL execution took {}ms", System.currentTimeMillis()-start);
                return Boolean.TRUE;
            }
        });

        try {
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            log.info("SPARQL query execution aborted due to timeout");
            future.cancel(true);
            throw new TimeoutException("SPARQL query execution aborted due to timeout (" + configurationService.getIntConfiguration("sparql.timeout",60)+"s)");
        } catch (ExecutionException e) {
            log.info("SPARQL query execution aborted due to exception: {}", e.getMessage());
            if(e.getCause() instanceof MarmottaException) {
                throw (MarmottaException)e.getCause();
            } else if(e.getCause() instanceof MalformedQueryException) {
                throw (MalformedQueryException)e.getCause();
            } else {
                throw new MarmottaException("unknown exception while evaluating SPARQL query", e.getCause());
            }
        }
    }

    /**
     * Execute a SPARQL update on the KiWi TripleStore. Throws a KiWiException in case the update execution fails.
     * <p/>
     * see http://www.w3.org/TR/sparql11-update/
     *
     * @param query a string representing the update query in SPARQL Update 1.1 syntax
     * @throws Exception
     */
    @Override
    public void update(URI context, String query) throws InvalidArgumentException, MarmottaException, MalformedQueryException, UpdateExecutionException {
        long start = System.currentTimeMillis();

        log.debug("executing SPARQL update:\n{}", query);

        try {
            RepositoryConnection connection = connectionService.getContextualConnection(context);
            try {
                connection.begin();
                Update update = connection.prepareUpdate(QueryLanguage.SPARQL,query,configurationService.getBaseUri());

                // workaround for a Sesame bug: we explicitly set the context for the query in the dataset
                DatasetImpl ds = new DatasetImpl();
                ds.addDefaultGraph(context);
                ds.addNamedGraph(context);
                ds.addDefaultRemoveGraph(context);
                ds.setDefaultInsertGraph(context);
                update.setDataset(ds);

                update.execute();
                connection.commit();
            } catch (UpdateExecutionException e) {
                connection.rollback();
                throw new MarmottaException("error while executing update",e);
            } catch (MalformedQueryException e) {
                connection.rollback();
                throw new MarmottaException("malformed query, update failed",e);
            } finally {
                connection.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while getting repository connection", ex);
            throw new MarmottaException("error while getting repository connection",ex);
        }
        log.debug("SPARQL update execution took {}ms", System.currentTimeMillis()-start);
    }



    // helpers
    private void query(TupleQuery query, TupleQueryResultWriter writer) throws QueryEvaluationException {
        try {
            query.evaluate(writer);
        } catch (TupleQueryResultHandlerException e) {
            throw new QueryEvaluationException("error while writing query tuple result: ",e);
        }
    }

    private void query(TupleQuery query, OutputStream output, String format) throws QueryEvaluationException {
        query(query, getTupleResultWriter(format, output));
    }

    private void query(BooleanQuery query, BooleanQueryResultWriter writer) throws QueryEvaluationException {
        try {
            writer.handleBoolean(query.evaluate());
        } catch (QueryResultHandlerException e) {
            throw new QueryEvaluationException("error while writing query boolean result: ",e);
        }
    }

    private void query(BooleanQuery query, OutputStream output, String format) throws QueryEvaluationException {
        query(query, getBooleanResultWriter(format, output));
    }

    private void query(GraphQuery query, OutputStream output, String format) throws QueryEvaluationException {
        query(query, output, Rio.getWriterFormatForMIMEType(format, RDFFormat.RDFXML));
    }

    private void query(GraphQuery query, OutputStream output, RDFFormat format) throws QueryEvaluationException {
        try {
            QueryResultIO.write(query.evaluate(), format, output);
        } catch (IOException e) {
            throw new QueryEvaluationException("error while writing query graph result: ",e);
        }
        catch(RDFHandlerException e) {
            throw new QueryEvaluationException("error while writing query graph result: ",e);
        }
        catch(UnsupportedRDFormatException e) {
            throw new QueryEvaluationException("Could not find requested output RDF format for results of query: ",e);
        }
    }

    private TupleQueryResultWriter getTupleResultWriter(String format, OutputStream os) {
        TupleQueryResultFormat resultFormat;
        if(format == null) {
            resultFormat = TupleQueryResultFormat.SPARQL;
        } else {
            resultFormat = QueryResultIO.getWriterFormatForMIMEType(format);
            if(resultFormat == null) {
                throw new InvalidArgumentException("could not produce format "+format);
            }
        }
        return QueryResultIO.createWriter(resultFormat, os);
    }

    private BooleanQueryResultWriter getBooleanResultWriter(String format, OutputStream os) {
        BooleanQueryResultFormat resultFormat;
        if(format == null) {
            resultFormat = BooleanQueryResultFormat.SPARQL;
        } else {
            resultFormat = QueryResultIO.getBooleanWriterFormatForMIMEType(format);
            if(resultFormat == null) {
                throw new InvalidArgumentException("could not produce format "+format);
            }
        }
        return QueryResultIO.createWriter(resultFormat, os);
    }

}
