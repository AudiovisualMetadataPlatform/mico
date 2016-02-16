package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

@Iri(MMM.FACE_DETECTION_BODY)
public interface FaceDetectionBodyMMM extends BodyMMM {


    @Iri(MMM.HAS_CONFIDENCE)
    Double getConfidence();

    @Iri(MMM.HAS_CONFIDENCE)
    void setConfidence(Double confidence);
}
