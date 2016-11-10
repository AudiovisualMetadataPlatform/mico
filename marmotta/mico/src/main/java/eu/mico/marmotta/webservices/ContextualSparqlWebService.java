/*
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
package eu.mico.marmotta.webservices;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import eu.mico.marmotta.api.ContextualConnectionService;
import eu.mico.marmotta.api.ContextualSparqlService;
import eu.mico.marmotta.api.QueryType;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.exporter.ExportService;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.util.WebServiceUtil;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Contextual SPARQL Web Service
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
@Path("/{contexts:" + ContextualConnectionService.CONTEXT_IDS_PATTERN + "}/" + ContextualSparqlWebService.PATH)
public class ContextualSparqlWebService extends ContextualWebServiceBase {

    public static final String PATH = "sparql";
    public static final String SELECT = "/select";
    public static final String UPDATE = "/update";
    public static final int TIMEOUT = 60;

    @Inject
    private Logger log;

    @Inject
    private ExportService exportService;

    @Inject
    private ContextualSparqlService sparqlService;

    public ContextualSparqlWebService() {
        super();
        outputMapper.remove("json");
        outputMapper.put("json", "application/sparql-results+json");
        outputMapper.put("xml", "application/sparql-results+xml");
        outputMapper.put("tabs", "text/tab-separated-values");
        outputMapper.put("csv", "text/csv");
    }

    /**
     * Single SPARQL endpoint, redirecting to the actual select endpoint
     * when possible
     */
    @GET
    public Response get(@QueryParam("query") String query, @QueryParam("update") String update, @Context HttpServletRequest request) throws URISyntaxException {
        if (StringUtils.isNotBlank(update)) {
            String msg = "update operations are not supported through get"; //or yes?
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } else {
            UriBuilder builder = UriBuilder.fromPath(PATH + SELECT);
            if (StringUtils.isNotBlank(query)) {
                builder.replaceQuery(request.getQueryString());
            }
            return Response.seeOther(builder.build()).build();
        }
    }

    /**
     * Single endpoint for direct post queries (not yet implemented)
     *
     * @param request
     * @return
     */
    @POST
    public Response post(@Context HttpServletRequest request) {
        String msg = "impossible to determine which type of operation (query/update) the request contains";
        log.error(msg);
        return Response.status(Response.Status.CONFLICT).entity(msg).build();
    }

    /**
     * Execute a SPARQL 1.1 tuple query on the LMF triple store using the query passed as query parameter to the
     * GET request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param query      the SPARQL 1.1 Query as a string parameter
     * @param out the format for serializing the query results ("html", "json", or "xml")
     * @return the query result in the format passed as argument
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     */
    @GET
    @Path(SELECT)
    public Response selectGet(
            @PathParam("contexts") String contexts,
            @QueryParam("query") String query,
            @QueryParam(OUT) String out,
            @Context HttpServletRequest request) {
        return select(query, checkInOutParameter(out), TIMEOUT, request, resolveContexts(contexts));
    }

    /**
     * Execute a SPARQL 1.1 tuple query on the LMF triple store using the query passed as form parameter to the
     * POST request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param query      the SPARQL 1.1 Query as a string parameter
     * @param out the format for serializing the query results ("html", "json", or "xml")
     * @return the query result in the format passed as argument
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     */
    @POST
    @Consumes({"application/x-www-url-form-urlencoded", "application/x-www-form-urlencoded"})
    @Path(SELECT)
    public Response selectPostForm(
            @PathParam("contexts") String contexts,
            @FormParam("query") String query,
            @QueryParam(OUT) String out,
            @Context HttpServletRequest request) {
        return select(query, checkInOutParameter(out), TIMEOUT, request, resolveContexts(contexts));
    }

    /**
     * Execute a SPARQL 1.1 tuple query on the LMF triple store using the query passed in the body of the
     * POST request. Result will be formatted using the result type passed as argument (either "html", "json" or "xml").
     * <p/>
     * see SPARQL 1.1 Query syntax at http://www.w3.org/TR/sparql11-query/
     *
     * @param request    the servlet request (to retrieve the SPARQL 1.1 Query passed in the body of the POST request)
     * @param out the format for serializing the query results ("html", "json", or "xml")
     * @return the query result in the format passed as argument
     * @HTTP 200 in case the query was executed successfully
     * @HTTP 500 in case there was an error during the query evaluation
     */
    @POST
    @Path(SELECT)
    public Response selectPost(
            @PathParam("contexts") String contexts,
            @QueryParam(OUT) String out,
            @Context HttpServletRequest request) {
        try {
            String query = CharStreams.toString(request.getReader());
            return select(query, checkInOutParameter(out), TIMEOUT, request, resolveContexts(contexts));
        } catch (IOException e) {
            log.error("body not found", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Actual SELECT implementation
     *
     * @param contexts
     * @param query
     * @param format
     * @param request
     * @return
     */
    private Response select(String query, String format, int timeout, HttpServletRequest request, URI... contexts) {
        try {
            if (StringUtils.isBlank(query)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("no SPARQL query specified").build();
            } else {
                QueryType queryType = sparqlService.getQueryType(query);
                List<ContentType> offeredTypes;
                switch (queryType) {
                    case TUPLE:
                        offeredTypes = MarmottaHttpUtils.parseQueryResultFormatList(TupleQueryResultWriterRegistry.getInstance().getKeys());
                        break;
                    case BOOL:
                        offeredTypes = MarmottaHttpUtils.parseQueryResultFormatList(BooleanQueryResultWriterRegistry.getInstance().getKeys());
                        break;
                    case GRAPH:
                        List<String> producedTypes = new ArrayList<String>(exportService.getProducedTypes());
                        producedTypes.remove("application/xml");
                        producedTypes.remove("text/plain");
                        producedTypes.remove("text/html");
                        producedTypes.remove("application/xhtml+xml");
                        offeredTypes = Lists.transform(producedTypes, new Function<String,ContentType>() {
                            @Override
                            public ContentType apply(String s) {
                                String[] split = s.split("/");
                                return new ContentType(split[0], split[1]);
                            }
                        });
                        break;
                    default:
                        return Response.status(Response.Status.BAD_REQUEST).entity("no result format specified or unsupported result format").build();
                }

                ContentType bestType = performContentNegotiation(format, enumToList(request.getHeaders("Accept")), offeredTypes);
                if (bestType == null) {
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity("no result format specified or unsupported result format").build();
                } else {
                    return buildQueryResponse(bestType, query, timeout, contexts);
                }
            }
        } catch (InvalidArgumentException e) {
            log.error("query parsing threw an exception", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("query execution threw an exception", e);
            return Response.serverError().entity("query not supported").build();
        }
    }

    /**
     * Execute a SPARQL 1.1 Update request passed in the query parameter of the GET. The update will
     * be carried out
     * on the LMF triple store.
     * <p/>
     * see SPARQL 1.1 Update syntax at http://www.w3.org/TR/sparql11-update/
     *
     * @param update the update query in SPARQL 1.1 syntax
     * @param query  the update query in SPARUL syntax
     * @return empty content in case the update was successful, the error message in case an error occurred
     * @HTTP 200 in case the update was carried out successfully
     * @HTTP 500 in case the update was not successful
     */
    @GET
    @Path(UPDATE)
    public Response updateGet(@PathParam("contexts") String contexts, @QueryParam("update") String update, @QueryParam("query") String query, @QueryParam(OUT) String resultType, @Context HttpServletRequest request) {
        String q = getUpdateQuery(update, query);
        try {
            return update(resolveContext(contexts), q, resultType, request);
        } catch (MarmottaException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }

    /**
     * Execute a SPARQL 1.1 Update request using update via POST directly;
     * see details at http://www.w3.org/TR/sparql11-protocol/\#update-operation
     *
     * @param request the servlet request (to retrieve the SPARQL 1.1 Update query passed in the
     *                body of the POST request)
     * @return empty content in case the update was successful, the error message in case an error
     * occurred
     * @HTTP 200 in case the update was carried out successfully
     * @HTTP 400 in case the update query is missing or invalid
     * @HTTP 500 in case the update was not successful
     */
    @POST
    @Path(UPDATE)
    @Consumes("application/sparql-update")
    public Response updatePostDirectly(@PathParam("contexts") String contexts, @Context HttpServletRequest request, @QueryParam(OUT) String resultType) {
        try {
            String q = CharStreams.toString(request.getReader());
            return update(resolveContext(contexts), q, resultType, request);
        } catch (MarmottaException | IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }

    /**
     * Execute a SPARQL 1.1 Update request using update via URL-encoded POST;
     * see details at http://www.w3.org/TR/sparql11-protocol/\#update-operation
     *
     * @param request the servlet request (to retrieve the SPARQL 1.1 Update query passed in the
     *                body of the POST request)
     * @return empty content in case the update was successful, the error message in case an error
     * occurred
     * @HTTP 200 in case the update was carried out successfully
     * @HTTP 400 in case the update query is missing or invalid
     * @HTTP 500 in case the update was not successful
     */
    @POST
    @Path(UPDATE)
    @Consumes({"application/x-www-url-form-urlencoded", "application/x-www-form-urlencoded"})
    public Response updatePostUrlEncoded(@PathParam("contexts") String contexts, @Context HttpServletRequest request) {
        try {
            Map<String, String> params = parseEncodedQueryParameters(CharStreams.toString(request.getReader()));
            String q = StringUtils.defaultString(params.get("update"));
            String resultType = StringUtils.defaultString(params.get(OUT));
            return update(resolveContext(contexts), q, resultType, request);
        } catch (MarmottaException | IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }

    /**
     * Actual update implementation
     */
    private Response update(URI context, String update, String resultType, HttpServletRequest request) {
        try {
            if (StringUtils.isNotBlank(update)) {
                sparqlService.update(context, update);
                return Response.ok().build();
            } else { // else nothing to do
                return Response.accepted("empty SPARQL update, nothing to do").build();
            }

        } catch (MalformedQueryException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(ex)).build();
        } catch (UpdateExecutionException e) {
            log.error("update execution threw an exception", e);
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        } catch (MarmottaException e) {
            return Response.serverError().entity(WebServiceUtil.jsonErrorResponse(e)).build();
        }
    }

    /**
     * Get right update query from both possible parameters, for keeping
     * backward compatibility with the old parameter
     *
     * @param update update parameter
     * @param query  query parameter
     * @return
     */
    private String getUpdateQuery(String update, String query) {
        if (StringUtils.isNotBlank(update))
            return update;
        else if (StringUtils.isNotBlank(query)) {
            log.warn("Update query still uses the old 'query' parameter");
            return query;
        } else
            return null;
    }

    /**
     * Parse the encoded query parameters
     *
     * @param body
     * @return parameters
     * @todo this should be somewhere already implemented
     */
    private Map<String, String> parseEncodedQueryParameters(String body) {
        Map<String, String> params = new HashMap<String, String>();
        for (String pair : body.split("&")) {
            int eq = pair.indexOf("=");
            try {
                if (eq < 0) {
                    // key with no value
                    params.put(URLDecoder.decode(pair, "UTF-8"), "");
                } else {
                    // key=value
                    String key = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                    params.put(key, value);
                }
            } catch (UnsupportedEncodingException e) {
                log.error("Query parameter cannot be decoded: {}", e.getMessage(), e);
            }
        }
        return params;
    }

    private Response buildQueryResponse(final ContentType format, final String query, final int timeout, final URI... contexts) throws Exception {
        final StreamingOutput entity = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    sparqlService.query(query, output, format.getMime(), timeout > 0 ? timeout : configurationService.getIntConfiguration("sparql.timeout", 120), contexts);
                } catch (MarmottaException ex) {
                    throw new WebApplicationException(ex.getCause(), Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(ex)).build());
                } catch (MalformedQueryException e) {
                    throw new WebApplicationException(e.getCause(), Response.status(Response.Status.BAD_REQUEST).entity(WebServiceUtil.jsonErrorResponse(e)).build());
                } catch (TimeoutException e) {
                    throw new WebApplicationException(e.getCause(), Response.status(Response.Status.GATEWAY_TIMEOUT).entity(WebServiceUtil.jsonErrorResponse(e)).build());
                }
            }
        };
        return Response.ok().entity(entity).header("Content-Type", format.getMime()).build();
    }

}
