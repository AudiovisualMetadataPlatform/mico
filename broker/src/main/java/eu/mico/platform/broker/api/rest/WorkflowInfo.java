package eu.mico.platform.broker.api.rest;

import java.util.ArrayList;
import java.util.List;

import org.jsondoc.core.annotation.ApiObject;

import eu.mico.platform.broker.api.MICOBroker.ExtractorStatus;
import eu.mico.platform.broker.api.MICOBroker.WorkflowStatus;

@ApiObject
public class WorkflowInfo {

public WorkflowInfo(WorkflowStatus wState) {
        state = wState;
        extractors = new ArrayList<ExtractorInfo>();
    }

private List<ExtractorInfo> extractors;

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

}
