package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Agent;
import eu.mico.platform.anno4j.model.ItemMMM;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemAnno4j implements Item {

    private static Logger log = LoggerFactory.getLogger(ItemAnno4j.class);

    private final PersistenceService persistenceService;
    private final ItemMMM itemMMM;

    public ItemAnno4j(ItemMMM itemMMM, PersistenceService persistenceService) {
        this.itemMMM = itemMMM;
        this.persistenceService = persistenceService;
    }

    @Override
    public Part createPart(URI extractorID) throws RepositoryException {
        try {
            PartMMM partMMM = persistenceService.getAnno4j().createObject(PartMMM.class);
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            partMMM.setSerializedAt(dateTime);

            Agent agent = this.persistenceService.getAnno4j().createObject(Agent.class);
            agent.setResource(extractorID);
            partMMM.setSerializedBy(agent);

            this.persistenceService.getAnno4j().persist(partMMM, this.getURI());
            this.itemMMM.addPartMMM(partMMM);

            log.info("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);

            return new PartAnno4j(partMMM, this, persistenceService);
        } catch (IllegalAccessException e) {
            throw new RepositoryException("Illegal access", e);
        } catch (InstantiationException e) {
            throw new RepositoryException("Couldn´t instantiate PartMMM class", e);
        }
    }

    @Override
    public Part getPart(URI uri) throws RepositoryException {
        PartMMM partMMM = persistenceService.getAnno4j().findByID(PartMMM.class, uri);
        return new PartAnno4j(partMMM, this, persistenceService);
    }

    @Override
    public Iterable<? extends Part> getParts() throws RepositoryException {
        ArrayList<PartAnno4j> partsAnno4j = new ArrayList<>();

        List<PartMMM> partsMMM = persistenceService.getAnno4j().findAll(PartMMM.class, this.getURI());

        for (PartMMM partMMM : partsMMM) {
            partsAnno4j.add(new PartAnno4j(partMMM, this, persistenceService));
        }

        return partsAnno4j;
    }

    @Override
    public URI getURI() {
        return new URIImpl(itemMMM.getResourceAsString());
    }

    @Override
    public String getSyntacticalType() {
        return itemMMM.getSyntacticalType();
    }

    @Override
    public void setSyntacticalType(String syntacticalType) throws RepositoryException {
        itemMMM.setSyntacticalType(syntacticalType);
    }

    @Override
    public String getSemanticType() {
        return itemMMM.getSemanticType();
    }

    @Override
    public void setSemanticType(String semanticType) throws RepositoryException {
        itemMMM.setSemanticType(semanticType);
    }

    @Override
    public Asset getAsset() throws RepositoryException {
        if (this.itemMMM.getAsset() == null) {
            try {
                Anno4j anno4j = this.persistenceService.getAnno4j();
                AssetMMM assetMMM = anno4j.createObject(AssetMMM.class);
                assetMMM.setLocation(this.getURI().getLocalName() + "/" + new URIImpl(assetMMM.getResourceAsString()).getLocalName());
                anno4j.persist(assetMMM, this.getURI());
                this.itemMMM.setAsset(assetMMM);

                log.info("No Asset available for Item {} - Created new Asset with id {} and location {}", this.getURI(), assetMMM.getResourceAsString(), assetMMM.getLocation());
            } catch (IllegalAccessException e) {
                throw new RepositoryException("Illegal access", e);
            } catch (InstantiationException e) {
                throw new RepositoryException("Couldn´t instantiate AssetMMM", e);
            }
        }

        return new AssetAnno4j(this.itemMMM.getAsset(), this.persistenceService);
    }

    @Override
    public String getSerializedAt() {
        return this.itemMMM.getSerializedAt();
    }
}