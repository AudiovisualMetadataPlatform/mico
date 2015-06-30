package eu.mico.platform.persistence.impl.BodyImpl;


import com.github.anno4j.model.Body;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.TVS_BODY_MICO)
public class TVSBody extends Body {

    /**
     * Confidence value for the detected shot/keyframe
     */
    @Iri(Ontology.HAS_CONFIDENCE_MICO)
    private Double confidence;

    public TVSBody() {
    }

    public TVSBody(Double confidence) {
        this.confidence = confidence;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
