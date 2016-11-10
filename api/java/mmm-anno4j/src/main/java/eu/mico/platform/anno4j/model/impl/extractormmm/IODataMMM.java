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
 * Superclass for input and output data that is either produced or consumed by a given ExtractorMMM and ModeMMM.
 */
@Iri(MMM.IO_DATA)
public interface IODataMMM extends ResourceObject {

    @Iri(MMM.HAS_INDEX)
    void setIndex(int Index);

    @Iri(MMM.HAS_INDEX)
    int getIndex();

    @Iri(MMM.HAS_CMD_LINE_SWITCH)
    void setCmdLineSwitch(String cmdLineSwitch);

    @Iri(MMM.HAS_CMD_LINE_SWITCH)
    String getCmdLineSwitch();

    @Iri(MMM.HAS_MIME_TYPE)
    void setMimeTypes(Set<MimeTypeMMM> mimeTypes);

    @Iri(MMM.HAS_MIME_TYPE)
    Set<MimeTypeMMM> getMimeTypes();

    void addMimeType(MimeTypeMMM mimeType);

    // Gonna be changed once the property on ResourceMMM has been changed
    @Iri(MMM.HAS_SEMANTIC_DATA_TYPE)
    void setSemanticTypes(Set<SemanticTypeMMM> semanticTypes);

    @Iri(MMM.HAS_SEMANTIC_DATA_TYPE)
    Set<SemanticTypeMMM> getSemanticTypes();

    void addSemanticType(SemanticTypeMMM semanticType);

    @Iri(MMM.HAS_SYNTACTIC_TYPE)
    void setSyntacticTypes(Set<SyntacticTypeMMM> syntacticTypes);

    @Iri(MMM.HAS_SYNTACTIC_TYPE)
    Set<SyntacticTypeMMM> getSyntacticTypes();

    void addSyntacticType(SyntacticTypeMMM syntacticType);
}
