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

package eu.mico.platform.broker.webservices;

import eu.mico.platform.broker.api.ItemState;
import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.model.v2.ServiceDescriptor;
import eu.mico.platform.broker.model.v2.Transition;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

import org.apache.commons.io.IOUtils;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiAuthNone;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiVersion;
import org.jsondoc.core.pojo.ApiVerb;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Api(name = "item status services", description = "Methods for checking status of items", group = "broker")
@ApiVersion(since = "1.0")
@ApiAuthNone
@Path("/status")
public class StatusWebService {

    private static Logger log = LoggerFactory.getLogger(StatusWebService.class);

    public static final SimpleDateFormat ISO8601FORMAT = createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTC");

    @Context
    private ServletContext servletContext;
    
    private MICOBroker broker;

    public StatusWebService(MICOBroker broker) {
        this.broker = broker;
    }

    @ApiMethod(
            path = "/status/info",
            verb = ApiVerb.GET,
            description = "retrieve general information about the service as plain text",
            produces = { MediaType.TEXT_PLAIN},
            responsestatuscode = "200 - OK"
    )
    @GET
    @Path("/info")
    @Produces("text/plain")
    public Response getInfoText(){
        String info = null;
        try{
            InputStream resourceAsStream = servletContext
                    .getResourceAsStream("/META-INF/MANIFEST.MF");
            Manifest mf = new Manifest();
            mf.read(resourceAsStream);
            Attributes atts = mf.getMainAttributes();
            info = atts.getValue("Implementation-Title") + " ("+ atts.getValue("Implementation-Version")+")"
                    + "\nGit-Revision: " + atts.getValue("Git-Revision")
                    + (atts.getValue("Git-Branch") != "UNKNOWN" ? 
                            "\nGit-Branch: " + atts.getValue("Git-Branch")
                            : "\nGit-Tag: "  + atts.getValue("Git-Tag"))
                    + "\nbuild on: " + atts.getValue("Build-Time");
        }catch(IOException e){
            info = "<unknown>";
        }
        return Response.ok(info).build();
    }

    @ApiMethod(
            path = "/status/info",
            verb = ApiVerb.GET,
            description = "retrieve general information about the service in machine readable format from META-INF/MANIFEST.MF",
            produces = { MediaType.APPLICATION_JSON },
            responsestatuscode = "200 - OK"
    )
    @GET
    @Path("/info")
    @Produces("application/json")
    public Response getInfoJson(){
        try{
            InputStream resourceAsStream = servletContext
                    .getResourceAsStream("/META-INF/MANIFEST.MF");
            Manifest mf = new Manifest();
            mf.read(resourceAsStream);
            Attributes atts = mf.getMainAttributes();
            return Response.ok(atts).build();
        }catch(IOException e ){
            return Response.noContent().build();
        }
    }

    @ApiMethod(
            path = "/status/dependencies",
            verb = ApiVerb.GET,
            description = "generate a connection diagram for all running extraction services",
            produces= {"image/png"},
            responsestatuscode = "200 - OK"
    )
    @GET
    @Path("/dependencies")
    @Produces("image/png")
    public Response getDependencyGraph(@QueryParam("width") final int width, @QueryParam("height") final int height) {
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                SwingImageCreator.createGraph(broker.getDependencies(), new Dimension(width != 0 ? width : 640, height != 0 ? height : 480), "png", output);
            }
        };
        return Response.ok(output).build();
    }


    @ApiMethod(
            path = "/status/services",
            verb = ApiVerb.GET,
            description = "retrieve a list of currently connected services",
            produces = { MediaType.APPLICATION_JSON },
            responsestatuscode = "200 - OK"
    )
    @GET
    @Path("/services")
    @Produces("application/json")
    public List<Map<String,String>> getServices()  {
        List<Map<String,String>> result = new ArrayList<>();
        for(ServiceDescriptor svc : broker.getDependencies().edgeSet()) {
            Map<String,String> sprops = new HashMap<>();
            sprops.put("name", svc.toString());
            sprops.put("uri", svc.getUri().stringValue());
            sprops.put("provides", svc.getProvides());
            sprops.put("requires", svc.getRequires());
            sprops.put("language", svc.getLanguage());
            sprops.put("time", ISO8601FORMAT.format(svc.getRegistrationTime()));
            sprops.put("calls", ""+svc.getCalls());
            result.add(sprops);
        }
        return result;
    }


    @ApiMethod(
            path = "/status/items",
            verb = ApiVerb.GET,
            description = "retrieve a list of items known to the platform service",
            produces = { MediaType.APPLICATION_JSON },
            responsestatuscode = "200 - OK"
    )
    @GET
    @Path("/items")
    @Produces("application/json")
    public List<Map<String,Object>> getItems(@QueryParam("uri") String itemUri, @QueryParam("parts") boolean showParts, @QueryParam("offset") int offset, @QueryParam("number") int number) throws RepositoryException {
        List<Map<String,Object>> result = new ArrayList<>();
        if(itemUri == null) {
            // retrieve a list of all items
            for(Map.Entry<String, ItemState> state : broker.getStates().entrySet()) {
                result.add(wrapContentItemStatus(state.getKey(),state.getValue(),showParts));
            }
            for(Map.Entry<String, ItemState> state : broker.getItemStatesFromCamel().entrySet()) {
                result.add(wrapContentItemStatus(state.getKey(),state.getValue(),showParts));
            }
            Collections.sort(result, new Comparator<Map<String,Object>>() {
                @Override
                public int compare(Map<String,Object> ci1, Map<String,Object> ci2) {
                    return ((String)(ci2.get("time"))).compareTo((String)(ci1.get("time")));
                }

            });
        } else if(broker.getStates().containsKey(itemUri)) {
            result.add(wrapContentItemStatus(itemUri, broker.getStates().get(itemUri),showParts));
        } else if(broker.getItemStatesFromCamel().containsKey(itemUri)) {
            result.add(wrapContentItemStatus(itemUri, broker.getItemStatesFromCamel().get(itemUri),showParts));
        } else {
            throw new NotFoundException("item with uri " + itemUri + " not found in broker");
        }

        if (offset >= result.size())
            offset = result.size() - 1;
        if (offset >=0 && number > 0) {
            if (offset + number > result.size())
                number = result.size() - offset;
            return result.subList(offset, offset + number);
        }
        if (offset > 0)
            return result.subList(offset, result.size()-1);
        return result;
    }

    private Map<String,Object> wrapContentItemStatus(String uri, ItemState state, boolean showParts) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", uri);
        sprops.put("finished", state.isFinalState() ? "true" : "false");
        sprops.put("hasError", state.hasError() ? "true" : "false");
        sprops.put("error", state.getError());
        sprops.put("time", ISO8601FORMAT.format(state.getCreated()));

        if(showParts) {
            List<Map<String, Object>> parts = new ArrayList<>();
            Item item = broker.getPersistenceService().getItem(new URIImpl(uri));
            log.trace("collect {} collect parts of item: {}",uri);
            if (item != null) {
                sprops.put("semanticType"   , item.getSemanticType());
                sprops.put("syntacticalType", item.getSyntacticalType());
                sprops.put("serializedAt"   , item.getSerializedAt());
                sprops.put("hasAsset", item.hasAsset());
                if(item.hasAsset()){
                    Asset asset = item.getAsset();
                    sprops.put("assetFormat", asset.getFormat());
                    sprops.put("assetLocation", asset.getLocation());
                }
                for (Part part : item.getParts()) {
                    log.trace("    - part: {} - {} ({})",part.getURI(),part.getSerializedBy(), part.getSerializedAt());
                    parts.add(wrapContentStatus(state, part));
                }
            }
            sprops.put("parts", parts);
        }

        return sprops;
    }

    private Map<String,Object> wrapContentStatus(ItemState state, Part part) throws RepositoryException {
        Map<String,Object> sprops = new HashMap<>();
        sprops.put("uri", part.getURI().stringValue());
        sprops.put("title", part.getRDFObject().getResourceAsString());
        sprops.put("type",  part.getSyntacticalType());
        sprops.put("creator",  part.getSerializedBy().getResourceAsString());
        sprops.put("created",  part.getSerializedAt());
        Resource[] inputs = part.getInputs().toArray(new Resource[0]);
        if(inputs.length > 0){
            sprops.put("source", stringValue(inputs[0].getURI()));
        }
        sprops.put("hasAsset", part.hasAsset());
        if(part.hasAsset()){
            Asset asset = part.getAsset();
            sprops.put("assetFormat", asset.getFormat());
            sprops.put("assetLocation", asset.getLocation());
        }

        if(state != null) {
            if(state.getStates().get(part.getURI()) != null) {
                sprops.put("state", state.getStates().get(part.getURI()).getSymbol());
            }

            List<Map<String, String>> transitions = new ArrayList<>();
            for (Map.Entry<String, Transition> t : state.getProgress().entrySet()) {
                if(t.getValue().getObject().equals(part.getURI())) {
                    Map<String, String> tprops = new HashMap<>();
                    tprops.put("correlation", t.getKey());
                    tprops.put("start_state", t.getValue().getStateStart().getSymbol());
                    tprops.put("end_state", t.getValue().getStateEnd().getSymbol());
                    tprops.put("progress", Float.toString(t.getValue().getProgress()));
                    tprops.put("service", t.getValue().getService().getUri().stringValue());
                    transitions.add(tprops);
                }
            }
            sprops.put("transitions", transitions);
        }

        return sprops;
    }

    private static String stringValue(Value v) {
        return v != null ? v.stringValue() : null;
    }


    @ApiMethod(
            path = "/status/download",
            verb = ApiVerb.GET,
            description = "retrieve the binary asset of an item or a part ",
            produces = { "IMAGE/*","VIDEO/*",MediaType.WILDCARD, MediaType.APPLICATION_OCTET_STREAM },
            responsestatuscode = "200 - OK"
    )
    @GET
    @Path("/download")
    public Response downloadPart(@QueryParam("itemUri") String itemUri, @QueryParam("partUri") String partUri) throws RepositoryException {
        PersistenceService ps = broker.getPersistenceService();

        final Item item = ps.getItem(new URIImpl(itemUri));

        if (item == null) {
            throw new NotFoundException("Item with URI " + itemUri + " not found in system");
        }

        StreamingOutput entity;
        String type;
        if (partUri == null || itemUri.equals(partUri)) {
            if (!item.hasAsset()){
                throw new NotFoundException("Item with URI " + partUri + " has no asset");
            }
            type = item.getAsset().getFormat();

            log.debug("providing <{}> asset from item {}", type, item.getURI());
            entity = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        IOUtils.copy(item.getAsset().getInputStream(), output);
                    } catch (RepositoryException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };

        } else {
            final Part part = item.getPart(new URIImpl(partUri));
            if (part == null) {
                throw new NotFoundException("Part with URI " + partUri + " not found in system");
            }
            if (part.hasAsset() == false) {
                throw new NotFoundException("Part with URI " + partUri + " has no asset");
            }
            type = part.getAsset().getFormat();

            log.debug("providing <{}> asset from part {}", type, part.getURI());
            entity = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        IOUtils.copy(part.getAsset().getInputStream(), output);
                    } catch (RepositoryException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };

        }

        try{
            MediaType.valueOf(type);
        }catch(IllegalArgumentException e){
            log.info("deliver unknown mediaType [{}] - set type to {}", type,
                    MediaType.APPLICATION_OCTET_STREAM);
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return Response.ok(entity, type).build();
    }


    @GET
    public Response getStatus() {
        String mVersionString = "n.n";
        try {
            InputStream manifestStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestStream);
            Attributes attributes = manifest.getMainAttributes();
            String impVersion = attributes.getValue("Implementation-Version");
            mVersionString = impVersion;
        }
        catch(IOException ex) {
            log.warn("Error while reading version: " + ex.getMessage());
        }
        return Response.status(Response.Status.OK).entity("Version: " + mVersionString).build();
    }


    private static SimpleDateFormat createDateFormat(String format, String timezone) {
        SimpleDateFormat sdf =
                new SimpleDateFormat(format, DateFormatSymbols.getInstance(Locale.US));
        if (timezone != null) {
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return sdf;
    }
}
