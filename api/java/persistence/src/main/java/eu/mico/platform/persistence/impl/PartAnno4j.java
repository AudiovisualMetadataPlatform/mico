package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Target;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class PartAnno4j implements Part {

    private static Logger log = LoggerFactory.getLogger(PersistenceServiceAnno4j.class);

    private final PersistenceService persistenceService;
    private final Item item;
    private final PartMMM partMMM;

    public PartAnno4j(PartMMM partMMM, Item item, PersistenceService persistenceService) {
        this.partMMM = partMMM;
        this.item = item;
        this.persistenceService = persistenceService;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public URI getURI() {
        return new URIImpl(partMMM.getResourceAsString());
    }

    @Override
    public String getSyntacticalType() {
        return partMMM.getSyntacticalType();
    }

    @Override
    public void setSyntacticalType(String syntacticalType) throws RepositoryException {
        partMMM.setSyntacticalType(syntacticalType);
    }

    @Override
    public String getSemanticType() {
        return partMMM.getSemanticType();
    }

    @Override
    public void setSemanticType(String semanticType) throws RepositoryException {
        partMMM.setSemanticType(semanticType);
    }

    @Override
    public Body getBody() {
        return partMMM.getBody();
    }

    @Override
    public void setBody(Body body) {
        partMMM.setBody(body);
    }

    @Override
    public Set<Target> getTarget() {
        return partMMM.getTarget();
    }

    @Override
    public void setTarget(Set<Target> targets) {
        partMMM.setTarget(targets);
    }

    @Override
    public void addTarget(Target target) {
        partMMM.addTarget(target);
    }

    @Override
    public String getSerializedAt() {
        return this.partMMM.getSerializedAt();
    }

    @Override
    public Agent getSerializedBy() {
        return this.partMMM.getSerializedBy();
    }

    @Override
    public Asset getAsset() throws RepositoryException {
        if (this.partMMM.getAsset() == null) {
            try {
                Anno4j anno4j = this.persistenceService.getAnno4j();
                AssetMMM assetMMM = anno4j.createObject(AssetMMM.class);
                assetMMM.setLocation(this.item.getURI().getLocalName() + "/" + this.getURI().getLocalName() + "/" + new URIImpl(assetMMM.getResourceAsString()).getLocalName());
                anno4j.persist(assetMMM, this.item.getURI());
                this.partMMM.setAsset(assetMMM);

                log.info("No Asset available for Part {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
            } catch (IllegalAccessException e) {
                throw new RepositoryException("Illegal access", e);
            } catch (InstantiationException e) {
                throw new RepositoryException("Couldn´t instantiate AssetMMM", e);
            }
        }

        return new AssetAnno4j(this.partMMM.getAsset(), this.persistenceService);
    }
}
