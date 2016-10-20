package eu.mico.platform.broker.api.rest;

import java.util.ArrayList;
import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import eu.mico.platform.broker.api.MICOBroker.WorkflowStatus;

@ApiObject(description="general information about a workflow and detailed information about required extractors and their connection state.")
public class WorkflowInfo {

public WorkflowInfo(WorkflowStatus wState, String wId, String wDesc) {
        id = wId;
        description = wDesc;
        state = wState;
        extractors = new ArrayList<ExtractorInfo>();
    }

    @ApiObjectField(description="unique workflow identifier")
    private String id;
    @ApiObjectField(description="short description of workflow")
    private String description;
    @ApiObjectField(description="list of extractors involved in this workflow")
    private List<ExtractorInfo> extractors;
    @ApiObjectField(description="availability information about workflow")
    private WorkflowStatus state;

public boolean addExtractor(ExtractorInfo eInfo) {
    return extractors.add(eInfo);
}

public List<ExtractorInfo> getExtractors() {
    return extractors;
}

public WorkflowStatus getState() {
    return state;
}

public void setState(WorkflowStatus state) {
    this.state = state;
}

@Override
public String toString() {
    return String.format("WorkflowInfo [state=%s, extractors=%s]", 
            state, extractorsString());
}

private String extractorsString(){
    StringBuilder sb = new StringBuilder();
    for (ExtractorInfo ei : extractors ){
       sb.append(ei.getName()).append("(").append(ei.getState()).append(")"); 
    }
    return sb.toString();
}

public String getId() {
    return id;
}

public void setId(String id) {
    this.id = id;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

}
