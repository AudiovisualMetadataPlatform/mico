package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

import java.util.Set;

/**
 * Class to represent a syntactic type of a given IODataMMM object.
 */
@Iri(MMM.SYNTACTIC_TYPE)
public interface SyntacticTypeMMM extends ResourceObject {

    @Iri(MMM.HAS_ANNOTATION_CONVERSION_SCHEMA_URI)
    void setAnnotationConversionSchemaUri(String uri);

    @Iri(MMM.HAS_ANNOTATION_CONVERSION_SCHEMA_URI)
    String getAnnotationConversionSchemaUri();

    @Iri(MMM.HAS_DESCRIPTION)
    void setDescription(String description);

    @Iri(MMM.HAS_DESCRIPTION)
    String getDescription();

    @Iri(MMM.HAS_SYNTACTIC_TYPE_URI)
    void setSyntacticTypeUri(String uri);

    @Iri(MMM.HAS_SYNTACTIC_TYPE_URI)
    String getSyntacticTypeUri();

    @Iri(MMM.HAS_MIME_TYPE)
    void setMimeTypes(Set<MimeTypeMMM> mimeTypes);

    @Iri(MMM.HAS_MIME_TYPE)
    Set<MimeTypeMMM> getMimeTypes();

    void addMimeType(MimeTypeMMM mimeType);
}
