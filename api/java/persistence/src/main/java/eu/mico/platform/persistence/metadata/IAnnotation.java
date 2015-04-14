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
    void setBody(IBody body);

    /**
     * Setter for the selection.
     *
     * @param target
     */
    void setTarget(ITarget target);

    /**
     * Getter for the body.
     *
     * @return
     */
    IBody getBody();

    /**
     * Getter for the selection.
     *
     * @return
     */
    ITarget getTarget();

    /**
     * Setter for the provenance information.
     *
     * @param provenance
     */
    void setProvenance(IProvenance provenance);

    /**
     * Getter for the provenance information.
     * @return
     */
    IProvenance getProvenance();
}
