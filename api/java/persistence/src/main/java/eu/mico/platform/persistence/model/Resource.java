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

import eu.mico.platform.anno4j.model.ResourceMMM;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 * Super type of items and parts
 */
public interface Resource {
    /**
     * Return the identifier (a unique URI) for this item. This URI will be based on the internal UUID of the
     * content item in the platform.
     *
     * @return
     */
    URI getURI();

    ResourceMMM getRDFObject();

    /**
     * the mime type, e.g. "image/jpeg"
     */
    String getSyntacticalType();

    void setSyntacticalType(String syntacticalType) throws RepositoryException;

    String getSemanticType();

    void setSemanticType(String semanticType) throws RepositoryException;

    Asset getAsset() throws RepositoryException;
    Asset getAssetWithLocation( URI location ) throws RepositoryException;

    boolean hasAsset() throws RepositoryException;
}
