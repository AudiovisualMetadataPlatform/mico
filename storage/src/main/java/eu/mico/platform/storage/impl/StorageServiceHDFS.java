package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.model.ContentItem;

import java.util.Collection;

/**
 * HDFS-based storage implementation
 */
public class StorageServiceHDFS implements StorageService {

    private final String host, user, pass;

    public StorageServiceHDFS(String host, String user, String pass) {
        this.host = host;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public Collection<ContentItem> list() {
        return null; //TODO
    }

}
