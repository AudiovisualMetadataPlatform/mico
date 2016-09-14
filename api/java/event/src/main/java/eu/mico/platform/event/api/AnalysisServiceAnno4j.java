/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform.event.api;

import com.github.anno4j.Transaction;
import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;
import org.openrdf.repository.RepositoryException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface to be implemented by services. Consists of some informational methods as well as a callback which is called
 * whenever a new event for this service has been received.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface AnalysisServiceAnno4j extends AnalysisServiceBase {


    /**
     * Call this service for the given content item and object. This method is called by the event manager whenever
     * a new analysis event for this service has been received in its queue. The API takes care of automatically
     * resolving the content item in the persistence service.
     *
     * @param resp         a response object that can be used to send back notifications about new objects to the broker
     * @param item         the item associated with the respective analysis event needed for context and modification
     * @param resourceList actual input objects for the analysis processing (Can include the item itself or attached parts of the item)
     * @param params       Arbitrary parameters need for the analysis configuration
     * @param anno4j       a transaction object of Anno4j
     * @throws AnalysisException   if the AnalysisService fails for some reason (e.g. the media file was corrupted;
     *                             processed annotations in the Item where incomplete; ...). Also IO exceptions while accessing
     *                             external resource should be wrapped as {@link AnalysisException}s.
     * @throws RepositoryException if writing RDF data to the Repository failed for some reason
     * @throws IOException         if sending events via the AnalysisResponse failed for some reason
     * @throws RuntimeException    the caller takes also care of runtime exceptions
     */
    void call(AnalysisResponse resp, Item item, List<Resource> resourceList, Map<String, String> params, Transaction anno4j) throws AnalysisException, IOException, RepositoryException;

}
