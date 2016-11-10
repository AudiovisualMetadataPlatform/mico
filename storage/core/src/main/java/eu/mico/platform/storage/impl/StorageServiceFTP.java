/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.util.VFSUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.util.HashMap;


/**
 * FTP-based storage implementation, port of the first API implementation
 *
 * @author Sergio Fernández
 */
public class StorageServiceFTP implements StorageService {

    private String baseUrl;

    public StorageServiceFTP(String host, int port, String basePath, String username, String password) {
        baseUrl = "ftp://";
        if (username != null && !username.isEmpty()) {
            baseUrl += username;
            if (password != null && !password.isEmpty()) {
                baseUrl += ":" + password;
            }
            baseUrl += "@";
        }
        baseUrl += host;
        if (port > 0 && port < 65536) {
            baseUrl += ":" + Integer.toString(port);
        }
        if (basePath != null && !basePath.isEmpty()) {
            baseUrl += basePath;

        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        VFSUtils.configure();
    }

    public StorageServiceFTP(String host, String username, String password) {
        this(host, -1, null, username, password);

    }

    public StorageServiceFTP(HashMap<String, String> params) {
        this(params.get("host") != null ? params.get("host") : "localhost",
             params.get("port") != null ? Integer.parseInt(params.get("port")) : -1,
             params.get("basePath") != null ? params.get("basePath") : "/",
             params.get("user") != null ? params.get("user") : "mico",
             params.get("pass") != null ? params.get("pass") : "mico"
        );
    }

    @Override
    public OutputStream getOutputStream(URI contentPath) throws IOException {
        FileSystemManager fsmgr = VFS.getManager();
        final FileObject d = fsmgr.resolveFile(getContentItemURL(contentPath.getPath()));
        final FileObject f = fsmgr.resolveFile(getContentPartURL(contentPath.getPath()));
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
    public InputStream getInputStream(URI contentPath) throws IOException {
        FileSystemManager fsmgr = VFS.getManager();
        final FileObject f = fsmgr.resolveFile(getContentPartURL(contentPath.getPath()));
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

    @Override
    public boolean delete(URI contentPath) throws IOException {
        FileSystemManager fsmgr = VFS.getManager();
        final FileObject f = fsmgr.resolveFile(getContentPartURL(contentPath.getPath()));
        if(f.getParent().exists() && f.exists()) {
            return f.delete();
        }
        return false;
    }

    private String getContentItemURL(String contentPath) {
        if (contentPath != null) {
            while (contentPath.startsWith("/"))
                contentPath = contentPath.substring(1);
        }
        if (contentPath == null || contentPath.isEmpty() || !contentPath.contains("/"))
            return baseUrl;
        if (contentPath.endsWith("/"))
            return baseUrl + contentPath;
        return baseUrl + contentPath.substring(0, contentPath.lastIndexOf("/") + 1);
    }

    private String getContentPartURL(String contentPath) {
        if (contentPath != null) {
            while (contentPath.startsWith("/"))
                contentPath = contentPath.substring(1);
        }
        if (contentPath == null || contentPath.isEmpty() || contentPath.endsWith("/"))
            return null;
        return baseUrl + contentPath + ".bin";
    }
}
