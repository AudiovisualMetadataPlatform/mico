package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;

import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.model.impl.multiplicity.Choice;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.TOPIC_CLASSIFICATION_ANNOTATION)
public interface TopicClassificationBody extends Choice, FAMBody {

    @Iri(FAM.CLASSIFICATION_SCHEME)
    RDFObject getClassificationScheme();
    
    @Iri(FAM.CLASSIFICATION_SCHEME)
    void setClassificationScheme(RDFObject scheme);

    void setClassificationScheme(Resource scheme);
    
    void setClassificationSchemeUri(String schemeUri);
    
    Collection<TopicBody> getTopics();
    
    void addTopic(TopicBody topic);

    
}
