package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

@Iri(MMMTERMS.TVS_BODY)
public interface TVSBodyMMM extends BodyMMM {


    /**
     * Gets Confidence value for the detected shotkeyframe.
     *
     * @return Value of Confidence value for the detected shotkeyframe.
     */
    @Iri(MMM.HAS_CONFIDENCE)
    Double getConfidence();

    /**
     * Sets new Confidence value for the detected shotkeyframe.
     *
     * @param confidence New value of Confidence value for the detected shotkeyframe.
     */
    @Iri(MMM.HAS_CONFIDENCE)
    void setConfidence(Double confidence);
}
