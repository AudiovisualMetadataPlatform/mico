package eu.mico.platform.anno4j.model;

import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

@Iri(MMM.RESOURCE)
public interface ResourceMMM extends ResourceObject {

    @Iri(MMM.HAS_ASSET)
    AssetMMM getAsset();

    @Iri(MMM.HAS_ASSET)
    void setAsset(AssetMMM asset);

    @Iri(MMM.HAS_SYNTACTICAL_TYPE)
    String getSyntacticalType();

    @Iri(MMM.HAS_SYNTACTICAL_TYPE)
    void setSyntacticalType(String syntacticalType);

    @Iri(MMM.HAS_SEMANTIC_TYPE)
    String getSemanticType();

    @Iri(MMM.HAS_SEMANTIC_TYPE)
    void setSemanticType(String semanticType);
}
