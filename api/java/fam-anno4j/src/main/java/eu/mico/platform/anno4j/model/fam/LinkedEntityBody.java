package eu.mico.platform.anno4j.model.fam;

import org.openrdf.annotations.Iri;
import org.openrdf.repository.object.LangString;
import org.openrdf.repository.object.RDFObject;

import eu.mico.platform.anno4j.model.namespaces.FAM;


@Iri(FAM.LINKED_ENTITY_ANNOTATION)
public interface LinkedEntityBody extends NamedEntityBody {

    @Iri(FAM.ENTITY_REFERENCE)
    public void setEntity(RDFObject entity);
    
    @Iri(FAM.ENTITY_REFERENCE)
    public RDFObject getEntity();
    
    @Iri(FAM.ENTITY_LABEL)
    public void setLabel(LangString label);
    
    @Iri(FAM.ENTITY_LABEL)
    public LangString getLabel();
    
}
