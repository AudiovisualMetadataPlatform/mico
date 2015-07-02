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

    /**
     * Gets Confidence value for the detected shotkeyframe.
     *
     * @return Value of Confidence value for the detected shotkeyframe.
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * Sets new Confidence value for the detected shotkeyframe.
     *
     * @param confidence New value of Confidence value for the detected shotkeyframe.
     */
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
