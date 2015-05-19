package eu.mico.platform.event.test.mock;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
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

    private Map<URI, String> responses;

    public AnalysisResponseCollector() {
        responses = new HashMap<>();
    }

    @Override
    public void sendMessage(ContentItem ci, URI object) throws IOException {
        try {
            log.debug("sent message about {}", object.stringValue());
            final Content content = ci.getContentPart(object);
            responses.put(object, IOUtils.toString(content.getInputStream()));
        } catch (RepositoryException e) {
            log.error("Repository Exception: {}", e.getMessage(), e);
        }
    }

    public Map<URI, String> getResponses() {
        return Collections.unmodifiableMap(responses);
    }

}
