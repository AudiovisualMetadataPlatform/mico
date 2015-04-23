package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * HDFS-based storage implementation
 *
 * @author Horst Stadler
 */
public class StorageServiceHDFS implements StorageService {

    private final Configuration hdfsConfig;
    private Path basePath;

    public StorageServiceHDFS(String host, int port, String basePath) {
        String defaultFS = "hdfs://" + host;
        if (port > 0 && port < 65536) {
            defaultFS += ":" + Integer.toString(port);
        }


        hdfsConfig = new Configuration();
        hdfsConfig.set("fs.defaultFS", defaultFS);

        String path = basePath;
        if (path == null || path.isEmpty()) {
            path = Path.SEPARATOR;
        }

        if (!path.startsWith(Path.SEPARATOR)) {
            path = Path.SEPARATOR + path;
        }
        if (!path.endsWith(Path.SEPARATOR)) {
            path += Path.SEPARATOR;
        }
        this.basePath = new Path(null, null, path);
    }

    public StorageServiceHDFS(HashMap<String, String> params) {
        this(params.get("host") != null ? params.get("host") : "localhost",
             params.get("port") != null ? Integer.parseInt(params.get("port")) : -1,
             params.get("basePath") != null ? params.get("basePath") : null);
    }

    @Override
    public OutputStream getOutputStream(URI contentPath) throws IOException {
        final FileSystem fs = FileSystem.get(this.hdfsConfig);
        Path path = getContentPartPath(contentPath.getPath());
        return new ProxyOutputStream(fs.create(path)) {
            @Override
            public void close() throws IOException {
                super.close();
                fs.close();
            }
        };

    }

    @Override
    public InputStream getInputStream(URI contentPath) throws IOException {
        final FileSystem fs = FileSystem.get(this.hdfsConfig);
        Path path = getContentPartPath(contentPath.getPath());
        return new ProxyInputStream(fs.open(path)) {
            @Override
            public void close() throws IOException {
                super.close();
                fs.close();
            }
        };
    }

    @Override
    public boolean delete(URI contentPath) throws IOException {
        final FileSystem fs = FileSystem.get(this.hdfsConfig);
        Path path = getContentPartPath(contentPath.getPath());
        if (fs.delete(path, false)) {
            while ((path = path.getParent()) != null && !fs.listFiles(path, false).hasNext()) {
                fs.delete(path, false);
            }
            return true;
        }
        return false;
    }

    private Path getContentPartPath(String contentPath) {
        if (contentPath == null || contentPath.isEmpty() || contentPath.endsWith(Path.SEPARATOR))
            return null;
        while (contentPath.startsWith(Path.SEPARATOR))
            contentPath = contentPath.substring(Path.SEPARATOR.length());
        return Path.mergePaths(basePath, new Path(null, null, contentPath + ".bin"));
    }

}
