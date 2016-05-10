package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body class for the Diarization extractor. Diarization detects spoken parts in a audio file and assigns different speaker roles.
 * One body of this class represents one detected time span of a speaker.
 *
 * The timestamp is to be set via a temporal FragmentSelector assigned to the respective specific resource of the given Part.
 */
@Iri(MMM.DIARIZATION_BODY)
public interface DiarizationBodyMMM extends BodyMMM{

    @Iri(RDF.VALUE)
    void setSpeaker(String speaker);

    @Iri(RDF.VALUE)
    String getSpeaker();
}
