package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;
import org.openrdf.repository.object.LangString;

/**
 * Body class for the Speech-Music-Discrimination extractor. This body is used when only music is detected during a time
 * span in the given audio stream.
 *
 * The relevant time information is stored in the associated specific resource of the given part, or rather in the
 * associated temporal FragmentSelector.
 */
@Iri(MMMTERMS.SPEECH_MUSIC_TO_TEXT_BODY)
public interface SpeechMusicToTextBodyMMM extends BodyMMM {

    @Iri(RDF.VALUE)
    LangString getValue();

    /**
     * The value of the body corresponds to the word that is detected.
     */
    @Iri(RDF.VALUE)
    void setValue(LangString value);
}
