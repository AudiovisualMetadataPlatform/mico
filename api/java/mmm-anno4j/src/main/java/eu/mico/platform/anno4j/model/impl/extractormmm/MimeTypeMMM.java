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
