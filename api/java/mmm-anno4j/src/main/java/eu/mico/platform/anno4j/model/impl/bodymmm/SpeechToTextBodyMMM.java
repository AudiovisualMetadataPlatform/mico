package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;
import org.openrdf.repository.object.LangString;

/**
 * Class represents the body for a SpeechToText annotation. The relevant timestamp information is stored in the
 * respective selector. The body itself contains which word has been detected.
 */
@Iri(MMMTERMS.STT_BODY_MICO)
public interface SpeechToTextBodyMMM extends BodyMMM {

    @Iri(RDF.VALUE)
    LangString getValue();

    /**
     * The value of the body corresponds to the word that is detected.
     */
    @Iri(RDF.VALUE)
    void setValue(LangString value);

}