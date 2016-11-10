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

package eu.mico.platform.broker.api.rest;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonInclude;

@ApiObject(name="InjectResponse",description="general information about inject status")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InjectResponse {

    @ApiObjectField(required=true, description="human readable description of inject status")
    private String message;
    @ApiObjectField(required=false, description="detailed information about the triggered workflow")
    private WorkflowInfo workflowInfo;
    
    
    public InjectResponse(String message) {
        this.message = message;
    }

    public InjectResponse(String message, WorkflowInfo workflowInfo) {
        this.message = message;
        this.workflowInfo = workflowInfo;
    }
    
    public WorkflowInfo getWorkflowInfo() {
        return workflowInfo;
    }
    public void setWorkflowInfo(WorkflowInfo wInfo) {
        this.workflowInfo = wInfo;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
