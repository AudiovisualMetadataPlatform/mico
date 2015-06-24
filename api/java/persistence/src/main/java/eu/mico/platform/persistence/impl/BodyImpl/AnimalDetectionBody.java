package eu.mico.platform.persistence.impl.BodyImpl;

import com.github.anno4j.model.Body;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.ANIMAL_DETECTION_BODY_MICO)
public class AnimalDetectionBody extends Body {

    /**
     * Confidence value for the detected animal
     */
    @Iri(Ontology.HAS_CONFIDENCE_MICO)
    private Double confidence;

    public AnimalDetectionBody() {
    }

    public AnimalDetectionBody(Double confidence) {
        this.confidence = confidence;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
