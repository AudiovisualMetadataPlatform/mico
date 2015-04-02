package eu.mico.platform.persistence.impl.BodyImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.FACE_RECOGNITION_BODY_MICO)
public class FaceRecognitionBody extends ModelPersistenceBodyImpl {

    /**
     * The name of the person that was detected
     */
    @Iri(Ontology.VALUE_RDF)
    private String detection;

    /**
     * Confidence value for the detected face
     */
    @Iri(Ontology.HAS_CONFIDENCE_MICO)
    private Double confidence;

    public FaceRecognitionBody() {
    }

    public FaceRecognitionBody(String detection, Double confidence) {
        this.detection = detection;
        this.confidence = confidence;
    }

    public String getDetection() {
        return detection;
    }

    public void setDetection(String detection) {
        this.detection = detection;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
