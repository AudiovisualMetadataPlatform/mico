package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.model.Content;
import eu.mico.platform.storage.model.ContentItem;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

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

    @Override
    public Collection<ContentItem> list() {
        return null; //TODO
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
