package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Storage implementation for local filesystem
 *
 * @author Horst Stadler
 */
public class StorageServiceLocalFS implements StorageService {

    private Path basePath;

    public StorageServiceLocalFS(URI basePath) {
        this.basePath = Paths.get(basePath).normalize().toAbsolutePath();
    }

    @Override
    public OutputStream getOutputStream(URI contentPath) throws IOException {
        File file = getContentPartPath(contentPath);
        if (file == null)
            return null;
        File parentDir = file.getParentFile();
        if (!parentDir.exists())
            parentDir.mkdirs();
        return new FileOutputStream(file);
    }

    @Override
    public InputStream getInputStream(URI contentPath) throws IOException {
        File file = getContentPartPath(contentPath);
        if (file == null)
            return null;
        return new FileInputStream(file);
    }

    @Override
    public boolean delete(URI contentPath) throws IOException {
        File file = getContentPartPath(contentPath);
        if (file == null)
            return false;
        boolean success = file.delete();
        File parent = file.getParentFile();
        if (parent.exists() && parent.list().length == 0) {
            parent.delete();
        }
        return success;
    }

    private File getContentPartPath(URI contentPath) {
        String path = Paths.get(contentPath.getPath()).normalize().toString();
        while(path.startsWith(File.separator))
            path = path.substring(File.separator.length());
        if (contentPath.getPath().endsWith(File.separator) || path.isEmpty())
            return null;
        File file = basePath.resolve(path + ".bin").normalize().toFile();
        if (!file.toPath().startsWith(basePath))
            return null;
        return file;
    }
}
