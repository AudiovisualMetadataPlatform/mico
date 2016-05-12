package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Body class for the Speech-Music-Discrimination extractor. This body is used when only music is detected during a time
 * span in the given audio stream.
 *
 * The relevant time information is stored in the associated specific resource of the given part, or rather in the
 * associated temporal FragmentSelector.
 */
@Iri(MMMTERMS.SPEECH_MUSIC_BODY)
public interface SpeechMusicBodyMMM extends BodyMMM {
}
