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
