/*
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
