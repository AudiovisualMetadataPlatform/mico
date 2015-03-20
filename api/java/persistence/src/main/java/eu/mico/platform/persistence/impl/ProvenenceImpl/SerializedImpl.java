package eu.mico.platform.persistence.impl.ProvenenceImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.metadata.IProvenance;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

import java.util.Date;

@Iri(Ontology.ANNOTATION_OA)
public class SerializedImpl extends ModelPersistenceBodyImpl implements IProvenance {
    
    @Iri(Ontology.SERIALIZED_AT_OA)
    private Date serializedAt;
    
    @Iri(Ontology.SERIALIZED_BY_OA)
    private int serializedBy;

    public SerializedImpl() {
    }

    public SerializedImpl(Date serializedAt, int serializedBy) {
        this.serializedAt = serializedAt;
        this.serializedBy = serializedBy;
    }

    public Date getSerializedAt() {
        return serializedAt;
    }

    public void setSerializedAt(Date serializedAt) {
        this.serializedAt = serializedAt;
    }

    public int getSerializedBy() {
        return serializedBy;
    }

    public void setSerializedBy(int serializedBy) {
        this.serializedBy = serializedBy;
    }
}
