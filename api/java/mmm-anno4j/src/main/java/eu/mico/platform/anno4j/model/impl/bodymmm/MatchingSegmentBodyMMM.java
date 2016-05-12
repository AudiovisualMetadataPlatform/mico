package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.impl.targetmmm.SpecificResourceMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 *
 * As this class inherits MicoSpecificResource, its also inherits its behaviour of having a Selector (to
 * specify the associated Fragment) and a Source (specifying the "target" of this "SpecificResource").
 */
@Iri(MMMTERMS.MATCHING_SEGMENT_BODY)
public interface MatchingSegmentBodyMMM extends BodyMMM, SpecificResourceMMM {

    // Inherited SpecificResource behaviour of having a source (another file that this relates to) and possibly a
    // selector which can further specify the selection by supporting a media fragment

    @Iri(MMMTERMS.HAS_FINGERPRINT)
    void setFingerprint(String fingerprint);

    @Iri(MMMTERMS.HAS_FINGERPRINT)
    String getFingerprint();
}
