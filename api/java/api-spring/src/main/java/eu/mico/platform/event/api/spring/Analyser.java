package eu.mico.platform.event.api.spring;

import eu.mico.platform.event.api.AnalysisResponse;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;

/**
 * Proxy interface to be implemented by MICO platform analyser which use the spring annotation API.
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
public interface Analyser {

    /**
     * The callback which is called whenever a new event for this service has been received.
     * @param analysisResponse  a response object that can be used to send back notifications about new objects to the broker
     * @param contentItem the content item to analyse
     * @param uri the URI of the object to analyse in the content item (a content part or a metadata URI)
     */
    public void call(AnalysisResponse analysisResponse, ContentItem contentItem, URI uri);
}