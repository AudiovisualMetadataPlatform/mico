package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body class for a matching segments of two videos. Inherits from the MatchingSegmentBodyMMM and therefore adapts its behaviour.
 * But it adds more semantic by its type, treating matches in video files.
 */
@Iri(MMM.MATCHING_VIDEO_SEGMENT_BODY)
public interface MatchingVideoSegmentBodyMMM extends MatchingSegmentBodyMMM {

}
