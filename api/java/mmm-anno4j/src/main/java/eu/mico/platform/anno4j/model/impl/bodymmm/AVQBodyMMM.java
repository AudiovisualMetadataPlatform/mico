package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body-implementation for the Audio-Video-Quality extractor.
 * Has two subclasses {@link AVQShotBodyMMM} and {@link AVQKeyFrameBodyMMM}.
 */
@Iri(MMM.AVQ_BODY)
public interface AVQBodyMMM extends BodyMMM {

    /**
     * Sets new Confidence value for the detected shotkeyframe.
     *
     * @param confidence New value of Confidence value for the detected shotkeyframe.
     */
    @Iri(MMM.HAS_CONFIDENCE)
    void setConfidence(Double confidence);

    /**
     * Gets Confidence value for the detected shotkeyframe.
     *
     * @return Value of Confidence value for the detected shotkeyframe.
     */
    @Iri(MMM.HAS_CONFIDENCE)
    Double getConfidence();
}
