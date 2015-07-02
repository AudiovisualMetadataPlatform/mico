package eu.mico.platform.persistence.impl.BodyImpl;


import com.github.anno4j.model.Body;
import com.github.anno4j.model.ontologies.RDF;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

/**
 * Body-implementation for the Audio-Video-Quality extractor.
 * Has two subclasses {@link eu.mico.platform.persistence.impl.BodyImpl.AVQShotBody} and {@link eu.mico.platform.persistence.impl.BodyImpl.AVQKeyFrameBody}.
 */
@Iri(Ontology.AVQ_BODY_MICO)
public class AVQBody extends Body {

    /**
     * Confidence value for the detected shot/keyframe
     */
    @Iri(Ontology.HAS_CONFIDENCE_MICO)
    private Double confidence;

    public AVQBody() {
    }

    public AVQBody(Double confidence) {
        this.confidence = confidence;
    }

    /**
     * Sets new Confidence value for the detected shotkeyframe.
     *
     * @param confidence New value of Confidence value for the detected shotkeyframe.
     */
    public void setConfidence(Double confidence) {
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
}
