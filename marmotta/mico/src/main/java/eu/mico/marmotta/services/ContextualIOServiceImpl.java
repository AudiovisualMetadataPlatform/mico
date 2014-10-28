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

import com.google.common.base.Preconditions;
import eu.mico.marmotta.api.ContextualConnectionService;
import eu.mico.marmotta.api.ContextualIOService;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ContextualIOServiceImpl implements ContextualIOService {

    private static Logger log = LoggerFactory.getLogger(ContextualIOServiceImpl.class);

    @Inject
    private SesameService sesameService;

    @Inject
    private ContextualConnectionService connectionService;

    @Inject
    private ConfigurationService configurationService;

    /**
     * Bulk import data from an input stream into the given context of the triplestore.
     *
     * @param stream
     * @param format
     * @param context
     */
    @Override
    public void importData(InputStream stream, RDFFormat format, URI context) throws RDFParseException, IOException, RDFHandlerException, RepositoryException {
        log.info("bulk import of context {}", context);
        long start = System.currentTimeMillis();

        RepositoryConnection connection = connectionService.getContextualConnection(context);
        try {
            connection.begin();
            connection.add(stream, context.stringValue(), format, context);
            connection.commit();
        } catch (RDFParseException | IOException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }

        log.info("bulk import took {}ms", System.currentTimeMillis() - start);
    }


    /**
     * Bulk export data from a context in the triplestore to the given output stream.
     *
     * @param stream
     * @param format
     * @param context
     */
    @Override
    public void exportData(OutputStream stream, RDFFormat format, URI context) throws RepositoryException, RDFHandlerException {
        Preconditions.checkNotNull(context, "context must not be null");

        log.info("bulk export of context {}", context);
        RDFWriter handler = Rio.createWriter(format, stream);
        RepositoryConnection connection = sesameService.getConnection();
        try {
            connection.exportStatements(null, null, null, true, handler, context);
        } finally {
            connection.commit();
            connection.close();
        }

    }
}
