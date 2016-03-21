package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;
import java.util.HashSet;

import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.annotations.Partial;

@Partial
public abstract class NamedEntityBodySupport extends FAMBodySupport
        implements NamedEntityBody {

    @Override
    public void addType(String typeUri) {
        if(typeUri == null){
            return;
        }
        addType(new Reference(typeUri));
    }

    @Override
    public void addType(RDFObject type) {
        if(type == null){
            return;
        }
        Collection<RDFObject> types = getTypes();
        if(types == null){
            types = new HashSet<>();
            setTypes(types);
        }
        types.add(type);

    }

}
