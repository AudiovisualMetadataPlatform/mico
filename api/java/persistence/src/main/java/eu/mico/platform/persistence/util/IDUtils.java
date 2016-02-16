package eu.mico.platform.persistence.util;

import eu.mico.platform.persistence.model.Part;
import eu.mico.platform.persistence.model.Item;
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
     * Generate random URI using the Part Item as base
     *
     * @param item parent content item
     * @return uri
     */
    public static URI generatedRandomUri(Item item) {
        return new URIImpl(item.getURI().stringValue() + "/" + generatedRandomId());
    }

    /**
     * Generate random URI using the Part Part as base
     *
     * @param part parent content part
     * @return uri
     */
    public static URI generatedRandomUri(Part part) {
        return new URIImpl(part.getURI().stringValue() + "/" + generatedRandomId());
    }

}
