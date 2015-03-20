package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.metadata.ISelection;
import eu.mico.platform.persistence.metadata.ITarget;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.SPECIFIC_RESOURCE_OA)
public class TargetImpl extends ModelPersistenceBodyImpl implements ITarget {
    
    @Iri(Ontology.HAS_SELECTOR_OA)
    private ISelection selection = null;
    
    @Iri(Ontology.HAS_SOURCE_OA)
    private String source;

    @Override
    public ISelection getSelection() {
        return selection;
    }

    @Override
    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }
}
