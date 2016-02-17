package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Body class for a generic object detection algorithm.
 */
@Iri(MMM.OBJECT_DETECTION_BODY)
public interface ObjectDetectionBodyMMM extends BodyMMM {

    @Iri(MMM.HAS_CONFIDENCE)
    void setConfidence(Double confidence);

    @Iri(MMM.HAS_CONFIDENCE)
    String getConfidence();

    @Iri(RDF.VALUE)
    void setValue(String value);

    @Iri(RDF.VALUE)
    String getValue();

    // Probably old information, might be deleted with new broker model
    @Iri(MMM.HAS_EXTRACTION_VERSION)
    void setExtractionVersion(String extractionVersion);

    @Iri(MMM.HAS_EXTRACTION_VERSION)
    String getExtractionVersion();
}
