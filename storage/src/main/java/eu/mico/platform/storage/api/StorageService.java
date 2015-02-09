package eu.mico.platform.storage.api;

import eu.mico.platform.storage.model.Content;
import eu.mico.platform.storage.model.ContentItem;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Storage Service
 *
 * @author Sergio Fernández
 * @author Horst Stadler
 */
public interface StorageService {

    Collection<ContentItem> list();

    OutputStream getOutputStream(Content part) throws IOException;

    InputStream getInputStream(Content part) throws IOException;

}
