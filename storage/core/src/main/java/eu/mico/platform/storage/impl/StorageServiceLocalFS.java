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
        if (!parentDir.exists()) {
            parentDir.mkdirs();
            //grant read and write permissions for the directory to every user
            parentDir.setReadable(true, false);
            parentDir.setWritable(true, false);
            parentDir.setExecutable(true, false);
        }
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
        String contentPathPath = contentPath.getPath();
        if (contentPathPath == null)    {
            throw new IllegalArgumentException("contentPath must contain path");
        }

        // "plain" usage of URI.get keeps trailing slash. Paths.get() therefore fails  with
        // java.nio.file.InvalidPathException: Illegal char <:> at index 2: /X:/workspace/bla

        // checks for "windows-style" /C:/... paths
        if(contentPathPath.matches("/\\p{Upper}:/.*"))    {
            // and removes trailing slash:
            contentPathPath = contentPathPath.substring(1);

        }

        String path = Paths.get(contentPathPath).normalize().toString();
        while(path.startsWith(File.separator))
            path = path.substring(File.separator.length());
        if (contentPath.getPath().endsWith(File.separator) || path.isEmpty())
            return null;
        File file = basePath.resolve(path + ".bin").normalize().toFile();
        if (!file.toPath().startsWith(basePath))
            return null;
        file.setReadable(true, false);    //everybody can read
        file.setWritable(true, true);     //owner can write
        file.setExecutable(false, false); //no one can execute
        return file;
    }
}
