package eu.mico.platform.persistence.impl.BodyImpl;

import com.github.anno4j.model.Body;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

/**
 * Class represents the body for a silent period of a SpeechToText analysis. The relevant timestamp information is stored in the
 * respective selector. The body itself contains nothing but represents the silence in the annotated time period.
 */
@Iri(Ontology.STT_SILENCE_BODY_MICO)
public class SpeechSilenceBody extends Body {

}
