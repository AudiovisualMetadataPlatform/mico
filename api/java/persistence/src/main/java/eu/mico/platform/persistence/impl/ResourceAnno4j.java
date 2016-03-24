package eu.mico.platform.persistence.impl;

import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.ResourceMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Resource;

public abstract class ResourceAnno4j implements Resource {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final ResourceMMM resourceMMM;
    protected final PersistenceService persistenceService;

    protected ResourceAnno4j(ResourceMMM resourceMMM, PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.resourceMMM = resourceMMM;
        
    }
    
    @Override
    public final URI getURI() {
        return (URI)resourceMMM.getResource();
    }

    @Override
    public final ResourceMMM getRDFObject() {
        return resourceMMM;
    }

    @Override
    public final String getSyntacticalType() {
        return resourceMMM.getSyntacticalType();
    }

    @Override
    public final void setSyntacticalType(String syntacticalType) throws RepositoryException {
        resourceMMM.setSyntacticalType(syntacticalType);
    }

    @Override
    public final String getSemanticType() {
        return resourceMMM.getSemanticType();
    }

    @Override
    public final void setSemanticType(String semanticType) throws RepositoryException {
        resourceMMM.setSemanticType(semanticType);
    }

    @Override
    public final Asset getAsset() throws RepositoryException {
        if (resourceMMM.getAsset() == null) {
            AssetMMM assetMMM = createObject(AssetMMM.class);
            StringBuilder location = new StringBuilder()
                    .append(persistenceService.getStoragePrefix())
                    .append(this.getURI().getLocalName())
                    .append("/")
                    .append(new URIImpl(assetMMM.getResourceAsString()).getLocalName());
            assetMMM.setLocation(location.toString());

            resourceMMM.setAsset(assetMMM);

            log.trace("No Asset available for Resource {} - Created new Asset with id {} and location {}", 
                    this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
        }
        return new AssetAnno4j(this.resourceMMM.getAsset(), this.persistenceService.getStorage());
    }

    @Override
    public final boolean hasAsset() throws RepositoryException {
        return resourceMMM.getAsset() != null;
    }
    
    protected <T extends RDFObject> T createObject(Class<T> clazz){
        return resourceMMM.getObjectConnection().getObjectFactory().createObject(IDGenerator.BLANK_RESOURCE, clazz);
    }

}
