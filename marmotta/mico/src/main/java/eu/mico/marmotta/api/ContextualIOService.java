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
