package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.model.Content;
import eu.mico.platform.storage.model.ContentItem;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * FTP-based storage implementation
 *
 * @author Sergio Fernández
 */
public class StorageServiceFTP implements StorageService {

    //TODO: port the FTP implementation from the Java API

    private final String host, user, pass;

    public StorageServiceFTP(String host, String user, String pass) {
        this.host = host;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public Collection<ContentItem> list() {
        return null; //TODO
    }

    @Override
    public OutputStream getOutputStream(Content part) {
        return null;
    }

    @Override
    public InputStream getInputStream(Content part) {
        return null;
    }

}
