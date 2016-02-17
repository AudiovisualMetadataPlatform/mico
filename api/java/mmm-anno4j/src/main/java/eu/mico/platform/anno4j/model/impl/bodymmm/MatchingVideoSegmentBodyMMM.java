package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.impl.targetmmm.SpecificResourceMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body class for a matching segments of two videos.
 *
 * As this class inherits MicoSpecificResource, its also inherits its behaviour of having a Selector (to
 * specify the associated Fragment) and a Source (specifying the "target" of this "SpecificResource").
 */
@Iri(MMM.MATCHING_VIDEO_SEGMENT_BODY)
public interface MatchingVideoSegmentBodyMMM extends BodyMMM, SpecificResourceMMM {

    // Inherited Selector and Source edges from MicoSpecificResource.
}
