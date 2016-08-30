package eu.mico.platform.demo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import eu.mico.platform.zooniverse.util.BrokerException;
import eu.mico.platform.zooniverse.util.BrokerServices;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.zooniverse.util.ItemData;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    private static final MimetypesFileTypeMap mimetypesMap = new MimetypesFileTypeMap();

    private final EventManager eventManager;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;
    private final BrokerServices brokerSvc;
    private final CloseableHttpClient httpClient;

    public DemoWebService(EventManager eventManager, String marmottaBaseUri, BrokerServices brokerSvc) {
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
        this.brokerSvc = brokerSvc;
        this.httpClient = HttpClientBuilder.create()
                .setUserAgent("MicoPlatform (ZooniverseWebService)")
                .build();

        mimetypesMap.addMimeTypes("image/jpeg jpeg jpg");
        mimetypesMap.addMimeTypes("image/png png");
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

            // Retrieve headers, read the Part-Disposition header to obtain the original name of the file
            MultivaluedMap<String, String> headers = inputPart.getHeaders();
            MediaType mediaType = inputPart.getMediaType();

            Preconditions.checkArgument(mediaType.isCompatible(MediaType.valueOf("image/*")), "Only type image/* is supported");

            // Handle the body of that part with an InputStream
            InputStream istream = inputPart.getBody(InputStream.class, null);

            String filename = getFileName(headers, mediaType);

            return createImageResult(mediaType.toString(), istream, filename);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e).build();
        }
    }

    private Response createImageResult(String mediaType, InputStream istream, String filename) throws Exception {
        try {
            final Item ci = injectContentItem(mediaType, istream, filename);

            //test if it is still in progress
            long start = System.currentTimeMillis();
            eu.mico.platform.zooniverse.util.Item itemStatus;
            do {
                Thread.sleep(timestep);

                if(System.currentTimeMillis() > start+timeout) {
                    return Response.status(408).entity("Image took to long to compute").build();
                }

                itemStatus = brokerSvc.getItem(ci.getURI().stringValue());
            } while(itemStatus == null || !itemStatus.hasFinished());

            Object result = createImageResult(ci);

            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id", ci.getURI(), "uri", ci.getURI().stringValue(), "result", result, "status", "done"))
                    .build();
        } catch (RepositoryException | IOException | BrokerException e) {
            log.error("Could not create Item");
            throw new Exception(e);
        } catch (InterruptedException e) {
            log.error("Some wired threat interrupt");
            Thread.currentThread().interrupt();
            throw new Exception(e);
        }
    }

    @POST
    @Path("/imageurl")
    @Produces("application/json")
    public Response uploadImageFromURL(@QueryParam("url") final java.net.URI imageUrl) {
        try {
            if (!(imageUrl.getScheme().equalsIgnoreCase("http") || imageUrl.getScheme().equalsIgnoreCase("https")) ||
                    isPrivateHost(imageUrl.getHost()) ||
                    !(imageUrl.getPort() == -1 || imageUrl.getPort() == 80 || imageUrl.getPort() == 443))
            {
                throw new ClientProtocolException(String.format("Invalid URL scheme (%s), host (%s) or port (%s)", imageUrl.getScheme(), imageUrl.getHost(), imageUrl.getPort()));
            }

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
                            type = MediaType.valueOf(mimetypesMap.getContentType(imageUrl.getPath()));
                        }

                        Header size = httpResponse.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
                        if (size == null || Integer.parseInt(size.getValue()) > 10 * 1024 * 1024) {
                            throw new ClientProtocolException("Part-length not set or maximum image size exceeded.");
                        }

                        if (type.toString().equalsIgnoreCase("image/jpeg") || mimetypesMap.toString().equalsIgnoreCase("image/png")) {
                            try (InputStream is = httpResponse.getEntity().getContent()) {
                                return createImageResult(type.toString(), is, imageUrl.getPath());
                            } catch (Exception e) {
                                throw new ClientProtocolException(e.getMessage());
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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("Could not fetch image to create content item");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();

        }
    }

    private boolean isPrivateHost(String host) throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(host);
        return addr.isMulticastAddress() ||
                addr.isAnyLocalAddress() ||
                addr.isLoopbackAddress() ||
                addr.isLinkLocalAddress() ||
                addr.isSiteLocalAddress();
    }

    private Item injectContentItem(String mediaType, InputStream istream, String filename) throws RepositoryException, IOException {
        final Item item = persistenceService.createItem();

        final Part part = item.createPart(ExtractorURI);
        part.setSyntacticalType(mediaType);
        part.getAsset().setFormat(mediaType);
        try (OutputStream outputStream = part.getAsset().getOutputStream()) {
            IOUtils.copy(istream, outputStream);
        } catch (IOException e) {
            log.error("Could not persist text data for ContentPart {}: {}", part.getURI(), e.getMessage());
            throw e;
        }

        eventManager.injectItem(item);
        return item;
    }

    // Parse Part-Disposition header to get the original file name
    private String getFileName(MultivaluedMap<String, String> headers, MediaType type) {

        String[] contentDispositionHeader = headers.getFirst("Part-Disposition").split(";");
        for (String name : contentDispositionHeader) {

            if ((name.trim().startsWith("filename"))) {

                String[] tmp = name.split("=");

                return tmp[1].trim().replaceAll("\"","");
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

            // Retrieve headers, read the Part-Disposition header to obtain the original name of the file
            MultivaluedMap<String, String> headers = inputPart.getHeaders();
            MediaType mediaType = inputPart.getMediaType();

            Preconditions.checkArgument(mediaType.isCompatible(MediaType.valueOf("video/mp4")), "Only type video/mp4 is supported");

            // Handle the body of that part with an InputStream
            InputStream istream = inputPart.getBody(InputStream.class,null);

            String filename = getFileName(headers, mediaType);

            try {
                final Item ci = injectContentItem(mediaType.toString(), istream, filename);

                //start Thread
                log.info("Send email: {}", email != null);
                if(email != null) {
                    log.info("Start email thread for {}", email);
                    EmailThread emailThread = new EmailThread(email,filename, ci);
                    emailThread.start();
                }

                return Response.status(Response.Status.CREATED)
                        .entity(ImmutableMap.of("id", ci.getURI(), "uri", ci.getURI().stringValue(), "status", "injected", "email", email != null))
                        .build();
            } catch (RepositoryException | IOException e) {
                log.error("Could not create Item");
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

        final Item item;
        try {
            item = persistenceService.getItem(contentItemUri);
            if (item == null)
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find Item '%s'", contentItemUri.stringValue())).build();
        } catch (RepositoryException e) {
            log.error("Error getting content item {}: {}", contentItemUri, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        try {
            //test if it is still in progress
            eu.mico.platform.zooniverse.util.Item itemStatus = brokerSvc.getItem(contentItemUri.stringValue());
            if (itemStatus != null && !itemStatus.hasFinished()) {
                return Response.status(Response.Status.ACCEPTED)
                        .entity(ImmutableMap.of("uri", uriString, "status", "inProgress"))
                        .build();
            }
        } catch (IOException | BrokerException e) {
            log.error("Error getting status of item {} from broker: {}", contentItemUri.stringValue(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        try {
            Object result = createImageResult(item);
            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id", item.getURI(), "uri", item.getURI().stringValue(), "result", result, "status", "done"))
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

        final Item item;
        try {
            item = persistenceService.getItem(contentItemUri);
            if (item == null)
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Could not find Item '%s'", contentItemUri.stringValue())).build();
        } catch (RepositoryException e) {
            log.error("Error getting content item {}: {}", contentItemUri, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        try {
            //test if it is still in progress
            eu.mico.platform.zooniverse.util.Item itemStatus = brokerSvc.getItem(contentItemUri.stringValue());
            if (itemStatus != null && !itemStatus.hasFinished()) {
                return Response.status(Response.Status.ACCEPTED)
                        .entity(ImmutableMap.of("uri", uriString, "status", "inProgress"))
                        .build();
            }
        } catch (IOException | BrokerException e) {
            log.error("Error getting status of item {} from broker: {}", contentItemUri.stringValue(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        try {
            Object result = createVideoResult(item);
            return Response.status(Response.Status.CREATED)
                    .entity(ImmutableMap.of("id", item.getURI(), "uri", item.getURI().stringValue(), "result", result, "status", "done"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

    }

    // TODO: refactor to model v2 impl
    private Object createImageResult(Item item) throws Exception {

//        final Metadata metadata;
        Map<String,Object> result = new HashMap<>();
        try {

            try {
                result.put("type", "image");
                result.put("part_uri", queryString(queryMediaPartURI, item));
                result.put("faces", queryList(queryFaces, item));
                result.put("animals", queryMapList(animals, item));

            } catch (MalformedQueryException | QueryEvaluationException e) {
                log.error("Error querying objects; {}", e);
                throw new Exception(e);
            }

        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Object createVideoResult(Item item) throws Exception {

        Map<String,Object> result = new HashMap<>();
        try {

            try {
                result.put("type", "video");
                result.put("part_uri", queryString(queryMediaPartURI, item));
                result.put("faces", queryMapList(timedFaces, item));
                result.put("shotImages", queryMapList(shotImages, item));
                //result.put("shots", queryMapList(shots, item));
                result.put("timedText", queryMapList(timedText, item));
            } catch (MalformedQueryException | QueryEvaluationException e) {
                log.error("Error querying objects; {}", e);
                throw new Exception(e);
            }

        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return result;
    }

    /***************
     * TODO adapt queries to new model
     ****************/
    private static String queryMediaPartURI = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "SELECT ?p WHERE {<%s> mico:hasContentPart ?p. ?p dct:creator \""+INJECTOR+"\" }";

    private static String animals = "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "SELECT ?animal ?region ?confidence ?version WHERE {\n" +
            "<%s> mico:hasContentPart ?cp .\n" +
            "?cp mico:hasContent ?annot .\n" +
            "?annot oa:hasBody ?body .\n" +
            "?annot oa:hasTarget ?tgt .\n" +
            "?tgt  oa:hasSelector ?fs .\n" +
            "?body rdf:value ?animal .\n" +
            "?body mico:hasConfidence ?confidence .\n" +
            "?body mico:hasExtractionVersion ?version .\n" +
            "?fs rdf:value ?region\n" +
            "FILTER EXISTS {?body rdf:type mico:AnimalDetectionBody}\n" +
            "}";

    private static String timedText = "PREFIX mm: <http://linkedmultimedia.org/sparql-mm/ns/1.0.0/function#>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX fam: <http://vocab.fusepool.info/fam#>\n" +
            "\n" +
            "SELECT (group_concat(?stt_value;separator=\" \") AS ?text) ((?group)*3 AS ?start) ((?group+1)*3 AS ?end) WHERE {\n" +
            "  <%s>\n" +
            "  mico:hasPart/mico:hasPart ?stt_annot .\n" +
            "  ?stt_annot oa:hasBody ?stt_body .\n" +
            "  ?stt_body rdf:type  ?stt_body_type .\n" +
            "  ?stt_body rdf:value ?stt_value .\n" +
            "  ?stt_annot oa:hasTarget ?tgt .\n" +
            "  ?tgt  oa:hasSelector ?fs .\n" +
            "  ?fs rdf:value ?timestamp .\n" +
            "  BIND (floor((mm:getStart(?timestamp) / 3)) AS ?group)\n" +
            "  FILTER (?stt_body_type = mico:STTBody)\n" +
            "  FILTER (!regex(?stt_value,\"^<eps>$\"))\n" +
            "  FILTER (!regex(?stt_value,\"^\\\\[noise\\\\]\"))\n" +
            "}\n" +
            "GROUP BY ?group\n" +
            "ORDER BY mm:getStart(?timestamp)";

    private static String queryFaces = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
            "PREFIX mico: <http://www.mico-project.eu/ns/platform/1.0/schema#>\n" +
            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "SELECT ?faces WHERE {\n" +
            "   <%s> mico:hasPart ?c." +
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
            "  ?tgt  oa:hasSelector ?fs .\n" +
            "  ?fs rdf:value ?timestamp .\n" +
            "  ?cp dct:type ?type\n" +
            "  BIND (uri(concat(replace(str(?cp),'marmotta','resource'),'.png')) AS ?location)" +
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

    private String queryString(String query, Item item) throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        query = String.format(query, item.getURI().stringValue());

        final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

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

    private List queryMapList(String query, Item item) throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        query = String.format(query, item.getURI().stringValue());

        List res = new ArrayList<>();
        final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

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

    private List queryList(String query, Item item) throws QueryEvaluationException, MalformedQueryException, RepositoryException {
        query = String.format(query, item.getURI().stringValue());

        List res = new ArrayList<>();
        final TupleQueryResult result = item.getObjectConnection().prepareTupleQuery(query).evaluate();

        try {
            while (result.hasNext()) {
                BindingSet bindings = result.next();
                Value v = bindings.getBinding(result.getBindingNames().get(0)).getValue();
                res.add(v.stringValue());
            }
        } finally {
            result.close();
        }
        return res;
    }
}

