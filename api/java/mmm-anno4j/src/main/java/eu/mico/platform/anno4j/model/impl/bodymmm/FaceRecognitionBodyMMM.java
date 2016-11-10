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
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

@Iri(MMMTERMS.FACE_RECOGNITION_BODY)
public interface FaceRecognitionBodyMMM extends BodyMMM {


    @Iri(RDF.VALUE)
    public String getDetection();

    /**
     * The name of the person that was detected
     */
    @Iri(RDF.VALUE)
    public void setDetection(String detection);

    @Iri(MMM.HAS_CONFIDENCE)
    public Double getConfidence();

    /**
     * Confidence value for the detected face
     */
    @Iri(MMM.HAS_CONFIDENCE)
    public void setConfidence(Double confidence);
}
