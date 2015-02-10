package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.metadata.ISelection;
import eu.mico.platform.persistence.metadata.ITarget;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;
import org.openrdf.model.URI;

@Iri(Ontology.SPECIFIC_RESOURCE_OA)
public class TargetImpl extends ModelPersistenceImpl implements ITarget {
    
    @Iri(Ontology.HAS_SELECTOR_OA)
    private ISelection selection = null;
    
    @Iri(Ontology.HAS_SOURCE_OA)
    private URI source;

    @Override
    public ISelection getSelection() {
        return selection;
    }

    @Override
    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    @Override
    public void setSource(URI source) {
        this.source = source;
    }

    @Override
    public URI getSource() {
        return source;
    }
}
