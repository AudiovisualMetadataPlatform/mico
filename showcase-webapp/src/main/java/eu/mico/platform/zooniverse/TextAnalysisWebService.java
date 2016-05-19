package eu.mico.platform.zooniverse;

import com.google.common.collect.ImmutableMap;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.zooniverse.model.TextAnalysisInput;
import eu.mico.platform.zooniverse.model.TextAnalysisOutput;
import eu.mico.platform.zooniverse.util.BrokerServices;
import eu.mico.platform.zooniverse.util.ItemData;
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
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@Path("/zooniverse/textanalysis")
public class TextAnalysisWebService {

    private static final Logger log = LoggerFactory.getLogger(TextAnalysisWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", DateFormatSymbols.getInstance(Locale.US));
    static {
        ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final EventManager eventManager;
    private final PersistenceService persistenceService;
    private final BrokerServices brokerSvc;

    public TextAnalysisWebService(EventManager eventManager, BrokerServices broker) {
        this.eventManager = eventManager;
        this.persistenceService = eventManager.getPersistenceService();
        this.brokerSvc = broker;
    }

    private static final String ExtractorURI = "http://www.mico-project.eu/services/ner-text";

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response uploadImage(TextAnalysisInput input) throws  IOException{

        boolean extractorRunning = false;

        for(Map<String, String>service : this.brokerSvc.getServices()) {
            if(service.containsKey("uri") && service.get("uri").equals(ExtractorURI)) {
                extractorRunning = true;
                break;
            }
        }

        if(!extractorRunning) return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(String.format("Extractor '%s' currently not active",ExtractorURI))
                .build();

        try {
            final Item item = persistenceService.createItem();
            item.setSyntacticalType("text/plain");

            try (OutputStream outputStream = item.getAsset().getOutputStream()) {
                IOUtils.copy(IOUtils.toInputStream(input.comment), outputStream);
                outputStream.close();
            } catch (IOException e) {
                log.error("Could not persist text data for ContentItem {}: {}", item.getURI(), e.getMessage());
                throw e;
            }

            eventManager.injectItem(item);

            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id",item.getURI().stringValue(),"status","submitted"))
                    .link(java.net.URI.create(item.getURI().stringValue()), "contentItem")
                    .build();
        } catch (RepositoryException | IOException e) {
            log.error("Could not create ContentItem");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("{id:.+}")
    public Response getResult(@PathParam("id") final String itemURI) throws IOException {

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
        final ItemData itemData = brokerSvc.getItemData(itemURI);
        if (itemData != null && !itemData.hasFinished()) {
            return Response.status(Response.Status.ACCEPTED)
                    .entity(ImmutableMap.of("id",itemURI,"status","inProgress"))
                    .build();
        }

        final TextAnalysisOutput out;
        try {
            out = getTextResult(item);
            item.getObjectConnection().close();

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
            "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n" +
            "SELECT DISTINCT ?uri ?label ?confidence WHERE {\n" +
            "  <%s>\n" +
            "  mmm:hasPart/oa:hasBody ?body.\n" +
            "  ?body a fam:LinkedEntity; fam:entity-label ?label; fam:entity-reference ?uri; fam:confidence ?confidence.\n" +
            "}";

    private static String queryTopics = "PREFIX fam: <http://vocab.fusepool.info/fam#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n" +
            "SELECT DISTINCT ?uri ?label ?confidence WHERE {\n" +
            "  <%s>\n" +
            "  mmm:hasPart/oa:hasBody ?body.\n" +
            "  ?body a fam:TopicAnnotation; fam:topic-label ?label; fam:topic-reference ?uri; fam:confidence ?confidence.\n" +
            "}";

    private static String querySentiment = "PREFIX fam: <http://vocab.fusepool.info/fam#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX mmm: <http://www.mico-project.eu/ns/mmm/2.0/schema#>\n" +
            "SELECT ?sentiment WHERE {\n" +
            "  <%s>\n" +
            "  mmm:hasPart/oa:hasBody ?body.\n" +
            "  ?body a fam:SentimentAnnotation; fam:sentiment ?sentiment.\n" +
            "}";

    // TODO: refactor, because model changed: Metadata object does not exist anymore. Dont use sparql queries, use anno4j
    private TextAnalysisOutput getTextResult(Item item) throws Exception {
        try {
            TextAnalysisOutput out = new TextAnalysisOutput(item);

            out.sentiment = querySentiment(item);
            out.entities = queryList(item, queryEntities);
            out.topics = queryList(item, queryTopics);

            return out;
        } catch (MalformedQueryException | QueryEvaluationException e) {
            log.error("Error querying objects; {}", e);
            throw new Exception(e);
        }
    }

    private List queryList(Item ci, String query) throws QueryEvaluationException, MalformedQueryException, RepositoryException {

        query = String.format(query, ci.getURI().stringValue());

        List res = new ArrayList<>();
        final TupleQueryResult result = ci.getObjectConnection().prepareTupleQuery(query).evaluate();

        try {
            while (result.hasNext()) {
                HashMap map = new HashMap<>();
                BindingSet bindings = result.next();
                for(String name : result.getBindingNames()) {
                    Value v = bindings.getBinding(name).getValue();
                    if(v instanceof Literal) {
                        Literal l = (Literal) v;
                        try {
                            map.put(name,l.doubleValue());//workaround !
                        } catch (IllegalArgumentException e) {
                            map.put(name,l.stringValue());
                        }
                    } else {
                        map.put(name, v.stringValue());
                    }
                }
                res.add(map);
            }
        } finally {
            result.close();
        }
        return res;
    }

    private Object querySentiment(Item item) throws QueryEvaluationException, MalformedQueryException, RepositoryException {

        String query = String.format(querySentiment, item.getURI().stringValue());

        final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

        try {
            if (result.hasNext()) {
                return ((Literal)result.next().getBinding(result.getBindingNames().get(0)).getValue()).doubleValue();
            }
        } finally {
            result.close();
        }

        return null;
    }

}
