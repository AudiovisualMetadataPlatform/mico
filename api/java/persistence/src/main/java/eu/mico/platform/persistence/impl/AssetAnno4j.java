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

package eu.mico.platform.persistence.impl;

import eu.mico.platform.anno4j.model.AssetMMM;
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
        log.trace("Open Outputstream for Asset with id {} and location {}", this.assetMMM.getResourceAsString(), this.assetMMM.getLocation());
        return storageService.getOutputStream(getAssetPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        log.trace("Open Inputstream for Asset with id {} and location {}", this.assetMMM.getResourceAsString(), this.assetMMM.getLocation());
        return storageService.getInputStream(getAssetPath());
    }

    @Override
    public String getName() {
        return this.assetMMM.getName();
    }

    @Override
    public void setName(String name) {
        this.assetMMM.setName(name);
    }

    private java.net.URI getAssetPath() {
        try {
            String path = this.assetMMM.getLocation();
            if (path == null) {
                throw new NullPointerException("asset does not have a location.");
            }
            if (path.startsWith(STORAGE_SERVICE_URN_PREFIX)) {
                String[] pathParts = path.substring(path.lastIndexOf(':') + 1).split("/");
                if (pathParts.length >= 2) {
                    String assetPath = "/" + pathParts[pathParts.length - 2] + "/" + pathParts[pathParts.length - 1];
                    return new java.net.URI(assetPath);
                }
            }
            throw new IllegalStateException("Invalid asset path " + path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cant parse path from URI " + this.assetMMM.getLocation(), e);
        }
    }
}
