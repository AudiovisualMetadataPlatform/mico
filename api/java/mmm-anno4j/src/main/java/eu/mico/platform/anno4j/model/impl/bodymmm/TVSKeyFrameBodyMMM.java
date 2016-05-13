package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.DC;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Class represents a KeyFrame of a given TVS analysis.
 */
@Iri(MMMTERMS.TVS_KEY_FRAME_BODY)
public interface TVSKeyFrameBodyMMM extends TVSBodyMMM {

    @Iri(DC.FORMAT)
    String getFormat();

    @Iri(DC.FORMAT)
    void setFormat(String format);
}
