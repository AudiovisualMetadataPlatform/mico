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
package eu.mico.platform.event.model;

import eu.mico.platform.event.model.Event.ErrorCodes;

/**
 * Thrown when a problem occurs during analysis. In this case, the event manager will send a NACK to the broker
 * to re-queue the task.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class AnalysisException extends Exception {

    private ErrorCodes code;

    public AnalysisException() {
        this(null,null,null);
    }

    public AnalysisException(String message) {
        this(null,message,null);
    }

    public AnalysisException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public AnalysisException(ErrorCodes code, String message, Throwable cause) {
        super(message, cause);
        this.code = code == null ? ErrorCodes.UNEXPECTED_ERROR : code;
    }
    /**
     * The ErrorCode
     * @return
     */
    public ErrorCodes getCode() {
        return code;
    }

}
