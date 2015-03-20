package eu.mico.platform.persistence.impl.BodyImpl;


import eu.mico.platform.persistence.impl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.metadata.IBody;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.AVQ_BODY_MICO)
public class AVQBody extends ModelPersistenceBodyImpl {

    @Iri(Ontology.HAS_REDEFINED_TYPE_MICO)
    private String type;
    
    @Iri(Ontology.VALUE_RDF)
    private String result;

    public AVQBody() {
    }

    public AVQBody(String type, String result) {
        this.type = type;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
