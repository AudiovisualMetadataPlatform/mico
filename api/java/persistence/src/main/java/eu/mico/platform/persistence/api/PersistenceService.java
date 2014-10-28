/**
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
package eu.mico.platform.persistence.api;

import eu.mico.platform.persistence.model.ContentItem;
import eu.mico.platform.persistence.model.Metadata;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 * A service for creating, retrieving and deleting content items in the MICO platform.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface PersistenceService {


    /**
     * Get a handle on the overall metadata storage of the persistence service. This can e.g. be used for querying
     * about existing content items.
     *
     * @return
     */
    public Metadata getMetadata() throws RepositoryException;

    /**
     * Create a new content item with a random URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    public ContentItem createContentItem() throws RepositoryException;

    /**
     * Create a new content item with the given URI and return it. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the newly created ContentItem
     */
    public ContentItem createContentItem(URI id) throws RepositoryException;


    /**
     * Return the content item with the given URI if it exists. The content item should be suitable for reading and
     * updating and write all updates to the underlying low-level persistence layer.
     *
     * @return a handle to the ContentItem with the given URI, or null if it does not exist
     */
    public ContentItem getContentItem(URI id) throws RepositoryException;

    /**
     * Delete the content item with the given URI. If the content item does not exist, do nothing.
     */
    public void deleteContentItem(URI id) throws RepositoryException;

    /**
     * Return an iterator over all currently available content items.
     *
     * @return iterable
     */
    public Iterable<ContentItem> getContentItems() throws RepositoryException;

}
