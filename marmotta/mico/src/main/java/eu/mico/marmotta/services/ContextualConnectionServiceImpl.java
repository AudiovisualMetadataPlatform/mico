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
package eu.mico.marmotta.services;


import eu.mico.marmotta.api.ContextualConnectionService;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.services.sesame.ResourceSubjectMetadata;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.contextaware.ContextAwareConnection;
import org.openrdf.repository.event.InterceptingRepositoryConnection;
import org.openrdf.repository.event.base.InterceptingRepositoryConnectionWrapper;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Serves connections to the Sesame Service where the context is always fixed, i.e. all read access will
 * only return triples from the given context and all updates will only insert into the given context,
 * regardless what context is given in the update.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ContextualConnectionServiceImpl implements ContextualConnectionService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    /**
     * Return a connection to the Sesame Service that is restricted by its context.
     *
     *
     * @param contexts
     * @return
     */
    @Override
    public RepositoryConnection getContextualConnection(URI... contexts) throws RepositoryException {
        log.debug("opening contextual connection with {}", Arrays.toString(contexts));

        ContextAwareConnection conn = new ContextAwareConnection(sesameService.getRepository(), sesameService.getConnection());
        conn.setInsertContext(contexts[0]);
        conn.setRemoveContexts(contexts[0]);
        conn.setReadContexts(contexts);

        return conn;
    }

    @Override
    public InterceptingRepositoryConnection getInterceptingContextualConnection(URI subject, URI context) throws RepositoryException {
        log.debug("opening contextual intercepting connection with {}", context);
        final InterceptingRepositoryConnectionWrapper interceptingConnection = new InterceptingRepositoryConnectionWrapper(sesameService.getRepository(), getContextualConnection(context));
        interceptingConnection.addRepositoryConnectionInterceptor(new ResourceSubjectMetadata(subject));
        return interceptingConnection;
    }

}
