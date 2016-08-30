package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Interface for the RDF nodes that serve as parameter of a given Mode of an Extractor.
 */
@Iri(MMM.PARAM)
public interface ParamMMM {

    @Iri(RDF.VALUE)
    void setValue(String value);

    @Iri(RDF.VALUE)
    String getValue();

    @Iri(MMM.HAS_NAME)
    void setName(String name);

    @Iri(MMM.HAS_NAME)
    String getName();
}
