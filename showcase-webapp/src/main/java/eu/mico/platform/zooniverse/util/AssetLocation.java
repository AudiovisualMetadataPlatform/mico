package eu.mico.platform.zooniverse.util;


import java.net.URI;
import java.net.URISyntaxException;

public class AssetLocation {
    private URI namespace;
    private String localName;

    public URI getNamespace() { return namespace; }
    public String getLocalName() { return localName; }

    public void setNamespace(String namespace) throws URISyntaxException { this.namespace = new URI(namespace); }
    public void setLocalName(String localName) { this.localName = localName; }
}