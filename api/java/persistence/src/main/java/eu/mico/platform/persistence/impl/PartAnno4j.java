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

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.ItemMMM;
import eu.mico.platform.anno4j.model.PartMMM;
import eu.mico.platform.anno4j.model.AssetMMM;
import eu.mico.platform.anno4j.model.ResourceMMM;
import eu.mico.platform.persistence.api.PersistenceService;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Resource;

import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.RDFObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class PartAnno4j extends ResourceAnno4j implements Part {

    private static Logger log = LoggerFactory.getLogger(PartAnno4j.class);

    private final Item item;
    private final PartMMM partMMM;

    public PartAnno4j(PartMMM partMMM, Item item, PersistenceService persistenceService) {
        super(partMMM, persistenceService);
        this.partMMM = partMMM;
        this.item = item;
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
    public Set<Target> getTargets() {
        return partMMM.getTarget();
    }

    @Override
    public void setTargets(Set<Target> targets) {
        partMMM.setTarget(targets);
    }

    @Override
    public void addTarget(Target target) {
        partMMM.addTarget(target);
    }

    @Override
    public Set<Resource> getInputs() {
        Set<Resource> resourceSet = new HashSet<>();
        for(ResourceMMM resourceMMM : this.partMMM.getInputs()) {
            if(resourceMMM instanceof ItemMMM) {
                resourceSet.add(new ItemAnno4j((ItemMMM) resourceMMM, this.persistenceService));
            } else {
                resourceSet.add(new PartAnno4j((PartMMM) resourceMMM, this.item, this.persistenceService));
            }
        }
        return resourceSet;
    }

    @Override
    public void setInputs(Set<Resource> inputs) {
        Set<ResourceMMM> resourceMMMSet = new HashSet<>();
        for(Resource resource : inputs) {
            resourceMMMSet.add(resource.getRDFObject());
        }
        this.partMMM.setInputs(resourceMMMSet);
    }

    @Override
    public void addInput(Resource input) {
        this.partMMM.addInput(input.getRDFObject());
    }

    @Override
    public String getSerializedAt() {
        return partMMM.getSerializedAt();
    }

    @Override
    public Agent getSerializedBy() {
        return partMMM.getSerializedBy();
    }
    
    @Override
    public Item getItem() {
        return item;
    }

}
