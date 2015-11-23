package eu.mico.platform.broker.webservices;

import com.google.common.base.Preconditions;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
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
    public File download(String item, String part, String suffix) throws IOException, URISyntaxException, RepositoryException {

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
