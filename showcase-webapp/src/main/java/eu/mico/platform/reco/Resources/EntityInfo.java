package eu.mico.platform.reco.Resources;

import eu.mico.platform.anno4j.model.fam.LinkedEntityBody;

import java.net.URI;

public class EntityInfo {

    private URI reference;
    private String label;


    public URI getReference() {
        return reference;
    }

    public String getLabel() {
        return label;
    }

    public EntityInfo(LinkedEntityBody b) {

        this.reference = URI.create(b.getEntity().getResource().toString());
        this.label = b.getEntity().toString();

    }


    @Override
    public String toString() {
        return "EntityInfo{" +
                "reference=" + reference +
                ", label='" + label + '\'' +
                '}';
    }
}
