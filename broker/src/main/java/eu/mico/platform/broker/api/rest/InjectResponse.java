package eu.mico.platform.broker.api.rest;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import com.fasterxml.jackson.annotation.JsonInclude;

@ApiObject
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
