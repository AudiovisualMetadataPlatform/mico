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

import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Body class for a matching segments of two videos. Inherits from the MatchingSegmentBodyMMM and therefore adapts its behaviour.
 * But it adds more semantic by its type, treating matches in audio files.
 */
@Iri(MMMTERMS.MATCHING_AUDIO_SEGMENT_BODY)
public interface MatchingAudioSegmentBodyMMM extends MatchingSegmentBodyMMM {

}
