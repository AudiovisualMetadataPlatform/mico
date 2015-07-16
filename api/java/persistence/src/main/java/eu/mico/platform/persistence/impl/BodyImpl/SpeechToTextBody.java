package eu.mico.platform.persistence.impl.BodyImpl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.ontologies.RDF;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

/**
 * Class represents the body for a SpeechToText annotation. The relevant timestamp information is stored in the
 * respective selector. The body itself contains which word has been detected.
 */
@Iri(Ontology.STT_BODY_MICO)
public class SpeechToTextBody extends Body {

    /**
     * The value of the body corresponds to the word that is detected.
     */
    @Iri(RDF.VALUE)
    private String value;

    /**
     * Default constructor.
     */
    public SpeechToTextBody() {};

    /**
     * Constructor also setting the value.
     * @param value The word that is detected.
     */
    public SpeechToTextBody(String value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return Value of value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets new value.
     *
     * @param value New value of value.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
