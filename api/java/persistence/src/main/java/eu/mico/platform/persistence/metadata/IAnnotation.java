package eu.mico.platform.persistence.metadata;

/**
 * A wrapper to bundle the body and selection object
 */
public interface IAnnotation {

    /**
     * Setter for the body.
     *  
     * @param body
     */
    public void setBody(IBody body);

    /**
     * Setter for the selection.
     *  
     * @param target
     */
    public void setTarget(ITarget target);

    /**
     * Getter for the body.
     *  
     * @return
     */
    public IBody getBody();

    /**
     * Getter for the selection.
     *  
     * @return
     */
    public ITarget getTarget();
}
