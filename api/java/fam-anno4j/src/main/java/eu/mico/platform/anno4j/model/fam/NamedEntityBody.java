package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;

import org.openrdf.annotations.Iri;

import org.openrdf.repository.object.LangString;
import org.openrdf.repository.object.RDFObject;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.ENTITY_MENTION_ANNOTATION)
public interface NamedEntityBody extends FAMBody {

    
    @Iri(FAM.ENTITY_MENTION)
    public void setMention(LangString mention);
    
    @Iri(FAM.ENTITY_MENTION)
    public LangString getMention();
    
    /**
     * Sets the types. 
     * @param types the types or <code>null</code> to remove all current types
     */
    @Iri(FAM.ENTITY_TYPE)
    public void setTypes(Collection<RDFObject> types);

    /**
     * Getter for the types.
     * @return the set of types
     */
    @Iri(FAM.ENTITY_TYPE)
    public Collection<RDFObject> getTypes();
    
    public void addType(String type);
    
    public void addType(RDFObject type);
    
}
