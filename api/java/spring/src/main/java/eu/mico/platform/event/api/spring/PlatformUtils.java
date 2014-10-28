/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
