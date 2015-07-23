/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.uc.zooniverse.webservices;

import com.sun.jersey.api.client.ClientResponse;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
@Path("zooniverse/{subjectID:[^/]+}")
public class ZooniverseWebService {

    private static final Logger log = LoggerFactory.getLogger(ZooniverseWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", DateFormatSymbols.getInstance(Locale.US));
    static {
        ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final EventManager eventManager;
    private final MICOBroker broker;
    private final PersistenceService persistenceService;
    private final CloseableHttpClient httpClient;

    public ZooniverseWebService(EventManager eventManager, MICOBroker broker) {
        this.eventManager = eventManager;
        this.broker = broker;

        this.persistenceService = eventManager.getPersistenceService();
        this.httpClient = HttpClientBuilder.create()
                .setUserAgent("MicoPlatform (ZooniverseWebService)")
                .build();
    }

    @PUT
    @Produces("application/json")
    public Response sendURLs(@PathParam("subjectID") final String subjectId, @QueryParam("url") final java.net.URI imageUrl) {
        try {
            final HttpGet request = new HttpGet(imageUrl);
            return httpClient.execute(request, new ResponseHandler<Response>() {
                @Override
                public Response handleResponse(HttpResponse httpResponse) throws IOException {
                    final StatusLine statusLine = httpResponse.getStatusLine();
                    if (statusLine.getStatusCode() == 200) {
                        final Header cType = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
                        if (cType != null) {
                            final MediaType type = MediaType.valueOf(cType.getValue());
                            try (InputStream is = httpResponse.getEntity().getContent()) {
                                return uploadImage(subjectId, type, is);
                            }
                        } else {
                            throw new ClientProtocolException("Could not determine ContentType of remote resource " + imageUrl);
                        }
                    }

                    throw new ClientProtocolException(String.format("HTTP-%d: %s", statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                }
            });
        } catch (ClientProtocolException e) {
            return Response.status(ClientResponse.Status.BAD_GATEWAY)
                    .entity(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not fetch image to create ContentItem for subjectId " + subjectId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @POST
    @Consumes("image/*")
    @Produces("application/json")
    public Response uploadImage(@PathParam("subjectID") String subjectId, @HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type, @Context InputStream postBody) {
        if (type == null || postBody == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("ContentType and postBody required!")
                    .build();
        }
        try {
            final URI uri = getContentItemUriForSubjectId(subjectId);
            if (uri != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(String.format("ContentItem with SubjectId '%s' already exists: <%s>%n", subjectId, uri))
                        .build();
            } else {
                final ContentItem ci = persistenceService.createContentItem();
                setSubjectIdForContentItemUri(ci.getURI(), subjectId);

                final Content contentPart = ci.createContentPart();
                contentPart.setType(String.format("%s/%s", type.getType(), type.getSubtype()));
                contentPart.setRelation(DCTERMS.CREATOR, new URIImpl("http://www.mico-project.eu/broker/zooniverse-web-service"));
                contentPart.setProperty(DCTERMS.CREATED, ISO8601FORMAT.format(new Date()));

                try (OutputStream outputStream = contentPart.getOutputStream()) {
                    IOUtils.copy(postBody, outputStream);
                } catch (IOException e) {
                    log.error("Could not persist binary data for ContentPart {}: {}", contentPart.getURI(), e.getMessage());
                    throw e;
                }

                eventManager.injectContentItem(ci);
                Map<String, Object> rspEntity = new HashMap<>();
                rspEntity.put("subjectId", subjectId);
                rspEntity.put("contentItem", ci.getURI());
                rspEntity.put("contentPart", contentPart.getURI());
                rspEntity.put("status", "submitted");

                return Response.status(Response.Status.ACCEPTED)
                        .entity(rspEntity)
                        .link(java.net.URI.create(ci.getURI().stringValue()), "contentItem")
                        .link(java.net.URI.create(contentPart.getURI().stringValue()), "contentPart")
                        .build();
            }
        } catch (RepositoryException | IOException e) {
            log.error("Could not create ContentItem for subjectId " + subjectId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getResult(@PathParam("subjectID") String subjectId) {
        try {
            final URI uri = getContentItemUriForSubjectId(subjectId);
            if (uri == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentItem with subjectId '%s'%n", subjectId)).build();
            } else {
                final ContentItem contentItem = persistenceService.getContentItem(uri);
                final ContentItemState state = broker.getStates().get(uri.stringValue());
                Map<String, Object> rspEntity = new HashMap<>();
                rspEntity.put("subjectId", subjectId);
                rspEntity.put("contentItem", contentItem.getURI().stringValue());

                final Response.Status status;
                if (state.isFinalState()) {
                    // FIXME: Change status to OK (200) after implementing this part.
                    // status = Response.Status.OK;
                    status = Response.Status.NOT_IMPLEMENTED;

                    rspEntity.put("status", "finished");
                    // TODO: Get the analysis results (anno4j? SPARQL?)
                    rspEntity.put("message", "Retrieving analysis result not yet implemented. Sorry :-(");
                } else {
                    status = Response.Status.ACCEPTED;

                    rspEntity.put("status", "inProgress");
                    rspEntity.put("message", String.format("Analysis of subject '%s' (<%s>) is not yet complete. Please try again later!", subjectId, uri));
                }
                return Response.status(status)
                        .entity(rspEntity)
                        .link(java.net.URI.create(uri.stringValue()), "contentItem")
                        .build();
            }
        } catch (RepositoryException e) {
            log.error("Could not load ContentItem for subjectId " + subjectId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @DELETE
    public Response deleteSubject(@PathParam("subjectID") String subjectId) {
        try {
            final URI contentItem = getContentItemUriForSubjectId(subjectId);
            if (contentItem == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentItem with subjectId '%s'%n", subjectId)).build();
            } else {
                persistenceService.deleteContentItem(contentItem);
                return Response.noContent().build();
            }
        } catch (RepositoryException e) {
            log.error("Could not delete ContentItem for subjectId " + subjectId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }


    private URI getContentItemUriForSubjectId(String subjectId) throws RepositoryException {
        final Metadata metadata = persistenceService.getMetadata();

        try {
            final TupleQueryResult result = metadata.query(String.format("SELECT ?uri WHERE { ?uri a <%s>; <%s> \"%s\".}", "http://www.mico-project.eu/ns/platform/1.0/schema#ContentItem", DCTERMS.IDENTIFIER, subjectId));
            try {
                if (result.hasNext()) {
                    return (URI) result.next().getBinding("uri").getValue();
                }
                return null;
            } finally {
                result.close();
            }
        } catch (QueryEvaluationException | MalformedQueryException e) {
            e.printStackTrace();
            return null;
        }

    }

    private void setSubjectIdForContentItemUri(URI contentItem, String subjectId) throws RepositoryException {
        final Metadata metadata = persistenceService.getMetadata();

        try {
            metadata.update(String.format("INSERT DATA { <%s> <%s> \"%s\". }", contentItem, DCTERMS.IDENTIFIER, subjectId));
        } catch (MalformedQueryException | UpdateExecutionException e) {
            e.printStackTrace();
        }
    }

}
