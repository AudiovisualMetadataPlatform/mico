package eu.mico.platform.reco.Resources;

import eu.mico.platform.anno4j.model.fam.LinkedEntityBody;

import java.net.URI;

public class EntityInfo {

    private URI reference;
    private String label;


    public EntityInfo(LinkedEntityBody b) {

        String uriString = b.getEntity().getResource().toString();
        try {
            uriString = b.getEntity().getResource().toString();

            // remove invisible control characters
            uriString = uriString.replaceAll("\\p{C}", "");

            this.reference = URI.create(uriString);
        } catch (IllegalArgumentException e) {
            this.reference = URI.create("http://dbpedia.org/page/Illegal_URI");
        }
        this.label = b.getEntity().toString();

    }

    public URI getReference() {
        return reference;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "EntityInfo{" +
                "reference=" + reference +
                ", label='" + label + '\'' +
                '}';
    }
}
