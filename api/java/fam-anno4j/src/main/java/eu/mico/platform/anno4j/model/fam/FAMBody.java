package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;

import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.model.Selector;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.ANNOTATION_BODY)
public interface FAMBody extends BodyMMM {

    @Iri(FAM.CONFIDENCE)
    void setConfidence(Double confidence);
    
    @Iri(FAM.CONFIDENCE)
    Double getConfidence();
    
    @Iri(FAM.EXTRACTED_FROM)
    public void setContent(RDFObject content);

    void setContentURI(Resource content);

    void setContentURI(String uri);
    
    @Iri(FAM.EXTRACTED_FROM)
    public RDFObject getContent();
    
    public void addSelector(Selector selector);
    
    @Iri(FAM.SELECTOR)
    public void setSelectors(Collection<Selector> selectors);
    
    /**
     * Unmodifiable collection of the selectors for this Body
     * @return
     */
    @Iri(FAM.SELECTOR)
    public Collection<Selector> getSelectors();


}
