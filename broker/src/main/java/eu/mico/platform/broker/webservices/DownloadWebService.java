package eu.mico.platform.broker.webservices;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    private final EventManager eventManager;
    private final String marmottaBaseUri;

    public DownloadWebService(EventManager eventManager, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
    }

    @GET
    @Path("{item}/{part}.{suffix}")
    public File download(@PathParam("item")String item, @PathParam("part")String part, @PathParam("suffix")String suffix) throws IOException, URISyntaxException, RepositoryException {

        //read file
        File file = File.createTempFile(item+part,suffix);
        file.deleteOnExit();

        PersistenceService ps = eventManager.getPersistenceService();

        final ContentItem item_o = ps.getContentItem(getURI(item,marmottaBaseUri));
        if(item == null) {
            throw new NotFoundException("Content Item with URI " + item + " not found in system");
        }
        final Content part_o = item_o.getContentPart(getURI(part,marmottaBaseUri));
        if(part == null) {
            throw new NotFoundException("Content Part with URI " + part + " not found in system");
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