package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Class to represent a MIME type of a given IODataMMM object.
 */
@Iri(MMM.MIME_TYPE)
public interface MimeTypeMMM extends ResourceObject {

    @Iri(MMM.HAS_FORMAT_CONVERSION_SCHEMA_URI)
    void setFormatConversionSchemaUri(String uri);

    @Iri(MMM.HAS_FORMAT_CONVERSION_SCHEMA_URI)
    String getFormatConversionSchemaUri();

    @Iri(MMM.HAS_STRING_ID)
    void setStringId(String id);

    @Iri(MMM.HAS_STRING_ID)
    String getStringId();
}
