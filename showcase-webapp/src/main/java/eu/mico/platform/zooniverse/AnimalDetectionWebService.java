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
package eu.mico.platform.zooniverse;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.zooniverse.util.BrokerServices;
import eu.mico.platform.zooniverse.util.ItemData;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
@Path("/zooniverse/animaldetection")
public class AnimalDetectionWebService {

    private static final Logger log = LoggerFactory.getLogger(AnimalDetectionWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", DateFormatSymbols.getInstance(Locale.US));
    static {
        ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final MimetypesFileTypeMap mimetypesMap = new MimetypesFileTypeMap();

    private final EventManager eventManager;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;
    private final BrokerServices brokerSvc;
    private final CloseableHttpClient httpClient;

    private static final String ExtractorURI = "http://www.mico-project.eu/services/animal-detection";


    public AnimalDetectionWebService(EventManager eventManager, String marmottaBaseUri, BrokerServices brokerSvc) {
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
        this.brokerSvc = brokerSvc;

        mimetypesMap.addMimeTypes("image/jpeg jpeg jpg");

        this.persistenceService = eventManager.getPersistenceService();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5 * 1000)
                .setConnectionRequestTimeout(5 * 1000)
                .setSocketTimeout(5 * 1000)
                .build();
        this.httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(10)
                .setUserAgent("MicoPlatform (ZooniverseWebService)")
                .build();
    }

    @PUT
    @Produces("application/json")
    public Response sendURLs(@QueryParam("url") final java.net.URI imageUrl) {
        try {
            final HttpGet request = new HttpGet(imageUrl);
            return httpClient.execute(request, new ResponseHandler<Response>() {
                @Override
                public Response handleResponse(HttpResponse httpResponse) throws IOException {
                    final StatusLine statusLine = httpResponse.getStatusLine();
                    if (statusLine.getStatusCode() == 200) {
                        final Header cType = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
                        final MediaType type;
                        if (cType != null && !MediaType.valueOf(cType.getValue()).equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                            type = MediaType.valueOf(cType.getValue());
                        } else {
                            type = MediaType.valueOf(mimetypesMap.getContentType(imageUrl.getPath().toLowerCase()));
                        }
                        log.debug("Content type for URL {} is {}, proceeding with type {}", imageUrl.toString(), cType.getValue(), type.toString());

                        if (type.toString().equalsIgnoreCase("image/jpeg")) {
                            try (InputStream is = httpResponse.getEntity().getContent()) {
                                return uploadImage(type, is);
                            }
                        } else if (type != null) {
                            throw new ClientProtocolException(String.format("Invalid MIME type %s of remote resource %s", type.toString(), imageUrl));
                        } else {
                            throw new ClientProtocolException("Could not determine MIME of remote resource " + imageUrl);
                        }
                    }

                    throw new ClientProtocolException(String.format("HTTP-%d: %s", statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                }
            });
        } catch (ClientProtocolException e) {
            log.error("Could not fetch image to create content item: %s", e.toString());
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not fetch image to create content item: {}", e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @POST
    @Consumes("image/*")
    @Produces("application/json")
    public Response uploadImage(@HeaderParam(HttpHeaders.CONTENT_TYPE) MediaType type, @Context InputStream postBody) {
        if (type == null || postBody == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("ContentType and postBody required!")
                    .build();
        }
        try {
            final Item item = persistenceService.createItem();

            final Part part = item.createPart(new URIImpl(ExtractorURI));
            part.setSemanticType("application/animaldetection-endpoint");
            String assetType = String.format("%s/%s", type.getType(), type.getSubtype());
            part.setSyntacticalType(assetType);

            Asset asset = part.getAsset();
            try (OutputStream outputStream = asset.getOutputStream()) {
                IOUtils.copy(postBody, outputStream);
                outputStream.close();
                asset.setFormat(assetType);
            } catch (IOException e) {
                log.error("Could not persist binary data for ContentPart {}: {}", part.getURI(), e.getMessage());
                throw e;
            } finally {
                postBody.close();
            }

            eventManager.injectItem(item);
            Map<String, Object> rspEntity = new HashMap<>();
            rspEntity.put("id", String.format("%s/%s", item.getURI(), part.getURI()));
            rspEntity.put("status", "submitted");

            return Response.status(Response.Status.CREATED)
                    .entity(rspEntity)
                    .link(java.net.URI.create(item.getURI().stringValue()), "contentItem")
                    .link(java.net.URI.create(part.getURI().stringValue()), "contentPart")
                    .build();
        } catch (RepositoryException | IOException e) {
            log.error("Could not create ContentItem");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @GET
    @Path("{contentItemID:[^/]+}/{contentPartID:[^/]+}")
    @Produces("application/json")
    public Response getResult(@PathParam("contentItemID") final String contentItemId, @PathParam("contentPartID") final String contentPartId) {

        final URI itemURI;
        final URI partURI;
        try {
            itemURI = concatUrlWithPath(marmottaBaseUri, contentItemId);
            partURI = concatUrlWithPath(marmottaBaseUri, String.format("%s/%s", contentItemId, contentPartId));
        }
        catch (URISyntaxException e) {
            log.error("Unable to create URI with marmotta base '{}', content item id '{}' and content part id '{}'", marmottaBaseUri, contentItemId, contentPartId);
            return Response.status(Response.Status.NOT_FOUND).entity(String.format("Unable to create URI with marmotta base '%s', content item id '%s' and content part id '%s'", marmottaBaseUri, contentItemId, contentPartId)).build();
        }

        final Item item;
        try {
            item = persistenceService.getItem(itemURI);
            if (item == null)
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentItem '%s'%n", itemURI.stringValue())).build();
        } catch (RepositoryException e) {
            log.error("Error getting content item {}: {}", itemURI, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        final Part part;
        try {

            part = item.getPart(partURI);
            if (part == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentPart '%s'%n", partURI.stringValue())).build();
            }
            if (part.getSyntacticalType() == null || !part.getSyntacticalType().startsWith("image/")) {
                log.error("The requested resource is not an image: {}", part.getURI());
                return Response.status(Response.Status.BAD_REQUEST).entity("The requested resource is not an image").build();
            }
        } catch (RepositoryException e) {
            log.error("Error getting content part{}: {}", partURI, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        Map<String, Object> rspEntity = new HashMap<>();

        try {
            final ItemData itemData = brokerSvc.getItemData(itemURI.stringValue());
            if (itemData != null && !itemData.hasFinished()) {
                rspEntity.put("status", "inProgress");
                return Response.status(Response.Status.ACCEPTED)
                        .entity(rspEntity)
                        .build();
            }
        } catch (IOException e) {
            log.error("Error getting status of item {} from broker: {}", itemURI.stringValue(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        try {
            List<Object> objects = getObjects(item);
            if (objects == null) {
                log.error("Empty objects list of content item: %s", itemURI.toString());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            rspEntity.put("objects", objects);
            rspEntity.put("objectsFound", objects.size());

            rspEntity.put("processingBegin", getProcessingBegin(item));
            rspEntity.put("processingEnd", getProcessingEnd(item));
        } catch (RepositoryException e) {
            log.error("Error processing queries: {}", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        //rspEntity.put("extractorVersion", null);
        rspEntity.put("status", "finished");

        return Response.status(Response.Status.OK)
                .entity(rspEntity)
                .build();

    }

    /***************
     * TODO adapt queries to new model
     ****************/
    private String getProcessingEnd(Item item) throws  RepositoryException {
        try {

            final String query = String.format(
                    "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n" +
                    "SELECT ?value WHERE {\n" +
                            " <%s>  mmm:hasPart> ?cp .\n" +
                            " ?cp <%s> \"text/vnd.fhg-hog-detector+xml\" .\n" + //TODO still correct?
                            " ?cp <%s> ?value .\n" +
                            "}",
                    item.getURI().stringValue(),
                    DCTERMS.TYPE,
                    DCTERMS.CREATED
            );

            final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

            try {
                if (result.hasNext()) {
                    return result.next().getBinding("value").getValue().stringValue();
                }
            } finally {
                result.close();
            }
        } catch (MalformedQueryException | QueryEvaluationException e) {
            log.error("Error querying objects; {}", e);
        }
        return null;
    }

    private String getProcessingBegin(Item item) throws  RepositoryException {

        try {

            final String query = String.format(
                    "SELECT ?value WHERE {\n" +
                            " <%s>   <http://www.w3.org/ns/oa#serializedAt> ?value .\n" + //TODO still correct?
                            "}",
                    item.getURI().stringValue()
            );

            final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

            try {
                if (result.hasNext()) {
                    return result.next().getBinding("value").getValue().stringValue();
                }
            } finally {
                result.close();
            }
        } catch (MalformedQueryException | QueryEvaluationException e) {
            log.error("Error querying objects; {}", e);
        }
        return null;
    }

    private List<Object> getObjects(Item item) throws RepositoryException{
        List<Object> rspObjects = new ArrayList<>();

        try {

            final String query = String.format(
                "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "SELECT ?animal ?region ?confidence ?version WHERE {\n" +
                    "<%s> mico:hasPart ?annot .\n" +
                    "?annot oa:hasBody ?body .\n" +
                    "?annot oa:hasTarget ?tgt .\n" +
                    "?tgt  oa:hasSelector ?fs .\n" +
                    "?body rdf:value ?animal .\n" +
                    "?body mmm:hasConfidence ?confidence .\n" + //TODO still correct
                    "?body mmm:hasExtractionVersion ?version .\n" + //TODO still correct
                    "?fs rdf:value ?region\n" +
                "FILTER EXISTS {?body rdf:type mmm:AnimalDetectionBody}\n" + //TODO still correct
                "}",
                item.getURI().stringValue()
            );

            final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

            try {
                while (result.hasNext()) {
                    Map<String, Object> rspObject = new HashMap<>();
                    BindingSet bindingSet = result.next();
                    rspObject.put("animal", bindingSet.getBinding("animal").getValue().stringValue());
                    rspObject.put("confidence", bindingSet.getBinding("confidence").getValue().stringValue());
                    rspObject.put("algorithmVersion", bindingSet.getBinding("version").getValue().stringValue());
                    String value = bindingSet.getBinding("region").getValue().stringValue();
                    final Pattern fragmentPattern = Pattern.compile("#xywh=(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)");
                    Matcher matcher = fragmentPattern.matcher(value);
                    if (!matcher.matches()) {
                        log.error("Error parsing value {}", value);
                        return null;
                    }
                    rspObject.put("x", Integer.parseInt(matcher.group(1)));
                    rspObject.put("y", Integer.parseInt(matcher.group(2)));
                    rspObject.put("w", Integer.parseInt(matcher.group(3)));
                    rspObject.put("h", Integer.parseInt(matcher.group(4)));
                    rspObjects.add(rspObject);
                }
            } finally {
                result.close();
            }
        } catch (MalformedQueryException | QueryEvaluationException e) {
            log.error("Error querying objects; {}", e);
            return null;
        }
        return rspObjects;
    }

    @DELETE
    @Path("{contentItemID:[^/]+}/{contentPartID:[^/]+}")
    public Response deleteSubject(@PathParam("contentItemID") final String contentItemId, @PathParam("contentPartID") final String contentPartId) {
        final URI contentItemUri;
        try {
            contentItemUri = concatUrlWithPath(marmottaBaseUri, contentItemId);
        }
        catch (URISyntaxException e) {
            log.error("Unable to create URI with marmotta base '{}', content item id '{}' and content part id '{}'", marmottaBaseUri, contentItemId, contentPartId);
            return Response.status(Response.Status.NOT_FOUND).entity(String.format("Unable to create URI with marmotta base '%s', content item id '%s' and content part id '%s'", marmottaBaseUri, contentItemId, contentPartId)).build();
        }

        final Item item;
        try {
            item = persistenceService.getItem(contentItemUri);
            if (item == null)
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentItem '%s'%n", contentItemUri.stringValue())).build();

            persistenceService.deleteItem(contentItemUri);
        } catch (RepositoryException e) {
            log.error("Error removing content item {}: {}", contentItemUri, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }



        return Response.noContent().build();
    }

    private URI concatUrlWithPath(String baseURL, String extraPath) throws URISyntaxException{
        java.net.URI baseURI = new java.net.URI(baseURL).normalize();
        String newPath = baseURI.getPath() + "/" + extraPath;
        java.net.URI newURI = baseURI.resolve(newPath);
        return new URIImpl(newURI.normalize().toString());
    }

}
