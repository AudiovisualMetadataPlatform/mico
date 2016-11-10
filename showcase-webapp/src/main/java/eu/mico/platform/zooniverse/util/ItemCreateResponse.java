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

package eu.mico.platform.zooniverse.util;


import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemCreateResponse {
    private URI itemUri;
    private AssetLocation assetLocation;
    private LocalDateTime created;
    private String syntacticalType;

    public void setItemUri(String itemUri) throws URISyntaxException { this.itemUri = new URI(itemUri); }
    public void setAssetLocation(AssetLocation assetLocation) { this.assetLocation = assetLocation; }
    public void setCreated(String created) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        this.created = LocalDateTime.from(f.parse(created));
    }
    public void setSyntacticalType(String syntacticalType) { this.syntacticalType = syntacticalType;}

    public URI getItemUri() { return itemUri; }
    public AssetLocation getAssetLocation() { return assetLocation; }
    public LocalDateTime getCreationDateTime() { return created; }
    public String getSyntacticalType() { return syntacticalType; }
    public String getItemId() {
        String path = itemUri.getPath();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        int idx = path.lastIndexOf("/");
        if (idx >= 0) {
            path = path.substring(idx + 1);
        }
        return path;
    }
}

