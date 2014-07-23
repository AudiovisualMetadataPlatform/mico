package eu.mico.platform.event.model;

/**
 * Thrown when a problem occurs during analysis. In this case, the event manager will send a NACK to the broker
 * to re-queue the task.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class AnalysisException extends Exception {

    public AnalysisException() {
    }

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
