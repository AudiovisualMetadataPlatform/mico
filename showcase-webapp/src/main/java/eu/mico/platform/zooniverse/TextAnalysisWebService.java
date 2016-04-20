package eu.mico.platform.zooniverse;

import com.google.common.collect.ImmutableMap;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.zooniverse.model.TextAnalysisInput;
import eu.mico.platform.zooniverse.model.TextAnalysisOutput;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
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
import java.util.*;

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
    private final PersistenceService persistenceService;

    public TextAnalysisWebService(EventManager eventManager, MICOBroker broker) {
        this.eventManager = eventManager;
        this.broker = broker;
        this.persistenceService = eventManager.getPersistenceService();
    }

    private static final URI ExtractorURI = new URIImpl("http://www.mico-project.eu/services/ner-text");

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response uploadImage(TextAnalysisInput input) {

        ServiceGraph dependencies = broker.getDependencies();

        boolean extractorRunning = false;

        for(URI descriptorURI : dependencies.getDescriptorURIs()) {
            if(descriptorURI.equals(ExtractorURI)) {
                extractorRunning = true;
                break;
            }
        }

        if(!extractorRunning) return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(String.format("Extractor '%s' currently not active",ExtractorURI.stringValue()))
                .build();

        try {
            final Item item = persistenceService.createItem();

            final Part part = item.createPart(ExtractorURI);
            part.setSyntacticalType("text/plain");
            try (OutputStream outputStream = part.getAsset().getOutputStream()) {
                IOUtils.copy(IOUtils.toInputStream(input.comment), outputStream);
            } catch (IOException e) {
                log.error("Could not persist text data for ContentPart {}: {}", part.getURI(), e.getMessage());
                throw e;
            }

            eventManager.injectItem(item);

            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id",item.getURI(),"status","submitted"))
                    .link(java.net.URI.create(item.getURI().stringValue()), "contentItem")
                    .build();
        } catch (RepositoryException | IOException e) {
            log.error("Could not create ContentItem");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("{id:[^/]+}")
    public Response getResult(@PathParam("id") final String itemURI) {

        final Item item;
        try {
            item = persistenceService.getItem(new URIImpl(itemURI));
            if (item == null)
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find ContentItem '%s'", itemURI)).build();
        } catch (RepositoryException e) {
            log.error("Error getting content item {}: {}", itemURI, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        //test if it is still in progress
        final ItemState state = broker.getStates().get(itemURI);
        if (state != null && !state.isFinalState()) {
            return Response.status(Response.Status.ACCEPTED)
                    .entity(ImmutableMap.of("id",itemURI,"status","inProgress"))
                    .build();
        }

        final TextAnalysisOutput out;
        try {
            out = getTextResult(item);

            if(out == null) {
                throw new Exception("Analysis result is empty");
            }

        } catch (Exception e) {
            log.error("Cannot load analysis results for '{}'",item, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        return Response.status(Response.Status.OK)
                .entity(out)
                .build();

    }
    private static String queryEntities = "PREFIX fam: <http://vocab.fusepool.info/fam#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "SELECT DISTINCT ?uri ?label WHERE {\n" +
            "  <%s>\n" +
            "  mico:hasContentPart/mico:hasContent/oa:hasBody ?body.\n" +
            "  ?body a fam:LinkedEntity; fam:entity-label ?label; fam:entity-reference ?uri.\n" +
            "}";

    private static String queryTopics = "PREFIX fam: <http://vocab.fusepool.info/fam#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "SELECT DISTINCT ?uri ?label ?confidence WHERE {\n" +
            "  <%s>\n" +
            "  mico:hasContentPart/mico:hasContent/oa:hasBody ?body.\n" +
            "  ?body a fam:TopicAnnotation; fam:topic-label ?label; fam:topic-reference ?uri; fam:confidence ?confidence.\n" +
            "}";

    private static String querySentiment = "PREFIX fam: <http://vocab.fusepool.info/fam#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "SELECT ?sentiment WHERE {\n" +
            "  <%s>\n" +
            "  mico:hasContentPart/mico:hasContent/oa:hasBody ?body.\n" +
            "  ?body a fam:SentimentAnnotation; fam:sentiment ?sentiment.\n" +
            "}";

    // TODO: refactor, because model changed: Metadata object does not exist anymore. Dont use sparql queries, use anno4j
    private TextAnalysisOutput getTextResult(Item item) throws Exception {
        try {
            TextAnalysisOutput out = new TextAnalysisOutput(item);

            out.sentiment = querySentiment(item);
            out.entities = queryList(item);
            out.topics = queryList(item);

            return out;
        } catch (MalformedQueryException | QueryEvaluationException e) {
            log.error("Error querying objects; {}", e);
            throw new Exception(e);
        }
    }

    private List queryList(Item ci) throws QueryEvaluationException, MalformedQueryException, RepositoryException {

        List res = new ArrayList<>();
//        final TupleQueryResult result = metadata.query(query);
//
//        try {
//            while (result.hasNext()) {
//                HashMap map = new HashMap<>();
//                BindingSet bindings = result.next();
//                for(String name : result.getBindingNames()) {
//                    Value v = bindings.getBinding(name).getValue();
//                    if(v instanceof Literal) {
//                        Literal l = (Literal) v;
//                        try {
//                            map.put(name,l.doubleValue());//workaround !
//                        } catch (IllegalArgumentException e) {
//                            map.put(name,l.stringValue());
//                        }
//                    } else {
//                        map.put(name, v.stringValue());
//                    }
//                }
//                res.add(map);
//            }
//        } finally {
//            result.close();
//        }
        return res;
    }

    private Object querySentiment(Item item) throws QueryEvaluationException, MalformedQueryException, RepositoryException {

//        String query = String.format(querySentiment, ci.getURI().stringValue());
//
//        final TupleQueryResult result = metadata.query(query);
//
//        try {
//            if (result.hasNext()) {
//                return ((Literal)result.next().getBinding(result.getBindingNames().get(0)).getValue()).doubleValue();
//            }
//        } finally {
//            result.close();
//        }

        return null;
    }

    private URI concatUrlWithPath(String baseURL, String extraPath) throws URISyntaxException{
        java.net.URI baseURI = new java.net.URI(baseURL).normalize();
        String newPath = baseURI.getPath() + "/" + extraPath;
        java.net.URI newURI = baseURI.resolve(newPath);
        return new URIImpl(newURI.normalize().toString());
    }

}