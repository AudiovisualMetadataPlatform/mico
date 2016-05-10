package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

@Iri(MMM.FACE_RECOGNITION_BODY)
public interface FaceRecognitionBodyMMM extends BodyMMM {


    @Iri(RDF.VALUE)
    public String getDetection();

    /**
     * The name of the person that was detected
     */
    @Iri(RDF.VALUE)
    public void setDetection(String detection);

    @Iri(MMM.HAS_CONFIDENCE)
    public Double getConfidence();

    /**
     * Confidence value for the detected face
     */
    @Iri(MMM.HAS_CONFIDENCE)
    public void setConfidence(Double confidence);
}
