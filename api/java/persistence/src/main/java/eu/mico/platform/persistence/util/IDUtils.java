package eu.mico.platform.persistence.util;

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

    public static String generatedRandomId() {
        return UUID.randomUUID().toString();
    }

    public static URI generatedRandomUri(ContentItem item) {
        return new URIImpl(item.getURI().stringValue() + "/" + generatedRandomId());
    }

}
