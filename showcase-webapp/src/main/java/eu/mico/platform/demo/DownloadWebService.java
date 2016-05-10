package eu.mico.platform.demo;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 23.11.15.
 */
@Path("/resource")
public class DownloadWebService {

    private static Logger logger = LoggerFactory.getLogger(DownloadWebService.class);

    private final EventManager eventManager;

    public DownloadWebService(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @GET
    @Path("{item}/{part}.png")
    @Produces("image/png")
    public File downloadPng(@PathParam("item")String item, @PathParam("part")String part) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(new URIImpl(item),new URIImpl(part),".png");
    }
    @GET
    @Path("{item}/{part}.jpg")
    @Produces("image/jpeg")
    public File downloadJpg(@PathParam("item")String item, @PathParam("part")String part) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(new URIImpl(item),new URIImpl(part),".jpg");
    }
    @GET
    @Path("{item}/{part}.mp4")
    @Produces("video/mp4")
    public File downloadMp4(@PathParam("item")String item, @PathParam("part")String part) throws IOException, URISyntaxException, RepositoryException {
        return createResponse(new URIImpl(item),new URIImpl(part),".mp4");
    }

    private File createResponse(URI item, URI part, String suffix) throws IOException, URISyntaxException, RepositoryException {
        //read file
        File file = File.createTempFile(item.toString() + part.toString(),suffix);
        file.deleteOnExit();

        PersistenceService ps = eventManager.getPersistenceService();

        final Item item_o = ps.getItem(item);

        if(item_o == null) {
            throw new NotFoundException("Part Item with URI " + item_o.toString() + " not found in system");
        }
        final Part part_o = item_o.getPart(part);

        if(part_o == null) {
            throw new NotFoundException("Part Part with URI " + part_o.toString() + " not found in system");
        }

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        IOUtils.copy(part_o.getAsset().getInputStream(),fileOutputStream);

        return file;
    }
}