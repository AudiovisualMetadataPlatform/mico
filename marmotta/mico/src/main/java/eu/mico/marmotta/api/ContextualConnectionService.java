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
package eu.mico.marmotta.api;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.event.InterceptingRepositoryConnection;

/**
 * Serves connections to the Sesame Service where the context is always fixed, i.e. all read access will
 * only return triples from the given context and all updates will only insert into the given context,
 * regardless what context is given in the update.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface ContextualConnectionService {

    public static final String CONTEXT_ID_PATTERN = "[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*";

    public static final String CONTEXT_IDS_PATTERN = CONTEXT_ID_PATTERN + "(," + CONTEXT_ID_PATTERN + ")*";

    /**
     * Return a connection to the Sesame Service that is restricted by its context.
     *
     * @param contexts
     * @return
     */
    RepositoryConnection getContextualConnection(URI... contexts) throws RepositoryException;

    InterceptingRepositoryConnection getInterceptingContextualConnection(URI subject, URI context) throws RepositoryException;

}
