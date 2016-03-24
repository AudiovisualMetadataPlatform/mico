package eu.mico.platform.persistence.impl;

import com.github.anno4j.model.Agent;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
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

public class ItemAnno4j extends ResourceAnno4j implements Item {

    private static Logger log = LoggerFactory.getLogger(ItemAnno4j.class);

    private final ItemMMM itemMMM;
    
    public ItemAnno4j(ItemMMM itemMMM, PersistenceService persistenceService) {
        super(itemMMM, persistenceService);
        this.itemMMM = itemMMM;
    }

    @Override
    public Part createPart(URI extractorID) throws RepositoryException {
        PartMMM partMMM = createObject(PartMMM.class);
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        partMMM.setSerializedAt(dateTime);

        Agent agent = createObject(Agent.class);
        agent.setResource(extractorID);
        partMMM.setSerializedBy(agent);

        this.itemMMM.addPart(partMMM);

        log.trace("Created Part with id {} in the context graph {} - Creator {}", partMMM.getResourceAsString(), this.getURI(), extractorID);

        return new PartAnno4j(partMMM, this, persistenceService);
    }

    
    @Override
    public Part getPart(URI uri) throws RepositoryException {
        try {
            PartMMM partMMM = itemMMM.getObjectConnection().getObject(PartMMM.class, uri);
            return new PartAnno4j(partMMM, this, persistenceService);
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Iterable<? extends Part> getParts() throws RepositoryException {

        Result<PartMMM> partsMMM = null;
        try {
            ArrayList<PartAnno4j> partsAnno4j = new ArrayList<>();
            partsMMM = itemMMM.getObjectConnection().getObjects(PartMMM.class);
            while(partsMMM.hasNext()){
                partsAnno4j.add(new PartAnno4j(partsMMM.next(), this, persistenceService));
            }
            return partsAnno4j;
        } catch(QueryEvaluationException e){
            throw new RepositoryException(e);
        } finally {
            if(partsMMM != null){
                try {
                    partsMMM.close();
                } catch (QueryEvaluationException e) {/*ignore*/}
            }
        }
    }

    @Override
    public String getSerializedAt() {
        return this.itemMMM.getSerializedAt();
    }

    @Override
    public ObjectConnection getObjectConnection(){
        return itemMMM.getObjectConnection();
    }
 
}