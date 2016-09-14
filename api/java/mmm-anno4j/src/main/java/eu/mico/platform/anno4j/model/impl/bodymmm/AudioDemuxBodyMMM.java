package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.gen.MA;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Body class for the Audio Demux extractor. Audio Demux excerpts the audio track from a given video file.
 * The audio file should be added as an Asset to the respective Part class of this body.
 */
@Iri(MMMTERMS.AUDIO_DEMUX_BODY)
public interface AudioDemuxBodyMMM extends BodyMMM {

    @Iri(MA.SAMPLING_RATE_STRING)
    void setFrameRate(String framerate);

    @Iri(MA.SAMPLING_RATE_STRING)
    String getFrameRate();
}
