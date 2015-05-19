package eu.mico.platform.persistence.impl.SelectorImpl;

import com.github.anno4j.model.Selector;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

@Iri(Ontology.SVG_SELECTOR_OA)
public class SVGSelector extends Selector {
    
    @Iri(Ontology.VALUE_RDF)
    private String vectorLocation;
    
    @Iri(Ontology.CONFORMS_TO_DCTERMS)
    private final String CONFORMS_TO = "http://www.w3.org/TR/SVG";

    public SVGSelector() {
    }

    public SVGSelector(String vectorLocation) {
        this.vectorLocation = vectorLocation;
    }

    public String getVectorLocation() {
        return vectorLocation;
    }

    public void setVectorLocation(String vectorLocation) {
        this.vectorLocation = vectorLocation;
    }
}
