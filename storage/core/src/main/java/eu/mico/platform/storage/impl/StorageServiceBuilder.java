package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.util.VFSUtils;

import java.net.URI;

/**
 * Builds proper StorageService depending on the URL.
 *
 * @author Horst Stadler
 */
public class StorageServiceBuilder {

    public static StorageService buildStorageService(URI url) {
        String userInfo = url.getUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null) {
            username = userInfo.split(":", 2)[0];
            if (userInfo.contains(":")) {
                password = userInfo.split(":", 2)[1];
            }
        }

        switch(url.getScheme().toLowerCase()) {
            case "ftp":
                VFSUtils.configure();
                return new StorageServiceFTP(url.getHost(), url.getPort(), url.getPath(), username, password);
            case "hdfs":
                return new StorageServiceHDFS(url.getHost(), url.getPort(), url.getPath());
            case "file":
                return new StorageServiceLocalFS(url);
        }

        return null;
    }
}
