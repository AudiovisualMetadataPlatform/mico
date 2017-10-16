package eu.mico.platform.anno4j.model.impl.bodymmm;

import org.openrdf.annotations.Iri;

import com.github.anno4j.model.Body;

import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;



@Iri(MMMTERMS.ASSET_EXPORT_BODY)
public interface AssetExportBody extends Body {

    /**
     * Gets corresponding location over the http://www.mico-project.eu/ns/mmm/2.0/schema#hasLocation relationship.
     *
     * @return The new location of exported Asset.
     */
    @Iri(MMM.HAS_LOCATION)
    String getLocation();

    /**
     * Sets corresponding location over the http://www.mico-project.eu/ns/mmm/2.0/schema#hasLocation relationship.
     *
     * @param location The external location of exported Asset.
     */
    @Iri(MMM.HAS_LOCATION)
    void setLocation(String location);

}