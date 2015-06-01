package eu.mico.platform.persistence.metadata;

/**
 * Object representing the provenance information
 * inside the MICO platform.
 */
public class MICOProvenance {

    /**
     * The Name of the extractor invoking the
     * createAnnotation() function of the
     * content object.
     */
    private String extractorName;

    /**
     * Specifies the input (type) the
     * given extractor requires.
     */
    private String requires;

    /**
     * Specifies the output (type) the
     * given extractor provides.
     */
    private String provides;

    public MICOProvenance() {
    }

    /**
     * Custom Constructor setting the the required provenance information.
     *
     * @param extractorName The name of the extractor.
     * @param requires      The required input (type).
     * @param provides      The provided output (type).
     */
    public MICOProvenance(String extractorName, String requires, String provides) {
        this.extractorName = extractorName;
        this.requires = requires;
        this.provides = provides;
    }


    /**
     * Sets new The Name of the extractor invoking the
     * createAnnotation function of the
     * content object..
     *
     * @param extractorName New value of The Name of the extractor invoking the
     *                      createAnnotation function of the
     *                      content object..
     */
    public void setExtractorName(String extractorName) {
        this.extractorName = extractorName;
    }

    /**
     * Sets new Specifies the input type the
     * given extractor requires..
     *
     * @param requires New value of Specifies the input type the
     *                 given extractor requires..
     */
    public void setRequires(String requires) {
        this.requires = requires;
    }

    /**
     * Gets The Name of the extractor invoking the
     * createAnnotation function of the
     * content object..
     *
     * @return Value of The Name of the extractor invoking the
     * createAnnotation function of the
     * content object..
     */
    public String getExtractorName() {
        return extractorName;
    }

    /**
     * Gets Specifies the output type the
     * given extractor provides..
     *
     * @return Value of Specifies the output type the
     * given extractor provides..
     */
    public String getProvides() {
        return provides;
    }

    /**
     * Gets Specifies the input type the
     * given extractor requires..
     *
     * @return Value of Specifies the input type the
     * given extractor requires..
     */
    public String getRequires() {
        return requires;
    }

    /**
     * Sets new Specifies the output type the
     * given extractor provides..
     *
     * @param provides New value of Specifies the output type the
     *                 given extractor provides..
     */
    public void setProvides(String provides) {
        this.provides = provides;
    }
}
