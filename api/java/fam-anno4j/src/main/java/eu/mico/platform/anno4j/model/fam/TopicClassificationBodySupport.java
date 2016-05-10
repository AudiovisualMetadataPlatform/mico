package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;

import org.openrdf.model.Resource;

import com.github.anno4j.annotations.Partial;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

@Partial
public abstract class TopicClassificationBodySupport extends FAMBodySupport implements TopicClassificationBody{

    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<TopicBody> getTopics() {
        if(getItems() == null){
            return null;
        }
        return Collections2.filter((Collection)getItems(), Predicates.instanceOf(TopicBody.class));
    }

    @Override
    public void setClassificationScheme(Resource scheme) {
        setClassificationScheme(scheme == null ? null : new Reference(scheme));
    }
    @Override
    public void setClassificationSchemeUri(String schemeUri) {
        setClassificationScheme(schemeUri == null ? null : new Reference(schemeUri));
    }
    
    @Override
    public void addTopic(TopicBody topic) {
        addItem(topic);
    }
}
