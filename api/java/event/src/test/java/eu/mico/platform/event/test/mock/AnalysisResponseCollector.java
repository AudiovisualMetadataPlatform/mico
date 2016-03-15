package eu.mico.platform.event.test.mock;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.event.model.Event.ErrorCodes;
import eu.mico.platform.persistence.model.Item;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnalysisResponseCollector implements AnalysisResponse {

    private static Logger log = LoggerFactory.getLogger(AnalysisResponseCollector.class);

    private Map<URI, String> responses,progresses,errors;

    private boolean finished = false;

    private boolean hasError = false;

    public AnalysisResponseCollector() {
        responses = new HashMap<>();
        progresses = new HashMap<>();
        errors = new HashMap<>();
    }

    @Override
    public void sendFinish(Item ci) throws IOException {
            log.debug("sent message about {}", ci.getURI().stringValue());
            responses.put(ci.getURI(), ci.getSemanticType());
    }

    public Map<URI, String> getResponses() {
        return Collections.unmodifiableMap(responses);
    }

    @Override
    public void sendProgress(Item ci, URI object, float progress)
            throws IOException {
        log.debug("sent progress message about {}", object.stringValue());
        progresses.put(object, String.valueOf(progress));
    }

    @Override
    public void sendError(Item ci, ErrorCodes code, String msg, String desc)
            throws IOException {
        log.debug("sent error message about {}", ci.getURI().stringValue());
        errors.put(ci.getURI(), msg);
    }

    @Override
    public void sendNew(Item ci, URI object)
            throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isError() {
        return hasError ;
    }

}
