package eu.mico.platform.anno4j.model.namespaces;

/**
 * Ontology class for the Ontology for Media Resources 1.0 ontology (ma:).
 * See <a href="https://www.w3.org/TR/mediaont-10/">https://www.w3.org/TR/mediaont-10/</a>
 */
public class MA {

    public final static String NS = "http://www.w3.org/ns/ma-ont#";
    public final static String PREFIX = "ma";

    /**
     * Refers to ma:samplingRate. The value differs depending on usage from interger, to float, to String.
     */
    public final static String SAMPLING_RATE = NS + "samplingRate";
}
