package eu.mico.platform.persistence.impl;

import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
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

    private final PersistenceService persistenceService;
    private final AssetMMM assetMMM;

    public AssetAnno4j(AssetMMM assetMMM, PersistenceService persistenceService) {
        this.assetMMM = assetMMM;
        this.persistenceService = persistenceService;
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
        try {
            log.debug("Open Outputstream for Asset with id {} and location {}", assetMMM.getResourceAsString(), getLocation());
            return this.persistenceService.getStorage().getOutputStream(new java.net.URI(this.assetMMM.getLocation()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cant parse URI from " + this.assetMMM.getLocation(), e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            log.debug("Open Inputstream for Asset with id {} and location {}", assetMMM.getResourceAsString(), getLocation());
            return this.persistenceService.getStorage().getInputStream(new java.net.URI(this.assetMMM.getLocation()));
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException("Cant parse URI from " + this.assetMMM.getLocation(), e);
        }
    }
}
