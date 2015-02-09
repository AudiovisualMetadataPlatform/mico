package eu.mico.platform.storage.api;

import eu.mico.platform.storage.model.Content;
import eu.mico.platform.storage.model.ContentItem;

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

    OutputStream getOutputStream(Content part);

    InputStream getInputStream(Content part);

}
