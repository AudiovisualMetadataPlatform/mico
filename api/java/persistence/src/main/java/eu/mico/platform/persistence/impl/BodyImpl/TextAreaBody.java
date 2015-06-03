package eu.mico.platform.persistence.impl.BodyImpl;

import com.github.anno4j.model.Body;
import com.github.anno4j.model.ontologies.CNT;
import com.github.anno4j.model.ontologies.DCTYPES;
import com.github.anno4j.model.ontologies.RDF;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(DCTYPES.TEXT)
public class TextAreaBody extends Body {

    @Iri(Ontology.FORMAT_DCTERMS)
    private String format;
    
    @Iri(CNT.CHARS)
    private String value;
    
    @Iri(RDF.VALUE)
    private final String TYPE = CNT.CONTENT_AS_TEXT;

    public TextAreaBody() {
    }

    public TextAreaBody(String format, String value) {
        this.format = format;
        this.value = value;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
