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

package eu.mico.platform.anno4j.model.impl.bodymmm;

import com.github.anno4j.model.namespaces.DCTERMS;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

@Iri(MMMTERMS.COLOR_LAYOUT_RAW_BODY)
public interface ColorLayoutRawBodyMMM extends BodyMMM {

    @Iri(MMM.HAS_LOCATION)
    String getLayoutLocation();

    @Iri(MMM.HAS_LOCATION)
    void setLayoutLocation(String layoutLocation);

    @Iri(DCTERMS.FORMAT)
    String getFormat();

    @Iri(DCTERMS.FORMAT)
    void setFormat(String format);
}
