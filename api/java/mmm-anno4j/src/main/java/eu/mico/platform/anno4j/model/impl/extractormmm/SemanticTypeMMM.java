package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Class to represent a semantic type for a given IODataMMM object.
 */
@Iri(MMM.SEMANTIC_TYPE)
public interface SemanticTypeMMM extends ResourceObject{

    @Iri(MMM.HAS_NAME)
    void setName(String name);

    @Iri(MMM.HAS_NAME)
    String getName();

    @Iri(MMM.HAS_DESCRIPTION)
    void setDescription(String description);

    @Iri(MMM.HAS_DESCRIPTION)
    String getDescription();

    @Iri(MMM.HAS_SEMANTIC_TYPE_URI)
    void setSemanticTypeUri(String uri);

    @Iri(MMM.HAS_SEMANTIC_TYPE_URI)
    String getSemanticTypeUri();
}
