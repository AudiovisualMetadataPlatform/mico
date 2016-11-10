/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    
    public ItemAnno4j(ItemMMM itemMMM, PersistenceService persistenceService) {
        super(itemMMM, persistenceService);
        this.itemMMM = itemMMM;
    }

    @Override
    public Part createPart(URI extractorID) throws RepositoryException {
        PartMMM partMMM = createObject(PartMMM.class);
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        partMMM.setSerializedAt(dateTime);

        Agent agent = createObject(extractorID, Agent.class);
        agent.setName(String.valueOf(extractorID));
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
        List<PartAnno4j> partsAnno4j = new ArrayList<>();
        Set<PartMMM> partsMMM = itemMMM.getParts();
        for (PartMMM partMMM : partsMMM) {
            partsAnno4j.add(new PartAnno4j(partMMM, this, persistenceService));
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
 
}