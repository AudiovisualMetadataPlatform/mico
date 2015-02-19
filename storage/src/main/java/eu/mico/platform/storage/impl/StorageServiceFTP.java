package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.model.Content;
import eu.mico.platform.storage.model.ContentItem;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * FTP-based storage implementation, port of the first API implementation
 *
 * @author Sergio Fernández
 */
public class StorageServiceFTP implements StorageService {

    private final String host, user, pass, contentUrl;

    public StorageServiceFTP(String host, String user, String pass) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.contentUrl = "ftp://" + user + ":" + pass + "@" + host;
    }

    public StorageServiceFTP(HashMap<String, String> params) {
        this(params.get("host") != null ? params.get("host") : "localhost",
             params.get("user") != null ? params.get("user") : "mico",
             params.get("pass") != null ? params.get("pass") : "mico"
        );
    }

    @Override
    public Collection<ContentItem> list() {
        throw new NotImplementedException(); //TODO
    }

    @Override
    public OutputStream getOutputStream(Content part) throws IOException {
        FileSystemManager fsmgr = VFS.getManager();
        final FileObject d = fsmgr.resolveFile(getContentItemPath(part));
        final FileObject f = fsmgr.resolveFile(getContentPartPath(part));
        if(!d.exists()) {
            d.createFolder();
        }
        f.createFile();
        return new ProxyOutputStream(f.getContent().getOutputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                f.close();
                d.close();
            }
        };
    }

    @Override
    public InputStream getInputStream(Content part) throws IOException {
        FileSystemManager fsmgr = VFS.getManager();
        final FileObject f = fsmgr.resolveFile(getContentPartPath(part));
        if(f.getParent().exists() && f.exists()) {
            return new ProxyInputStream(f.getContent().getInputStream()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    f.close();
                }
            };
        } else {
            return null;
        }
    }

    private String getContentItemPath(Content part) {
        return contentUrl + "/" + part.getId().substring(0, part.getId().lastIndexOf('/'));
    }

    private String getContentPartPath(Content part) {
        return contentUrl + "/" + part.getId() + ".bin";
    }

}
