/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.persistence.api;

import com.github.anno4j.Anno4j;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.storage.api.StorageService;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 * A service for creating, retrieving and deleting content items in the MICO platform.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface PersistenceService {

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created Item
     */
    Item createItem() throws RepositoryException;

    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the Item with the given URI, or null if it does not exist
     */
    Item getItem(URI id) throws RepositoryException;

    /**
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    void deleteItem(URI id) throws RepositoryException;

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    Iterable<? extends Item> getItems() throws RepositoryException;

    StorageService getStorage();

    Anno4j getAnno4j();
}
