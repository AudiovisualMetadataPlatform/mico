package eu.mico.platform.storage.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

/**
 * Storage Service
 *
 * @author Sergio Fernández
 * @author Horst Stadler
 */
public interface StorageService {

    OutputStream getOutputStream(URI contentPath) throws IOException;

    InputStream getInputStream(URI contentPath) throws IOException;

    boolean delete(URI contentPath) throws IOException;

}
