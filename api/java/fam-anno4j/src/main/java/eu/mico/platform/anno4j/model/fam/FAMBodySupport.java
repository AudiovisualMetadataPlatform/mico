package eu.mico.platform.anno4j.model.fam;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.Selector;
import com.github.anno4j.model.impl.ResourceObjectSupport;

@Partial
public abstract class FAMBodySupport extends ResourceObjectSupport
        implements FAMBody {

    @Override
    public void addSelector(Selector selector) {
        if(selector == null){
            return;
        }
        Set<Selector> selectors = getSelectors();
        if(selectors == null){
            selectors = new HashSet<>();
            setSelectors(selectors);
        }
        selectors.add(selector);
    }

    @Override
    public void setContentURI(Resource content) {
        setContent(new Reference(content));
    }
    
    @Override
    public void setContentURI(String uri) {
        setContent(new Reference(uri));
    }
        
}
