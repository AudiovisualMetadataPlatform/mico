package eu.mico.platform.persistence.impl.ProvenenceImpl;


import eu.mico.platform.persistence.impl.ModelPersistenceImpl;
import eu.mico.platform.persistence.metadata.IProvenance;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

import java.util.Date;

@Iri(Ontology.ANNOTATION_OA)
public class AnnotatedImpl extends ModelPersistenceImpl implements IProvenance {
    
    @Iri(Ontology.ANNOTATED_AT_OA)
    private Date annotatedAt;
    
    @Iri(Ontology.ANNOTATED_BY_OA)
    private int extractorID;

    public AnnotatedImpl() {
    }

    public AnnotatedImpl(Date annotatedAt, int extractorID) {
        this.annotatedAt = annotatedAt;
        this.extractorID = extractorID;
    }

    public Date getAnnotatedAt() {
        return annotatedAt;
    }

    public void setAnnotatedAt(Date getAnnotatedAt) {
        this.annotatedAt = annotatedAt;
    }

    public int getExtractorID() {
        return extractorID;
    }

    public void setExtractorID(int extractorID) {
        this.extractorID = extractorID;
    }
}
