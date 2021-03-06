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
package eu.mico.platform.persistence.model;

import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.ResourceObject;
import org.openrdf.repository.object.RDFObject;

import java.util.Set;

/**
 * Binary content, offered through inputstreams and outputstreams. Can be used for representing any kind of non-RDF
 * content.
 *
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 * @author Horst Stadler
 */
public interface Part extends Resource {

    /**
     * Return the parent content item.
     * @return
     */
    Item getItem();

    Body getBody();

    void setBody(Body body);

    Set<Target> getTargets();

    void setTargets(Set<Target> targets);

    void addTarget(Target target);

    Set<Resource> getInputs();

    void setInputs(Set<Resource> inputs);

    void addInput(Resource input);

    String getSerializedAt();

    Agent getSerializedBy();
}
