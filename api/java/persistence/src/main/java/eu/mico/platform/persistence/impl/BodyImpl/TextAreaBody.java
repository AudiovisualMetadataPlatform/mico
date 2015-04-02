package eu.mico.platform.persistence.impl.BodyImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.TEXT_DCTYPES)
public class TextAreaBody extends ModelPersistenceBodyImpl {

    @Iri(Ontology.FORMAT_DCTERMS)
    private String format;
    
    @Iri(Ontology.CHARS_CNT)
    private String value;
    
    @Iri(Ontology.TYPE_RDF)
    private final String TYPE = Ontology.CONTENT_AS_TEXT_CNT;

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
