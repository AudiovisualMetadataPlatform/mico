package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl.ModelPersistenceBodyImpl;
import eu.mico.platform.persistence.metadata.IAnnotation;
import eu.mico.platform.persistence.metadata.IBody;
import eu.mico.platform.persistence.metadata.IProvenance;
import eu.mico.platform.persistence.metadata.ITarget;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;


@Iri(Ontology.NS_OA + "Annotation")
public class AnnotationImpl extends ModelPersistenceBodyImpl implements IAnnotation {

    @Iri(Ontology.HAS_BODY_OA)
    private IBody body;

    @Iri(Ontology.HAS_TARGET_OA)
    private ITarget target;

    @Iri(Ontology.ANNOTATED_BY_OA)
    private IProvenance provenance;


    @Override
    public void setBody(IBody body) {
        this.body = body;
    }

    @Override
    public void setTarget(ITarget target) {
        this.target = target;
    }
    
    @Override
    public IBody getBody() {
        return body;
    }

    @Override
    public ITarget getTarget() {
        return target;
    }

    @Override
    public void setProvenance(IProvenance provenance) {
        this.provenance = provenance;
    }

    @Override
    public IProvenance getProvenance() {
        return provenance;
    }


}
