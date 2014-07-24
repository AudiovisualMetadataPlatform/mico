package eu.mico.platform.broker.exception;

/**
 * Thrown when a state is not found in the service graph.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class StateNotFoundException extends Exception {

    public StateNotFoundException(String message) {
        super(message);
    }
}
