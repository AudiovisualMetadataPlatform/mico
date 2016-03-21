package eu.mico.platform.anno4j.model.fam;

import java.util.HashSet;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.LangString;

import com.github.anno4j.annotations.Partial;

@Partial
public abstract class TopicBodySupport extends FAMBodySupport implements TopicBody {

    
    @Override
    public void addTopicLabel(LangString name) {
        if(name == null){
            return;
        }
        if(getTopicLabels() == null){
            setTopicLabels(new HashSet<LangString>());
        }
        getTopicLabels().add(name);
    }
    
    @Override
    public void addTopicLabel(Literal label) {
        if(label != null){
            addTopicLabel(new LangString(label.stringValue(), label.getLanguage()));
        }
        
    }

    @Override
    public void setTopicUri(String reference) {
        setTopic(reference == null ? null : new Reference(reference));
    }
    
    @Override
    public void setTopic(Resource reference) {
        setTopic(reference == null ? null : new Reference(reference));
    }

}
