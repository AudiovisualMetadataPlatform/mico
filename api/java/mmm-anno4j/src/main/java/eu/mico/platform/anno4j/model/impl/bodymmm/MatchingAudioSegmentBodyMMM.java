package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body class for a matching segments of two videos. Inherits from the MatchingSegmentBodyMMM and therefore adapts its behaviour.
 * But it adds more semantic by its type, treating matches in audio files.
 */
@Iri(MMM.MATCHING_AUDIO_SEGMENT_BODY)
public interface MatchingAudioSegmentBodyMMM extends MatchingSegmentBodyMMM {

}
