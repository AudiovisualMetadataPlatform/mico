/*
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
import java.io.OutputStream;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Selector;
import com.github.anno4j.model.impl.targets.SpecificResource;

import eu.mico.platform.event.model.AnalysisException;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.persistence.model.Asset;
import eu.mico.platform.persistence.model.Item;
import eu.mico.platform.persistence.model.Part;

/**
 * An object passed to the AnalysisService callback method to interact with the message broker using the proper channel.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @author Marcel Sieland
 */
public interface AnalysisResponse {


    /**
     * Commits any changes to the parsed Item and sends a message to the broker's 
     * callback queue that the parsed Item was processed successfully.
     * <p>
     * After this method completes (without exception) any change to the Item 
     * will result in Exceptions. In case of an Exceptions changes to the Item
     * are still allowed and callers should try to send an suiting Error using 
     * {@link #sendError(Item, ErrorCodes, String, String)}
     * 
     * @param ci     the updated content item
     * @throws RepositoryException on any error while committing the Item
     * @throws IOException if something with the used communication channel is wrong
     */
    void sendFinish(Item ci) throws IOException, RepositoryException;

    /**
     * Send a message to the broker's callback queue that the given content 
     * item and object have been updated.
     * <p>
     * This method does not commit any changes to the item.
     *
     * @param ci        the updated content item
     * @param object    the updated object
     * @param progress  the progress value (0..100)
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendProgress(Item ci, URI object, float progress) throws IOException;


    /**
     * Sends a message to the broker's callback queue that the given content item 
     * and object could not be processed. Also tries to rollback any changes
     * to the Item performed by the AnalysisService.
     * <p>
     * After calling this method no further changes to the Item will be possible.
     * Method calls on the Item or any connected Object will result in Exceptions.
     *
     * @param item   the processed content item
     * @param code   the error code
     * @param msg    the error message
     * @param desc   further information about the error
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendError(Item item, ErrorCodes code, String msg, String desc) throws IOException;
    
    /**
     * Sends a message to the broker's callback queue that the given content item 
     * and object could not be processed. Also tries to rollback any changes
     * to the Item performed by the AnalysisService.
     * <p>
     * After calling this method no further changes to the Item will be possible.
     * Method calls on the Item or any connected Object will result in Exceptions.
     *
     * @param item     the processed content item
     * @param msg    the error message
     * @param t      the original exception to be reported. The 
     *               {@link Throwable#printStackTrace(java.io.PrintWriter) stacktrace}
     *               will be used as description of the error message
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendError(Item item, AnalysisException e) throws IOException;

    /**
     * Sends a message to the broker's callback queue that the given content item 
     * and object could not be processed. Also tries to rollback any changes
     * to the Item performed by the AnalysisService.
     * <p>
     * After calling this method no further changes to the Item will be possible.
     * Method calls on the Item or any connected Object will result in Exceptions.
     *
     * @param item   the processed item
     * @param code   the error code
     * @param msg    the error message
     * @param t      the original exception to be reported. The 
     *               {@link Throwable#printStackTrace(java.io.PrintWriter) stacktrace}
     *               will be used as description of the error message
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendError(Item item, ErrorCodes code, String msg, Throwable t) throws IOException;

    /**
     * Commits the current state of the Item and sends a message to the broker's 
     * callback queue that a new content part was added.
     * <p>
     * As the Broker might react to the new part at any time after calling this
     * method it is important that all required/expected information about the 
     * new part are already present before calling this
     * <p>
     * In the typical case of notifying a new {@link Part} this means that
     * this method must only be called after <ul>
     * <li> the {@link Asset} was created and the data where written to the {@link OutputStream}
     * <li> the {@link Annotation} body is completely written
     * <li> the {@link SpecificResource} with its source and {@link Selector} is defined.
     * </ul>
     *
     * @param ci          the processed content item
     * @param object      the new object / part
     * @throws RepositoryException on any error while committing the Item
     * @throws IOException if something with the used communication channel is wrong
     */
    public void sendNew(Item ci, URI object) throws IOException, RepositoryException;

    /**
     * Indicates if broker was informed, that extraction process has finished.
     * 
     * @return true if {@link sendFinish(...)} has be called, false otherwise
     */
    public boolean isFinished();

    /**
     * Indicates if broker was informed, that an error occurred during analysis process.
     * 
     * @return true if {@link sendError(...)} has be called, false otherwise
     */
    public boolean isError();

    /**
     * Indicates if a new part message was sent to the broker
     * @return <code>true</code> if {@link #sendNew(Item, URI)} has be called,
     * <code>false</code> otherwise
     */
    boolean hasNew();


}
