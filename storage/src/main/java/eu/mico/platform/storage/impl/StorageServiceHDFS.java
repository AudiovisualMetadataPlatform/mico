package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.model.Content;
import eu.mico.platform.storage.model.ContentItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang.NotImplementedException;
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

    public StorageServiceHDFS(String host) throws IOException {
        hdfsConfig = new Configuration();
        hdfsConfig.set("fs.defaultFS", "hdfs://" + host);
    }

    public StorageServiceHDFS(HashMap<String, String> params) throws IOException {
        this(params.get("host") != null ? params.get("host") : "localhost");
    }

    @Override
    public Collection<ContentItem> list() {
        throw new NotImplementedException(); //TODO
    }

    @Override
    public OutputStream getOutputStream(Content part) throws IOException {
        final FileSystem fs = FileSystem.get(this.hdfsConfig);
        Path path = getContentPartPath(part);
        return new ProxyOutputStream(fs.create(path)) {
            @Override
            public void close() throws IOException {
                super.close();
                fs.close();
            }
        };

    }

    @Override
    public InputStream getInputStream(Content part) throws IOException {
        final FileSystem fs = FileSystem.get(this.hdfsConfig);
        Path path = getContentPartPath(part);
        return new ProxyInputStream(fs.open(path)) {
            @Override
            public void close() throws IOException {
                super.close();
                fs.close();
            }
        };
    }

    private Path getContentPartPath(Content part) {
        return new Path(null, null, "/" + part.getId() + ".bin");
    }

}
