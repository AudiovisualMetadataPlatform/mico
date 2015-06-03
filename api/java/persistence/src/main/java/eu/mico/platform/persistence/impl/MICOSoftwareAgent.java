package eu.mico.platform.persistence.impl;

import com.github.anno4j.model.impl.agent.Software;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri("http://www.w3.org/ns/prov/SoftwareAgent")
public class MICOSoftwareAgent extends Software {

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


    public MICOSoftwareAgent(String requires, String provides, String extractorName) {
        this.requires = requires;
        this.provides = provides;
        super.setName(extractorName);
    }

    public MICOSoftwareAgent() {
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
}
