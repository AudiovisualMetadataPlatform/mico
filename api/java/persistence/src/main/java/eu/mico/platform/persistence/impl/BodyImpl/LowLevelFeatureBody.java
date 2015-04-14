package eu.mico.platform.persistence.impl.BodyImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.LOW_LEVEL_FEATURE_BODY_MICO)
public class LowLevelFeatureBody extends ModelPersistenceBodyImpl {
    
    @Iri(Ontology.FORMAT_DCTERMS)
    private String format;
    
    @Iri(Ontology.HAS_LOCATION_MICO)
    private String location;

    public LowLevelFeatureBody() {
    }

    public LowLevelFeatureBody(String format, String location) {
        this.format = format;
        this.location = location;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
