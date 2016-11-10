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

import com.github.anno4j.model.namespaces.RDF;
import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Body class for the Diarization extractor. Diarization detects spoken parts in a audio file and assigns different speaker roles.
 * One body of this class represents one detected time span of a speaker.
 *
 * The timestamp is to be set via a temporal FragmentSelector assigned to the respective specific resource of the given Part.
 */
@Iri(MMMTERMS.DIARIZATION_BODY)
public interface DiarizationBodyMMM extends BodyMMM{

    @Iri(RDF.VALUE)
    void setSpeaker(String speaker);

    @Iri(RDF.VALUE)
    String getSpeaker();
}
