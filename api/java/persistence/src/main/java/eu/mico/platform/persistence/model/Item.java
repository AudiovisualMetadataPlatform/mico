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
package eu.mico.platform.persistence.model;

import com.github.anno4j.Anno4j;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;

/**
 * Representation of a Item. A Item is a collection of ContentParts, e.g. an HTML page together with
 * its embedded images. ContentParts can be either original content or created during analysis. For compatibility
 * with the Linked Data platform, its RDF type is ldp:BasicContainer
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Horst Stadler
 */
public interface Item extends Resource {

    /**
     * Create a new content part with a random URI and return a handle. The handle can then be used for updating the
     * content and metadata of the content part.
     *
     * @param extractorID The id of the extractor which creates the current part
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    Part createPart(URI extractorID) throws RepositoryException;

    /**
     * Return a handle to the ContentPart with the given URI, or null in case the content item does not have this
     * content part.
     *
     * @param uri the URI of the content part to return
     * @return a handle to a ContentPart object that is suitable for reading and updating
     */
    Part getPart(URI uri) throws RepositoryException;

    /**
     * Return an iterator over all content parts contained in this content item.
     *
     * @return an iterable that (lazily) iterates over the content parts
     */
    Iterable<? extends Part> getParts() throws RepositoryException;

    String getSerializedAt();

    Anno4j getContextedAnno4j();
}
