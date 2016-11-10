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

import eu.mico.platform.persistence.util.URITools;
import org.openrdf.idGenerator.IDGenerator;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.util.Set;
import java.util.UUID;

public class IDGeneratorAnno4j implements IDGenerator {

    private String sparqlBaseURI;

    public IDGeneratorAnno4j(String sparqlBaseURI) {
        this.sparqlBaseURI = sparqlBaseURI;
    }

    @Override
    public Resource generateID(Set<URI> types) {
        return new URIImpl(URITools.normalizeURI(sparqlBaseURI + "/" + UUID.randomUUID()));
    }
}
