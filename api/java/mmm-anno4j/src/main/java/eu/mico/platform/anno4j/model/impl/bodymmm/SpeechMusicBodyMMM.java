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

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Body class for the Speech-Music-Discrimination extractor. This body is used when only music is detected during a time
 * span in the given audio stream.
 *
 * The relevant time information is stored in the associated specific resource of the given part, or rather in the
 * associated temporal FragmentSelector.
 */
@Iri(MMMTERMS.SPEECH_MUSIC_BODY)
public interface SpeechMusicBodyMMM extends BodyMMM {
}
