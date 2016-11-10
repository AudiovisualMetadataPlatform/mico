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
import eu.mico.platform.anno4j.model.namespaces.gen.MA;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

/**
 * Body class for the Audio Demux extractor. Audio Demux excerpts the audio track from a given video file.
 * The audio file should be added as an Asset to the respective Part class of this body.
 */
@Iri(MMMTERMS.AUDIO_DEMUX_BODY)
public interface AudioDemuxBodyMMM extends BodyMMM {

    @Iri(MA.SAMPLING_RATE_STRING)
    void setFrameRate(String framerate);

    @Iri(MA.SAMPLING_RATE_STRING)
    String getFrameRate();
}
