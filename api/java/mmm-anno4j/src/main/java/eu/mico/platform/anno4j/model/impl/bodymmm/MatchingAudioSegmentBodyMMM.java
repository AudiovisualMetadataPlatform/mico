package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.impl.targetmmm.SpecificResourceMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body class for a matching audio segments.
 * <p/>
 * As this class inherits SpecificResourceMMM, its also inherits its behaviour of having a Selector (to
 * specify the associated Fragment) and a Source (specifying the "target" of this "SpecificResource").
 */
@Iri(MMM.MATCHING_AUDIO_SEGMENT_BODY)
public interface MatchingAudioSegmentBodyMMM extends BodyMMM, SpecificResourceMMM {

    // Inherited Selector and Source edges from MicoSpecificResource.
}
