package eu.mico.marmotta.api;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A service for bulk-loading and exporting operations
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface ContextualIOService {


    /**
     * Bulk import data from an input stream into the given context of the triplestore.
     *
     * @param stream
     * @param format
     * @param context
     */
    public void importData(InputStream stream, RDFFormat format, URI context) throws RDFParseException, IOException, RDFHandlerException, RepositoryException;


    /**
     * Bulk export data from a context in the triplestore to the given output stream.
     * @param stream
     * @param format
     * @param context
     */
    public void exportData(OutputStream stream, RDFFormat format, URI context) throws RepositoryException, RDFHandlerException;

}
