package eu.mico.platform.persistence.impl;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.ontologies.OADM;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

/**
 * Customized Annotation object for the MICO platform.
 * Contains the required input type and the provided output type
 * of the related extractor.
 */
@Iri(OADM.ANNOTATION)
public class MICOAnnotation extends Annotation {

    /**
     * Specifies the input (type) the
     * given extractor requires.
     */
    @Iri(Ontology.REQUIRES_MICO)
    private String requires;

    /**
     * Specifies the output (type) the
     * given extractor provides.
     */
    @Iri(Ontology.PROVIDES_MICO)
    private String provides;

    public MICOAnnotation() {
    }

    public MICOAnnotation(String provides, String requires) {
        this.provides = provides;
        this.requires = requires;
    }


    /**
     * Sets new requires.
     *
     * @param requires New value of requires.
     */
    public void setRequires(String requires) {
        this.requires = requires;
    }

    /**
     * Gets provides.
     *
     * @return Value of provides.
     */
    public String getProvides() {
        return provides;
    }

    /**
     * Gets requires.
     *
     * @return Value of requires.
     */
    public String getRequires() {
        return requires;
    }

    /**
     * Sets new provides.
     *
     * @param provides New value of provides.
     */
    public void setProvides(String provides) {
        this.provides = provides;
    }
}
