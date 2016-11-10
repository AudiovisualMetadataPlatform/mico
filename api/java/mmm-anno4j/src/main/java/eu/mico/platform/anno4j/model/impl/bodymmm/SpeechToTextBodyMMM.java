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
import org.openrdf.repository.object.LangString;

/**
 * Class represents the body for a SpeechToText annotation. The relevant timestamp information is stored in the
 * respective selector. The body itself contains which word has been detected.
 */
@Iri(MMMTERMS.STT_BODY_MICO)
public interface SpeechToTextBodyMMM extends BodyMMM {

    @Iri(RDF.VALUE)
    LangString getValue();

    /**
     * The value of the body corresponds to the word that is detected.
     */
    @Iri(RDF.VALUE)
    void setValue(LangString value);

}