package eu.mico.platform.event.test.mock;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnalysisResponseCollector implements AnalysisResponse {

    private static Logger log = LoggerFactory.getLogger(AnalysisResponseCollector.class);

    private Map<URI, String> responses,progresses,errors;

    public AnalysisResponseCollector() {
        responses = new HashMap<>();
        progresses = new HashMap<>();
        errors = new HashMap<>();
    }

    @Override
    public void sendMessage(Item ci, URI object) throws IOException {
        try {
            log.debug("sent message about {}", object.stringValue());
            final Part part = ci.getPart(object);
            responses.put(object, IOUtils.toString(part.getInputStream()));
        } catch (RepositoryException e) {
            log.error("Repository Exception: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendFinish(Item ci, URI object) throws IOException {
        try {
            log.debug("sent message about {}", object.stringValue());
            final Part part = ci.getPart(object);
            responses.put(object, IOUtils.toString(part.getInputStream()));
        } catch (RepositoryException e) {
            log.error("Repository Exception: {}", e.getMessage(), e);
        }
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
    public void sendError(Item ci, URI object, String msg, String desc)
            throws IOException {
        log.debug("sent error message about {}", object.stringValue());
        errors.put(object, msg);
    }

    @Override
    public void sendNew(Item ci, URI object)
            throws IOException {
        // TODO Auto-generated method stub
        
    }

}
