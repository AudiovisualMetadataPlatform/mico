package eu.mico.platform.broker.webservices;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.core.header.FormDataContentDisposition;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.EmailThread;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.impl.MarmottaContentItem;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import eu.mico.platform.uc.zooniverse.model.TextAnalysisInput;
import eu.mico.platform.uc.zooniverse.model.TextAnalysisOutput;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 18.11.15.
 */
@Path("demo")
public class DemoWebService {

    public static final String INJECTOR = "http://www.mico-project.eu/broker/demo/inject/image/file";
    private static long timeout = 10000;
    private static long timestep = 500;

    private static final Logger log = LoggerFactory.getLogger(DemoWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", DateFormatSymbols.getInstance(Locale.US));

    static {
        ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final EventManager eventManager;
    private final MICOBroker broker;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;

    public DemoWebService(EventManager eventManager, MICOBroker broker, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.broker = broker;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
    }

    private static final URI ExtractorURI = new URIImpl("http://www.mico-project.eu/services/ner-text");

    @POST
    @Path("/image")
    @Produces("application/json")
    @Consumes("multipart/form-data")
    public Response uploadImage(MultipartFormDataInput input) {

        Map<String, List<InputPart>> formParts = input.getFormDataMap();

        try {
            Preconditions.checkArgument(formParts.containsKey("file"), "File has to be included");

            List<InputPart> inPart = formParts.get("file");

            Preconditions.checkArgument(inPart.size() == 1, "One and only one file has to be included");

            InputPart inputPart = inPart.get(0);

            // Retrieve headers, read the Content-Disposition header to obtain the original name of the file
            MultivaluedMap<String, String> headers = inputPart.getHeaders();
            MediaType mediaType = inputPart.getMediaType();

            Preconditions.checkArgument(mediaType.isCompatible(MediaType.valueOf("image/*")), "Only type image/* is supported");

            // Handle the body of that part with an InputStream
            InputStream istream = inputPart.getBody(InputStream.class,null);

            String filename = getFileName(headers, mediaType);

            try {
                final ContentItem ci = injectContentItem(mediaType, istream, filename);

                //test if it is still in progress
                long start = System.currentTimeMillis();

                ContentItemState state = broker.getStates().get(ci.getURI().stringValue());

                while (state == null || !state.isFinalState()) {

                    if(System.currentTimeMillis() > start+timeout) {
                        return Response.status(408).entity("Image took to long to compute").build();
                    }

                    Thread.sleep(timestep);

                    state = broker.getStates().get(ci.getURI().stringValue());
                }

                Object result = createImageResult(ci.getURI().stringValue());

                return Response.status(Response.Status.CREATED)
                        .entity(ImmutableMap.of("id", ci.getID(), "uri", ci.getURI().stringValue(), "result", result, "status", "done"))
                        .build();
            } catch (RepositoryException | IOException e) {
                log.error("Could not create ContentItem");
                throw new Exception(e);
            } catch (InterruptedException e) {
                log.error("Some wired threat interrupt");
                Thread.currentThread().interrupt();
                throw new Exception(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e).build();
        }
    }

    private ContentItem injectContentItem(MediaType mediaType, InputStream istream, String filename) throws RepositoryException, IOException {
        final ContentItem ci = persistenceService.createContentItem();

        final Content contentPart = ci.createContentPart();
        contentPart.setType(mediaType.toString());
        contentPart.setProperty(DCTERMS.TITLE, filename);
        contentPart.setRelation(DCTERMS.CREATOR, new URIImpl(INJECTOR));
        contentPart.setProperty(DCTERMS.CREATED, ISO8601FORMAT.format(new Date()));
        try (OutputStream outputStream = contentPart.getOutputStream()) {
            IOUtils.copy(istream, outputStream);
        } catch (IOException e) {
            log.error("Could not persist text data for ContentPart {}: {}", contentPart.getURI(), e.getMessage());
            throw e;
        }

        eventManager.injectContentItem(ci);
        return ci;
    }

    // Parse Content-Disposition header to get the original file name
    private String getFileName(MultivaluedMap<String, String> headers, MediaType type) {

        String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
        for (String name : contentDispositionHeader) {

            if ((name.trim().startsWith("filename"))) {

                String[] tmp = name.split("=");

                String fileName = tmp[1].trim().replaceAll("\"","");

                return fileName;
            }
        }
        return "randomName." + type.getSubtype();
    }

    @POST
    @Path("/video")
    @Produces("application/json")
    @Consumes("multipart/form-data")
    public Response uploadVideo(MultipartFormDataInput input) {

        String email = null;

        Map<String, List<InputPart>> formParts = input.getFormDataMap();

        try {

            if(formParts.containsKey("email")) {
                List<InputPart> _email = formParts.get("email");
                if(_email.size()==1) {
                    email = _email.get(0).getBodyAsString();
                    Preconditions.checkArgument(EmailValidator.getInstance().isValid(email),"Email is not valid");
                }
            }

            Preconditions.checkArgument(formParts.containsKey("file"), "File has to be included");

            List<InputPart> inPart = formParts.get("file");

            Preconditions.checkArgument(inPart.size() == 1, "One and only one file has to be included");

            InputPart inputPart = inPart.get(0);

            // Retrieve headers, read the Content-Disposition header to obtain the original name of the file
            MultivaluedMap<String, String> headers = inputPart.getHeaders();
            MediaType mediaType = inputPart.getMediaType();

            Preconditions.checkArgument(mediaType.isCompatible(MediaType.valueOf("video/mp4")), "Only type video/mp4 is supported");

            // Handle the body of that part with an InputStream
            InputStream istream = inputPart.getBody(InputStream.class,null);

            String filename = getFileName(headers, mediaType);

            try {
                final ContentItem ci = injectContentItem(mediaType, istream, filename);

                //start Thread
                if(email != null) {
                    EmailThread emailThread = new EmailThread(email,filename,broker,ci);
                    emailThread.run();
                }

                return Response.status(Response.Status.CREATED)
                        .entity(ImmutableMap.of("id", ci.getID(), "uri", ci.getURI().stringValue(), "status", "injected", "email", email != null))
                        .build();
            } catch (RepositoryException | IOException e) {
                log.error("Could not create ContentItem");
                throw new Exception(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e).build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/image")
    public Response getImageResult(@QueryParam("uri") final String uriString) {

        final URI contentItemUri = new URIImpl(uriString);

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
                    .entity(ImmutableMap.of("uri", uriString, "status", "inProgress"))
                    .build();
        }

        try {
            Object result = createImageResult(uriString);
            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id", contentItem.getID(), "uri", contentItem.getURI().stringValue(), "result", result, "status", "done"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

    }

    @GET
    @Produces("application/json")
    @Path("/video")
    public Response getVideoResult(@QueryParam("uri") final String uriString) {

        final URI contentItemUri = new URIImpl(uriString);

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
                    .entity(ImmutableMap.of("uri", uriString, "status", "inProgress"))
                    .build();
        }

        try {
            Object result = createVideoResult(uriString);
            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id", contentItem.getID(), "uri", contentItem.getURI().stringValue(), "result", result, "status", "done"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

    }

    private Object createImageResult(String uri) throws Exception {

        final Metadata metadata;
        Map<String,Object> result = new HashMap<>();
        try {
            metadata = persistenceService.getMetadata();

            try {
                result.put("type", "image");
                result.put("part_uri", queryString(queryMediaPartURI, metadata, uri));
                result.put("faces", queryList(queryFaces, metadata, uri));

            } catch (MalformedQueryException | QueryEvaluationException e) {
                log.error("Error querying objects; {}", e);
                throw new Exception(e);
            }

        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Object createVideoResult(String uri) throws Exception {

        final Metadata metadata;
        Map<String,Object> result = new HashMap<>();
        try {
            metadata = persistenceService.getMetadata();

            try {
                result.put("type", "video");
                result.put("part_uri", queryString(queryMediaPartURI, metadata, uri));
                result.put("faces", queryMapList(timedFaces, metadata, uri));
                result.put("shotImages", queryMapList(shotImages, metadata, uri));
                result.put("shots", queryMapList(shots, metadata, uri));
            } catch (MalformedQueryException | QueryEvaluationException e) {
                log.error("Error querying objects; {}", e);
                throw new Exception(e);
            }

        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String queryMediaPartURI = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "SELECT ?p WHERE {<%s> mico:hasContentPart ?p. ?p dct:creator \""+INJECTOR+"\" }";

    private static String queryFaces = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "SELECT ?faces WHERE {\n" +
            "   <%s> mico:hasContentPart/mico:hasContent ?c." +
            "   ?c oa:hasTarget/oa:hasSelector/rdf:value ?faces." +
            "   ?c oa:hasBody/rdf:type <http://www.mico-project.eu/ns/platform/1.0/schema#FaceDetectionBody>\n" +
            "}";

    private static String shotImages = "PREFIX mm: <http://linkedmultimedia.org/sparql-mm/ns/1.0.0/function#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "SELECT ?type ?timestamp ?location WHERE {\n" +
            "  <%s> mico:hasContentPart ?cp .\n" +
            "  ?cp mico:hasContent ?annot .\n" +
            "  ?annot oa:hasBody ?body .\n" +
            "  ?annot oa:hasTarget ?tgt .\n" +
            "  ?tgt  mico:hasLocation ?location .\n" +
            "  ?tgt  oa:hasSelector ?fs .\n" +
            "  ?fs rdf:value ?timestamp .\n" +
            "  ?cp dct:type ?type\n" +
            "  FILTER EXISTS {?body rdf:type mico:TVSShotBoundaryFrameBody .\n" +
            "                 ?cp dct:type \"image/png\"}\n" +
            "} ORDER BY mm:getStart(?timestamp)";

    private static String shots = "PREFIX mm: <http://linkedmultimedia.org/sparql-mm/ns/1.0.0/function#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "SELECT ?time WHERE {\n" +
            "  <%s>  mico:hasContentPart ?cp .\n" +
            "  ?cp mico:hasContent ?annot .\n" +
            "  ?annot oa:hasBody ?body .\n" +
            "  ?annot oa:hasTarget ?tgt .\n" +
            "  ?tgt  oa:hasSelector ?fs .\n" +
            "  ?fs rdf:value ?time\n" +
            "  FILTER EXISTS {?body rdf:type mico:TVSShotBody}\n" +
            "} ORDER BY mm:getStart(?time)";

    private static String timedFaces = "PREFIX mm: <http://linkedmultimedia.org/sparql-mm/ns/1.0.0/function#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "SELECT DISTINCT ?timestamp ?region WHERE {\n" +
            "  <%s> mico:hasContentPart ?cp .\n" +
            "  ?cp mico:hasContent ?annot .\n" +
            "  ?annot oa:hasBody ?body .\n" +
            "  ?annot oa:hasTarget ?tgt .\n" +
            "  ?tgt  oa:hasSelector ?fs .\n" +
            "  ?fs rdf:value ?region .\n" +
            "  ?tgt oa:hasSource ?pcp .\n" +
            "  ?pcp dct:source ?ppcp .\n" +
            "  ?ppcp mico:hasContent ?ppannot .\n" +
            "  ?ppannot oa:hasBody ?ppbody .\n" +
            "  ?ppbody rdf:type ?frameType . \n" +
            "  ?ppannot oa:hasTarget ?pptgt .\n" +
            "  ?pptgt  oa:hasSelector ?ppfs .\n" +
            "  ?ppfs rdf:value ?timestamp\n" +
            "  FILTER EXISTS {?body rdf:type mico:FaceDetectionBody .\n" +
            "                ?pcp dct:type \"text/vnd.fhg-ccv-facedetection+xml\"}\n" +
            "} ORDER BY mm:getStart(?timestamp)";

    private String queryString(String query, Metadata metadata, String uri) throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        query = String.format(query, uri);

        final TupleQueryResult result = metadata.query(query);

        try {
            if (result.hasNext()) {
                BindingSet bindings = result.next();
                return bindings.getBinding(result.getBindingNames().get(0)).getValue().stringValue();
            }
        } finally {
            result.close();
        }
        return null;
    }

    private List queryMapList(String query, Metadata metadata, String uri) throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        query = String.format(query, uri);

        List res = new ArrayList<>();
        final TupleQueryResult result = metadata.query(query);

        try {
            while (result.hasNext()) {
                HashMap map = new HashMap<>();
                BindingSet bindings = result.next();
                for (String name : result.getBindingNames()) {
                    Value v = bindings.getBinding(name).getValue();
                    map.put(name, v.stringValue());
                }
                res.add(map);
            }
        } finally {
            result.close();
        }
        return res;
    }

    private List queryList(String query, Metadata metadata, String uri) throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        query = String.format(query, uri);

        List res = new ArrayList<>();
        final TupleQueryResult result = metadata.query(query);

        try {
            while (result.hasNext()) {
                HashMap map = new HashMap<>();
                BindingSet bindings = result.next();
                Value v = bindings.getBinding(result.getBindingNames().get(0)).getValue();
                res.add(v);
            }
        } finally {
            result.close();
        }
        return res;
    }
}

