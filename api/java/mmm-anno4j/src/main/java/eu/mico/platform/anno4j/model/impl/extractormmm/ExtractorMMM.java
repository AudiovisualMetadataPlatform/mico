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

import com.github.anno4j.model.impl.agent.Software;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

import java.util.Set;

/**
 * Provenance class for an extractor in the MICO platform.
 * There, an extractor is the instance that creates metadata towards a given multimedia item.
 */
@Iri(MMM.EXTRACTOR)
public interface ExtractorMMM extends Software {

    @Iri(MMM.HAS_NAME)
    void setName(String name);

    @Iri(MMM.HAS_NAME)
    String getName();

    @Iri(MMM.HAS_VERSION)
    void setVersion(String version);

    @Iri(MMM.HAS_VERSION)
    String getVersion();

    @Iri(MMM.HAS_STRING_ID)
    void setStringId(String id);

    @Iri(MMM.HAS_STRING_ID)
    String getStringId();

    @Iri(MMM.HAS_MODE)
    void setModes(Set<ModeMMM> modes);

    @Iri(MMM.HAS_MODE)
    Set<ModeMMM> getModes();

    void addMode(ModeMMM mode);

}
