package eu.mico.platform.persistence.impl.BodyImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl;
import eu.mico.platform.persistence.metadata.IBody;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.COLOR_LAYOUT_RAW_BODY)
public class ColorLayoutRawBody extends ModelPersistenceImpl implements IBody {
    
    @Iri(Ontology.HAS_LOCATION_MICO)
    private String layoutLocation;
    
    @Iri(Ontology.FORMAT_DCTERMS)
    private String format;

    public ColorLayoutRawBody() {
    }

    public ColorLayoutRawBody(String layoutLocation, String format) {
        this.layoutLocation = layoutLocation;
        this.format = format;
    }

    public String getLayoutLocation() {
        return layoutLocation;
    }

    public void setLayoutLocation(String layoutLocation) {
        this.layoutLocation = layoutLocation;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
