package eu.mico.platform.anno4j.model.fam;

import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.object.LangString;
import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.model.impl.ResourceObject;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.TOPIC_ANNOTATION)
public interface TopicBody extends FAMBody {

    /**
     * Getter for the name (<code>fam:topic-label</code>) of the topic 
     * @return the name
     */
    @Iri(FAM.TOPIC_LABEL)
    public LangString getName();
    
    /**
     * Setter for the name (<code>fam:topic-label</code>) of the topic 
     * @param name the name
     */
    @Iri(FAM.TOPIC_LABEL)
    public void setName(LangString name);
    /**
     * Getter for the reference (<code>fam:topic-reference</code>) of the topic
     * @return the reference
     */
    @Iri(FAM.TOPIC_REFERENCE)
    public RDFObject getTopic();
    /**
     * Setter for the reference (<code>fam:topic-reference</code>) of the topic
     * @param reference the reference
     */
    @Iri(FAM.TOPIC_REFERENCE)
    public void setTopic(RDFObject topic);
    
    /**
     * Setter for the topic by parsing a reference
     * @param reference the reference to the topic
     */
    public void setTopicUri(String reference);
    
    /**
     * Setter for the topic by parsing a reference
     * @param reference the reference to the topic
     */
    public void setTopic(Resource reference);
    
}
