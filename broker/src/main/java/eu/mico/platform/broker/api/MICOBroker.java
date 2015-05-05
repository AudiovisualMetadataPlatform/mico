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
package eu.mico.platform.broker.api;

import eu.mico.platform.broker.model.ContentItemState;
import eu.mico.platform.broker.model.ServiceGraph;
import eu.mico.platform.persistence.api.PersistenceService;

import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface MICOBroker {
    ServiceGraph getDependencies();

    Map<String, ContentItemState> getStates();

    PersistenceService getPersistenceService();
}
