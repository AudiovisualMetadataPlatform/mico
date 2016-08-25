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

