package eu.mico.platform.broker.webservices;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collections;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 23.11.15.
 */
@Path("/resource")
public class DownloadWebService {

    private static Logger logger = LoggerFactory.getLogger(DownloadWebService.class);

    private final EventManager eventManager;
    private final String marmottaBaseUri;

    public DownloadWebService(EventManager eventManager, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
    }

    @GET
    @Path("{item}/{part}.png")
    @Produces("image/png")
    public File downloadPng(@PathParam("item")String item, @PathParam("part")String part) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(item,part,"png");
    }
    @GET
    @Path("{item}/{part}.jpg")
    @Produces("image/jpeg")
    public File downloadJpg(@PathParam("item")String item, @PathParam("part")String part) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(item,part,"jpg");
    }
    @GET
    @Path("{item}/{part}.mp4")
    @Produces("video/mp4")
    public File downloadMp4(@PathParam("item")String item, @PathParam("part")String part) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(item,part,"mp4");
    }

    private File createResponse(String item, String part, String suffix) throws IOException, URISyntaxException, RepositoryException {
        //read file
        File file = File.createTempFile(item+part,suffix);
        file.deleteOnExit();

        PersistenceService ps = eventManager.getPersistenceService();

        final ContentItem item_o = ps.getContentItem(getURI(marmottaBaseUri,item));
        if(item_o == null) {
            throw new NotFoundException("Content Item with URI " + getURI(marmottaBaseUri,item) + " not found in system");
        }
        final Content part_o = item_o.getContentPart(getURI(marmottaBaseUri,item+"/"+part));

        if(part_o == null) {
            throw new NotFoundException("Content Part with URI " + getURI(marmottaBaseUri,item+"/"+part) + " not found in system");
        }

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        IOUtils.copy(part_o.getInputStream(),fileOutputStream);

        return file;
    }

    private URI getURI(String baseURL, String extraPath) throws URISyntaxException {
        java.net.URI baseURI = new java.net.URI(baseURL).normalize();
        String newPath = baseURI.getPath() + "/" + extraPath;
        java.net.URI newURI = baseURI.resolve(newPath);
        return new URIImpl(newURI.normalize().toString());
    }

}