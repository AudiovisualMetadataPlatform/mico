package eu.mico.platform.event.api.spring;

import com.google.common.io.ByteStreams;
import eu.mico.platform.persistence.model.Content;
import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.repository.RepositoryException;

import java.io.*;

/**
 * Utilities for a convenient access to the MICO platform.
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
public class PlatformUtils {

    /**
     * Creates a new content Item and content part and uploads the given file to the MICO platform.
     * @param file The media file.
     * @param type Symbolic identifier for the media type (e.g. MIME type).
     * @param platformConfiguration MICO platform configuration.
     * @return the created content part.
     * @throws RepositoryException
     * @throws IOException
     */
    public static Content persistNewContentItemWithPart(File file, String type, PlatformConfiguration platformConfiguration) throws RepositoryException, IOException {
        ContentItem contentItem =  platformConfiguration.getPersistenceService().createContentItem();
        Content contentPart = contentItem.createContentPart();

        try (
            OutputStream outputStream = contentPart.getOutputStream();
            InputStream inputStream = new FileInputStream(file);
        ) {
            ByteStreams.copy(inputStream, outputStream);
            contentPart.setType(type);
            platformConfiguration.getEventManager().injectContentItem(contentItem);
        }

        return contentPart;
    }
}
