package eu.mico.platform.persistence.impl;

import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.storage.api.StorageService;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

public class AssetAnno4j implements Asset {

    private static Logger log = LoggerFactory.getLogger(AssetAnno4j.class);

    private final StorageService storageService;
    private final AssetMMM assetMMM;

    public AssetAnno4j(AssetMMM assetMMM, StorageService storageService) {
        this.assetMMM = assetMMM;
        this.storageService = storageService;
    }

    @Override
    public URI getLocation() {
        return new URIImpl(this.assetMMM.getLocation());
    }

    @Override
    public String getFormat() {
        return this.assetMMM.getFormat();
    }

    @Override
    public void setFormat(String format) {
        this.assetMMM.setFormat(format);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        log.trace("Open Outputstream for Asset with id {} and location {}", assetMMM.getResourceAsString(), getLocation());
        return storageService.getOutputStream(getAssetPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        log.trace("Open Inputstream for Asset with id {} and location {}", assetMMM.getResourceAsString(), getLocation());
        return storageService.getInputStream(getAssetPath());
    }

    private java.net.URI getAssetPath() {
        try {
            String path = new java.net.URI(this.assetMMM.getLocation()).getPath();
            String []pathParts = path.replaceAll("^/+", "").replaceAll("/+$", "").split("/");
            if (pathParts.length >= 2) {
                String assetPath = "/" + pathParts[pathParts.length-2] + "/" + pathParts[pathParts.length-1];
                return new java.net.URI(assetPath);
            }
            throw new IllegalStateException("Invalid asset path " + path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cant parse path from URI " + this.assetMMM.getLocation(), e);
        }
    }
}
