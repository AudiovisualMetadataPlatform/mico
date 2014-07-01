package eu.mico.platform.persistence.model;

import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryConnection;

/**
 * ContentPart Metadata, in RDF format. Offers structured access through the Sesame Repository API (and later commons-rdf
 * once it becomes available)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface Metadata {


    /**
     * Return a repository connection for accessing the RDF data that is suitable for both querying and updating. Changes
     * will be written back through the persistence API.
     * @return
     */
    public RepositoryConnection getConnection();
}
