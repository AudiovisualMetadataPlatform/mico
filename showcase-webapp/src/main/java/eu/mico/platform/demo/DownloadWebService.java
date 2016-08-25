package eu.mico.platform.demo;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 23.11.15.
 */
@Path("/resource")
public class DownloadWebService {

    private static Logger logger = LoggerFactory.getLogger(DownloadWebService.class);

    private final EventManager eventManager;
    private final String marmottaBaseUri;
    private final java.nio.file.Path fsMediaPath;
    private final String mediaURL;

    public DownloadWebService(EventManager eventManager, String marmottaBaseUri, String mediaDirectory, String mediaURL) throws IOException{
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
        fsMediaPath = Paths.get(mediaDirectory);
        if (!Files.exists(fsMediaPath)) {
            Files.createDirectory(fsMediaPath);
        }
        this.mediaURL = mediaURL;
    }

    @GET
    @Path("{item}.png")
    @Produces("image/png")
    public Response downloadPng(@PathParam("item")String item) throws IOException, URISyntaxException, RepositoryException {
        return createResponse( item, ".png");
    }

    @GET
    @Path("{item}.jpg")
    @Produces("image/jpeg")
    public Response downloadJpg(@PathParam("item")String item) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(item, ".jpg");
    }

    @GET
    @Path("{item}.mp4")
    @Produces("video/mp4")
    public Response downloadMp4(@PathParam("item")String item) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(item, ".mp4");
    }

    private Response createResponse(String item, String suffix) throws URISyntaxException,IOException, RepositoryException {
        PersistenceService ps = eventManager.getPersistenceService();
        final Item item_o = ps.getItem(new URIImpl(marmottaBaseUri + "/" + item));
        if(item_o == null || !item_o.hasAsset()) {
            throw new NotFoundException("Part Item with URI " + item_o.toString() + " not found in system");
        }

        String filename = item + suffix;
        File file = new File(fsMediaPath.resolve(filename).toString());
        if (!file.exists()) {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(item_o.getAsset().getInputStream(),fileOutputStream);
            fileOutputStream.close();
            file.deleteOnExit();
        }
        return Response.seeOther(new java.net.URI(mediaURL + "/" + filename)).build();
    }
}
