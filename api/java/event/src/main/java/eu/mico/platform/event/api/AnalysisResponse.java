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

import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;

import java.io.IOException;

/**
 * An object passed to the AnalysisService callback method to interact with the message broker using the proper channel.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface AnalysisResponse {


    /**
     * Send a message to the broker's callback queue that the given content item and object have been updated. Can be
     * used e,g, to notify the message broker that a new content part has been created or an object or entity has been
     * identified by the analysis service.
     *
     * @param ci     the updated content item
     * @param object the updated object
     */
    public void sendMessage(ContentItem ci, URI object) throws IOException;

}
