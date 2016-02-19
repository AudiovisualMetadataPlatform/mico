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
package eu.mico.platform.event.api.spring;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Resource;

import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

/**
 * Proxy interface to be implemented by MICO platform analyser which use the spring annotation API.
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
public interface Analyser {

    /**
     * The callback which is called whenever a new event for this service has been received.
     * @param analysisResponse  a response object that can be used to send back notifications about new objects to the broker
     * @param item the content item to analyse
     * @param uri the URI of the object to analyse in the content item (a content part or a metadata URI)
     */
	public void call(AnalysisResponse resp, Item item, List<Resource> resourceList, Map<String, String> params);
}