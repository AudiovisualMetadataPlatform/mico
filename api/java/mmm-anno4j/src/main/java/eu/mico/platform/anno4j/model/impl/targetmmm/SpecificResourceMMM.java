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

package eu.mico.platform.anno4j.model.impl.targetmmm;

import com.github.anno4j.model.Selector;
import com.github.anno4j.model.impl.targets.SpecificResource;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;
import org.openrdf.repository.object.RDFObject;

/**
 * Class represents a specific resource in the MICO context.
 */
@Iri(MMM.SPECIFIC_RESOURCE)
public interface SpecificResourceMMM extends SpecificResource {

    /**
     * Gets Refers to http:www.w3.orgnsoa#hasSelector
     * The relationship between a oa:SpecificResource and a oa:Selector.
     * There MUST be exactly 0 or 1 oa:hasSelector relationship associated with a Specific Resource..
     *
     * Refers to http://www.w3.org/ns/oa#hasSelector
     * The relationship between a oa:SpecificResource and a oa:Selector.
     * There MUST be exactly 0 or 1 oa:hasSelector relationship associated with a Specific Resource.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#hasSelector
     * The relationship between a oa:SpecificResource and a oa:Selector.
     * There MUST be exactly 0 or 1 oa:hasSelector relationship associated with a Specific Resource..
     */
    @Iri(MMM.HAS_SELECTOR)
    Selector getSelector();

    /**
     * Sets new Refers to http:www.w3.orgnsoa#hasSelector
     * The relationship between a oa:SpecificResource and a oa:Selector.
     * There MUST be exactly 0 or 1 oa:hasSelector relationship associated with a Specific Resource..
     *
     * Refers to http://www.w3.org/ns/oa#hasSelector
     * The relationship between a oa:SpecificResource and a oa:Selector.
     * There MUST be exactly 0 or 1 oa:hasSelector relationship associated with a Specific Resource.
     *
     * @param selector New value of Refers to http:www.w3.orgnsoa#hasSelector
     *                 The relationship between a oa:SpecificResource and a oa:Selector.
     *                 There MUST be exactly 0 or 1 oa:hasSelector relationship associated with a Specific Resource..
     */
    @Iri(MMM.HAS_SELECTOR)
    void setSelector(Selector selector);

    /**
     * Gets Refers to http:www.w3.orgnsoa#hasSource
     * The relationship between a Specific Resource and the resource that it is a more specific representation of.
     * There must be exactly 1 oa:hasSource relationship associated with a Specific Resource..
     *
     * Refers to http://www.w3.org/ns/oa#hasSource
     * The relationship between a Specific Resource and the resource that it is a more specific representation of.
     * There must be exactly 1 oa:hasSource relationship associated with a Specific Resource.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#hasSource
     * The relationship between a Specific Resource and the resource that it is a more specific representation of.
     * There must be exactly 1 oa:hasSource relationship associated with a Specific Resource..
     */
    @Iri(MMM.HAS_SOURCE)
    RDFObject getSource();

    /**
     * Sets new Refers to http:www.w3.orgnsoa#hasSource
     * The relationship between a Specific Resource and the resource that it is a more specific representation of.
     * There must be exactly 1 oa:hasSource relationship associated with a Specific Resource.
     *
     * Refers to http://www.w3.org/ns/oa#hasSource
     * The relationship between a Specific Resource and the resource that it is a more specific representation of.
     * There must be exactly 1 oa:hasSource relationship associated with a Specific Resource.
     *
     * @param source New value of Refers to http:www.w3.orgnsoa#hasSource
     *               The relationship between a Specific Resource and the resource that it is a more specific representation of.
     *               There must be exactly 1 oa:hasSource relationship associated with a Specific Resource..
     */
    @Iri(MMM.HAS_SOURCE)
    void setSource(RDFObject source);
}
