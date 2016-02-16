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

import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;

import java.io.IOException;

/**
 * An object passed to the AnalysisService callback method to interact with the message broker using the proper channel.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Marcel Sieland
 */
public interface AnalysisResponse {


    /**
     * Send a message to the broker's callback queue that the given content item and object have been processed.
     * 
     * @param ci     the updated content item
     * @param object the updated object
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendFinish(ContentItem ci, URI object) throws IOException;

    /**
     * Send a message to the broker's callback queue that the given content item and object have been updated. 
     *
     * @param ci        the updated content item
     * @param object    the updated object
     * @param progress  the progress value (0..100)
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendProgress(ContentItem ci, URI object, float progress) throws IOException;


    /**
     * Send a message to the broker's callback queue that the given content item and object could not be processed. 
     *
     * @param ci     the processed content item
     * @param object the processed object
     * @param code   one of the defined error codes, could be used by broker to handle the error
     * @param msg    the error message
     * @param desc   further information about the error
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendError(ContentItem ci, URI object, ErrorCodes code, String msg, String desc) throws IOException;

    /**
     * Send a message to the broker's callback queue that a new content part was added. 
     *
     * @param ci           the processed content item
     * @param object       the new object / part
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendNew(ContentItem ci, URI object) throws IOException;

}
