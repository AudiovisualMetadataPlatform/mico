package eu.mico.platform.anno4j.model.fam;

import org.openrdf.model.Resource;

import com.github.anno4j.annotations.Partial;

@Partial
public abstract class TopicBodySupport extends FAMBodySupport implements TopicBody {


    @Override
    public void setTopicUri(String reference) {
        setTopic(reference == null ? null : new Reference(reference));
    }
    
    @Override
    public void setTopic(Resource reference) {
        setTopic(reference == null ? null : new Reference(reference));
    }

}
