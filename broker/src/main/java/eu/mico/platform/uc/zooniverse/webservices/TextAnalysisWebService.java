package eu.mico.platform.uc.zooniverse.webservices;

import com.google.common.collect.ImmutableMap;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.uc.zooniverse.model.TextAnalysisInput;
import eu.mico.platform.uc.zooniverse.model.TextAnalysisOutput;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@Path("zooniverse/textanalysis")
public class TextAnalysisWebService {

    private static final Logger log = LoggerFactory.getLogger(TextAnalysisWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", DateFormatSymbols.getInstance(Locale.US));
    static {
        ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final EventManager eventManager;
    private final MICOBroker broker;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;

    public TextAnalysisWebService(EventManager eventManager, MICOBroker broker, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.broker = broker;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response uploadImage(TextAnalysisInput input) {

        //TODO checks

        try {
            final ContentItem ci = persistenceService.createContentItem();

            for(String comment: input.comments) {
                final Content contentPart = ci.createContentPart();
                contentPart.setType("text/plain");
                contentPart.setRelation(DCTERMS.CREATOR, new URIImpl("http://www.mico-project.eu/broker/zooniverse-text-analysis-web-service"));
                contentPart.setProperty(DCTERMS.CREATED, ISO8601FORMAT.format(new Date()));
                try (OutputStream outputStream = contentPart.getOutputStream()) {
                    IOUtils.copy(IOUtils.toInputStream(comment), outputStream);
                } catch (IOException e) {
                    log.error("Could not persist text data for ContentPart {}: {}", contentPart.getURI(), e.getMessage());
                    throw e;
                }
            }

            eventManager.injectContentItem(ci);

            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id",ci.getID(),"status","submitted","link",ci.getURI().stringValue()))
                    .link(java.net.URI.create(ci.getURI().stringValue()), "contentItem")
                    .build();
        } catch (RepositoryException | IOException e) {
            log.error("Could not create ContentItem");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getResult(@PathParam("contentItemID") final String contentItemId) {

        final URI contentItemUri;

        //get content item
        try {
            contentItemUri = concatUrlWithPath(marmottaBaseUri, contentItemId);
        }
        catch (URISyntaxException e) {
            log.error("Bad format for content item id '{}'", contentItemId);
            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("Bad format for content item id '%s'", contentItemId)).build();
        }

        final ContentItem contentItem;
        try {
            contentItem = persistenceService.getContentItem(contentItemUri);
            if (contentItem == null)
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentItem '%s'", contentItemUri.stringValue())).build();
        } catch (RepositoryException e) {
            log.error("Error getting content item {}: {}", contentItemUri, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        //test if it is still in progress
        final ContentItemState state = broker.getStates().get(contentItemUri.stringValue());
        if (state != null && !state.isFinalState()) {
            return Response.status(Response.Status.ACCEPTED)
                    .entity(ImmutableMap.of("id",contentItemId,"status","inProgress","link",contentItemUri))
                    .build();
        }

        final TextAnalysisOutput out;
        try {
            out = getResult(contentItem);

            if(out == null) {
                throw new Exception("Analysis result is empty");
            }

        } catch (Exception e) {
            log.error("Cannot load analysis results for '{}'",contentItem, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        return Response.status(Response.Status.OK)
                .entity(out)
                .build();

    }

    private TextAnalysisOutput getResult(ContentItem ci) throws Exception {
        TextAnalysisOutput out = new TextAnalysisOutput(ci);

        final Metadata metadata = persistenceService.getMetadata();

        try {

            final String query = "";//TODO

            final TupleQueryResult result = metadata.query(query);

            try {
                while (result.hasNext()) {
                    result.next().getBinding("value").getValue().stringValue();
                }
            } finally {
                result.close();
            }
        } catch (MalformedQueryException | QueryEvaluationException e) {
            log.error("Error querying objects; {}", e);
            throw new Exception(e);
        }

        return out;
    }

    private URI concatUrlWithPath(String baseURL, String extraPath) throws URISyntaxException{
        java.net.URI baseURI = new java.net.URI(baseURL).normalize();
        String newPath = baseURI.getPath() + "/" + extraPath;
        java.net.URI newURI = baseURI.resolve(newPath);
        return new URIImpl(newURI.normalize().toString());
    }

}
