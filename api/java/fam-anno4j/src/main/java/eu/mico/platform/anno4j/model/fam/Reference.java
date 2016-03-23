package eu.mico.platform.anno4j.model.fam;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

public class Reference implements RDFObject {

    private Resource resource;
    
    public Reference(String uri) {
        resource = new URIImpl(uri);
    }
    
    public Reference(Resource resource) {
        this.resource = resource;
    }

    @Override
    public ObjectConnection getObjectConnection() {
        return null;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

}
