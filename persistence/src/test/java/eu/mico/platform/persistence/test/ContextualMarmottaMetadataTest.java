package eu.mico.platform.persistence.test;

import eu.mico.platform.persistence.impl.ContextualMarmottaMetadata;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ContextualMarmottaMetadataTest extends BaseMarmottaTest {



    @Test
    public void testUpdate() throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        ContextualMarmottaMetadata m = new ContextualMarmottaMetadata(baseUrl, UUID.randomUUID());

        m.update("INSERT DATA { <http://example.org/resource/R1> <http://example.org/property/P1> \"Value 1\" } ");


    }
}
