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

package eu.mico.platform.anno4j.model;

import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Body;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.namespaces.OADM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Precedes;
import org.openrdf.annotations.Sparql;

import java.util.Set;

/**
 * Class represents a Part. A Part resembles an extractor step and consecutively an (intermediary)
 * result of an Item and its extraction chain.
 */
@Iri(MMM.PART)
public interface PartMMM extends Annotation, ResourceMMM {

    /**
     * Gets http:www.w3.org/ns/oa#hasBody relationship.
     *
     * @return Value of http:www.w3.org/ns/oa#hasBody.
     */
    @Iri(MMM.HAS_BODY)
    @Override
    Body getBody();

    /**
     * Sets http:www.w3.org/ns/oa#hasBody.
     *
     * @param body New value of http:www.w3.org/ns/oa#hasBody.
     */
    @Iri(MMM.HAS_BODY)
    @Override
    void setBody(Body body);

    /**
     * Gets http:www.w3.org/ns/oa#hasTarget relationships.
     *
     * @return Values of http:www.w3.org/ns/oa#hasTarget.
     */
    @Iri(MMM.HAS_TARGET)
    @Override
    Set<Target> getTarget();

    /**
     * Sets http:www.w3.org/ns/oa#hasTarget.
     *
     * @param targets New value of http:www.w3.org/ns/oa#hasTarget.
     */
    @Iri(MMM.HAS_TARGET)
    @Override
    void setTarget(Set<Target> targets);

    /**
     * Gets the objects that were the semantic input for this Part.
     *
     * @return A set of objects that are used as semantic input for creating this Part.
     */
    @Iri(MMM.HAS_INPUT)
    Set<ResourceMMM> getInputs();

    /**
     * Sets the Set of objects that are the semantic input for this Part.
     *
     * @param inputs    The set of objects that form the semantic input for this Part
     */
    @Iri(MMM.HAS_INPUT)
    void setInputs(Set<ResourceMMM> inputs);

    /**
     * Adds a single object to the set of objects, that form the semantic input for this Part.
     *
     * @param input The object that is to be added to the set of objects, that form the semantic input for this part.
     */
    void addInput(ResourceMMM input);

    /**
     * Adds a http:www.w3.org/ns/oa#hasTarget relationship.
     *
     * @param target New http:www.w3.org/ns/oa#hasTarget relationship.
     */
    @Override
    void addTarget(Target target);

    @Sparql("SELECT ?agent WHERE { $this <"+ OADM.SERIALIZED_BY + "> ?agent }")
    @Override
    Agent getSerializedBy();
}
