package eu.mico.platform.persistence.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.Transaction;
import com.github.anno4j.model.Agent;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.ResourceMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;

import org.openrdf.model.URI;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ItemAnno4j extends ResourceAnno4j implements Item {

    private static Logger log = LoggerFactory.getLogger(ItemAnno4j.class);

    private final ItemMMM itemMMM;
    
    public ItemAnno4j(ItemMMM itemMMM, PersistenceService persistenceService, Anno4j anno4j) {
        super(itemMMM, persistenceService, anno4j);
        this.itemMMM = itemMMM;
    }

    @Override
    public Part createPart(URI extractorID) throws RepositoryException {

        Transaction transaction = null;
        boolean error = false;

        PartMMM partMMM;
        try {
            transaction = anno4j.createTransaction();
            transaction.begin();

            transaction.setAllContexts(this.getURI());
            partMMM = transaction.createObject(PartMMM.class);

            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            partMMM.setSerializedAt(dateTime);

            Agent agent = transaction.createObject(Agent.class, extractorID);
            partMMM.setSerializedBy(agent);

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

        this.itemMMM.addPart(partMMM);
        log.trace("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);
        return new PartAnno4j(partMMM, this, persistenceService, anno4j);
    }

    
    @Override
    public Part getPart(URI uri) throws RepositoryException {
        Transaction transaction = anno4j.createTransaction();
        transaction.setAllContexts(this.getURI());

        PartMMM partMMM = transaction.findByID(PartMMM.class, uri);
        if (partMMM != null) {
            return new PartAnno4j(partMMM, this, persistenceService, anno4j);
        } else {
            return null;
        }
    }

    @Override
    public Iterable<? extends Part> getParts() throws RepositoryException {
        List<PartAnno4j> partsAnno4j = new ArrayList<>();
        Set<PartMMM> partsMMM = itemMMM.getParts();

        for (PartMMM partMMM : partsMMM) {
            partsAnno4j.add(new PartAnno4j(partMMM, this, persistenceService, anno4j));
        }

        return partsAnno4j;
    }

    @Override
    public String getSerializedAt() {
        return this.itemMMM.getSerializedAt();
    }

    @Override
    public ObjectConnection getObjectConnection(){
        return itemMMM.getObjectConnection();
    }

    @Override
    public ItemMMM getRDFObject() {
        return itemMMM;
    }
}