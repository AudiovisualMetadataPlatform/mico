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
package eu.mico.platform.event.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

/**
 * Interface to be implemented by services. Consists of some informational methods as well as a callback which is called
 * whenever a new event for this service has been received.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface AnalysisService {

    /**
     * Return a unique ID (URI) that identifies this service and its functionality.
     *
     * @return a unique ID identifying this service globally
     */
    URI getServiceID();


    /**
     * Return the type of output produced by this service as symbolic identifier. In the first version of the API, this
     * is simply an arbitrary string (e.g. MIME type)
     *
     * @return a symbolic identifier representing the output type of this service
     */
    String getProvides();


    /**
     * Return the type of input required by this service as symbolic identifier. In the first version of the API, this
     * is simply an arbitrary string (e.g. MIME type)
     *
     * @return  a symbolic identifier representing the input type of this service
     */
    String getRequires();


    /**
     * Return the queue name that should be used by the messaging infrastructure for this service. If explicitly set,
     * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
     * balancing.
     *
     * The implementation can return null, in which case the event API will choose a random queue name.
     *
     * @return a string identifying the queue name this service wants to use
     */
    String getQueueName();


    /**
     * Call this service for the given content item and object. This method is called by the event manager whenever
     * a new analysis event for this service has been received in its queue. The API takes care of automatically
     * resolving the content item in the persistence service.
     *
     * @param resp   a response object that can be used to send back notifications about new objects to the broker
     * @throws AnalysisException if the AnalysisService fails for some reason (e.g. the media file was corrupted;
     * processed annotations in the Item where incomplete; ...). Also IO exceptions while accessing
     * external resource should be wrapped as {@link AnalysisException}s.
     * @throws RepositoryException if writing RDF data to the Repository failed for some reason
     * @throws IOException if sending events via the AnalysisResponse failed for some reason
     * @throws RuntimeException the caller takes also care of runtime exceptions 
     */
    void call(AnalysisResponse resp, Item item,  List<Resource> resourceList,  Map<String, String> params) throws AnalysisException, IOException, RepositoryException;

}
