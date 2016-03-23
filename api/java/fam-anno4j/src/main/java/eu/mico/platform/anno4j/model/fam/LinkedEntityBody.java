package eu.mico.platform.anno4j.model.fam;

import static eu.mico.platform.anno4j.model.namespaces.FAM.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.openrdf.annotations.Iri;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
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
