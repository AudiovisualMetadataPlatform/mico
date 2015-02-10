package eu.mico.platform.persistence.metadata;

import org.openrdf.model.Resource;
import org.openrdf.repository.object.RDFObject;

/**
 * An implementation is needed to avoid blank nodes when persisting.
 */
public interface IModelPersistence extends RDFObject {

    public void setResource(Resource resource);
}
