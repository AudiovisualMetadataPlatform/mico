package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.Transaction;
import eu.mico.platform.anno4j.model.ItemMMM;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.ResourceMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Resource;

public abstract class ResourceAnno4j implements Resource {

    protected final Anno4j anno4j;
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final ResourceMMM resourceMMM;
    protected final PersistenceService persistenceService;

    protected ResourceAnno4j(ResourceMMM resourceMMM, PersistenceService persistenceService, Anno4j anno4j) {
        this.persistenceService = persistenceService;
        this.resourceMMM = resourceMMM;
        this.anno4j = anno4j;
    }
    
    @Override
    public final URI getURI() {
        return (URI)resourceMMM.getResource();
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

            AssetMMM assetMMM;
            Transaction transaction = null;
            boolean error = false;
            try {
                transaction = anno4j.createTransaction();
                transaction.begin();

                if(this instanceof ItemAnno4j) {
                    transaction.setAllContexts(this.getURI());
                } else {
                    transaction.setAllContexts(((PartAnno4j) this).getItem().getURI());
                }

                assetMMM = transaction.createObject(AssetMMM.class);

            } catch (RepositoryException | RuntimeException e ) {
                error = true;
                throw e;
            } catch (IllegalAccessException | InstantiationException e) {
                error = true;
                throw new IllegalStateException(e);
            } finally {
                if(transaction != null){
                    if(error){
                        transaction.rollback(); //rollback any triples created during this method
                        transaction.close(); //in case we have not succeeded we can close the connection
                    } else {
                        transaction.commit(); //commit the item before returning
                    }
                } //failed to open connection
            }

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
}
