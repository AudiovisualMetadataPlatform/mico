package eu.mico.platform.event.api;

import org.openrdf.model.URI;

/**
 * Interface for common methods of AnalysisService and AnalysisServiceAnno4j
 */
public interface AnalysisServiceBase {
    /**
     * Return a unique ID (URI) that identifies this service and its functionality.
     *
     * @return a unique ID identifying this service globally
     */
    URI getServiceID();

    /**
     * Return an ID (String) that identifies this extractor
     *
     * @return an ID that identifies the general functionality of this extractor
     */
    String getExtractorID();

    /**
     * Return an ID (String) that identifies this extractor
     *
     * @return an ID that identifies the specific mode in which the extractor is running
     */
    String getExtractorModeID();

    /**
     * Returns the version of the extractor
     *
     * @return Extractor version (String)
     */
    String getExtractorVersion();

    /**
     * Return the type of output produced by this service as symbolic identifier. In the first version of the API, this
     * is simply an arbitrary string (e.g. MIME type)
     *
     * @return a symbolic identifier representing the output type of this service
     */
    String getProvides();

    /**
     * Return the type of input required by this service as symbolic identifier. In the first version of the API, this
     * is simply an arbitrary string (e.g. MIME type)
     *
     * @return  a symbolic identifier representing the input type of this service
     */
    String getRequires();

    /**
     * Return the queue name that should be used by the messaging infrastructure for this service. If explicitly set,
     * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
     * balancing.
     *
     * The implementation can return null, in which case the event API will choose a random queue name.
     *
     * @return a string identifying the queue name this service wants to use
     */
    String getQueueName();
}
