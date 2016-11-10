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

package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Interface for the RDF nodes that serve as parameter of a given Mode of an Extractor.
 */
@Iri(MMM.PARAM)
public interface ParamMMM {

    @Iri(RDF.VALUE)
    void setValue(String value);

    @Iri(RDF.VALUE)
    String getValue();

    @Iri(MMM.HAS_NAME)
    void setName(String name);

    @Iri(MMM.HAS_NAME)
    String getName();
}
