package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.DCTERMS;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

@Iri(MMM.LOW_LEVEL_FEATURE_BODY)
public interface LowLevelFeatureBodyMMM extends BodyMMM
{
    
    @Iri(DCTERMS.FORMAT)
    String getFormat();

    @Iri(DCTERMS.FORMAT)
    void setFormat(String format);

    @Iri(MMM.HAS_LOCATION)
    String getLocation();

    @Iri(MMM.HAS_LOCATION)
    void setLocation(String location);
}
