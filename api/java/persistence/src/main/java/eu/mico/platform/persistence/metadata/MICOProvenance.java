package eu.mico.platform.persistence.metadata;

import com.github.anno4j.model.Agent;
import com.github.anno4j.model.Motivation;

public class MICOProvenance {

    /**
     * Refers to http://www.w3.org/ns/oa#motivatedBy
     */
    private Motivation motivatedBy;
    /**
     * Refers to http://www.w3.org/ns/oa#serializedBy
     */
    private Agent serializedBy;
    /**
     * Refers to http://www.w3.org/ns/oa#serializedAt
     */
    private String serializedAt;
    /**
     * Refers to http://www.w3.org/ns/oa#annotatedBy
     */
    private Agent annotatedBy;
    /**
     * Refers to http://www.w3.org/ns/oa#annotatedAt
     */
    private String annotatedAt;

    public MICOProvenance() {

    }

    public MICOProvenance(Motivation motivatedBy, Agent serializedBy, String serializedAt, Agent annotatedBy, String annotatedAt) {
        this.motivatedBy = motivatedBy;
        this.serializedBy = serializedBy;
        this.serializedAt = serializedAt;
        this.annotatedBy = annotatedBy;
        this.annotatedAt = annotatedAt;
    }

    /**
     * Sets new Refers to http:www.w3.orgnsoa#serializedBy.
     *
     * @param serializedBy New value of Refers to http:www.w3.orgnsoa#serializedBy.
     */
    public void setSerializedBy(Agent serializedBy) {
        this.serializedBy = serializedBy;
    }

    /**
     * Gets Refers to http:www.w3.orgnsoa#serializedAt.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#serializedAt.
     */
    public String getSerializedAt() {
        return serializedAt;
    }

    /**
     * Sets new Refers to http:www.w3.orgnsoa#annotatedAt.
     *
     * @param annotatedAt New value of Refers to http:www.w3.orgnsoa#annotatedAt.
     */
    public void setAnnotatedAt(String annotatedAt) {
        this.annotatedAt = annotatedAt;
    }

    /**
     * Sets new Refers to http:www.w3.orgnsoa#serializedAt.
     *
     * @param serializedAt New value of Refers to http:www.w3.orgnsoa#serializedAt.
     */
    public void setSerializedAt(String serializedAt) {
        this.serializedAt = serializedAt;
    }

    /**
     * Gets Refers to http:www.w3.orgnsoa#motivatedBy.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#motivatedBy.
     */
    public Motivation getMotivatedBy() {
        return motivatedBy;
    }

    /**
     * Sets new Refers to http:www.w3.orgnsoa#motivatedBy.
     *
     * @param motivatedBy New value of Refers to http:www.w3.orgnsoa#motivatedBy.
     */
    public void setMotivatedBy(Motivation motivatedBy) {
        this.motivatedBy = motivatedBy;
    }

    /**
     * Gets Refers to http:www.w3.orgnsoa#annotatedBy.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#annotatedBy.
     */
    public Agent getAnnotatedBy() {
        return annotatedBy;
    }

    /**
     * Sets new Refers to http:www.w3.orgnsoa#annotatedBy.
     *
     * @param annotatedBy New value of Refers to http:www.w3.orgnsoa#annotatedBy.
     */
    public void setAnnotatedBy(Agent annotatedBy) {
        this.annotatedBy = annotatedBy;
    }

    /**
     * Gets Refers to http:www.w3.orgnsoa#serializedBy.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#serializedBy.
     */
    public Agent getSerializedBy() {
        return serializedBy;
    }

    /**
     * Gets Refers to http:www.w3.orgnsoa#annotatedAt.
     *
     * @return Value of Refers to http:www.w3.orgnsoa#annotatedAt.
     */
    public String getAnnotatedAt() {
        return annotatedAt;
    }
}
