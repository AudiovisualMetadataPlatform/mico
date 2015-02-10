package eu.mico.platform.persistence.impl;

import eu.mico.platform.persistence.util.PathUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import java.util.UUID;

public abstract class ModelPersistenceImpl implements RDFObject {

    // This unique resource identifier is needed to avoid black nodes
    private Resource resource = new URIImpl(PathUtil.marmottaServerUrl + "/" + UUID.randomUUID());

    @Override
    public ObjectConnection getObjectConnection() {
        // will be implemented by the proxy object
        return null;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Resource getResource() {
        return resource;
    }
}
