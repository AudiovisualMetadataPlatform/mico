package eu.mico.platform.persistence.util;

import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.util.UUID;

/**
 * Some ID generation utilities
 *
 * @author Sergio Fernandez
 */
public class IDUtils {

    /**
     * Generate random UUID
     *
     * @return uuid
     */
    public static UUID generatedRandomUuid() {
        return UUID.randomUUID();
    }

    /**
     * Generate random ID
     *
     * @return id
     */
    public static String generatedRandomId() {
        return generatedRandomUuid().toString();
    }

    /**
     * Generate random URI using the Content Item as base
     *
     * @param item parent content item
     * @return uri
     */
    public static URI generatedRandomUri(ContentItem item) {
        return new URIImpl(item.getURI().stringValue() + "/" + generatedRandomId());
    }

    /**
     * Generate random URI using the Content Part as base
     *
     * @param part parent content part
     * @return uri
     */
    public static URI generatedRandomUri(Content part) {
        return new URIImpl(part.getURI().stringValue() + "/" + generatedRandomId());
    }

}
