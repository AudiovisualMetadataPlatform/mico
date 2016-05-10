package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Class represents the body for a silent period of a SpeechToText analysis. The relevant timestamp information is stored in the
 * respective selector. The body itself contains nothing but represents the silence in the annotated time period.
 */
@Iri(MMM.STT_SILENCE_BODY_MICO)
public interface SpeechSilenceBodyMMM extends BodyMMM {

}