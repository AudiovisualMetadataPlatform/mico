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

package eu.mico.platform.anno4j.model;

import com.github.anno4j.model.namespaces.OADM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

import java.util.Set;

/**
 * Class represents an ItemMMM of the MICO workflow.
 * For every ingested multimedia file at the platform, an ItemMMM will be created which corresponds
 * (with its follow Parts) to the file with its analysed metadata background.
 */
@Iri(MMM.ITEM)
public interface ItemMMM extends ResourceMMM {

    /**
     * Gets the available Parts over corresponding http://www.mico-project.eu/ns/mmm/2.0/schema#hasPart relationships.
     *
     * @return Values of http://www.mico-project.eu/ns/mmm/2.0/schema#hasPart.
     */
    @Iri(MMM.HAS_PART)
    Set<PartMMM> getParts();

    /**
     * Sets a set of Parts by specifying http://www.mico-project.eu/ns/mmm/2.0/schema#hasPart relationships.
     *
     * @param parts New values of http://www.mico-project.eu/ns/mmm/2.0/schema#hasPart.
     */
    @Iri(MMM.HAS_PART)
    void setParts(Set<PartMMM> parts);

    /**
     * Adds a new single http://www.mico-project.eu/ns/mmm/2.0/schema#hasPart relationship to this ItemMMM.
     *
     * @param part The new Part to add.
     */
    void addPart(PartMMM part);

    @Iri(OADM.SERIALIZED_AT)
    String getSerializedAt();

    /**
     * Sets http:www.w3.org/ns/oa#serializedAt.
     *
     * @param serializedAt New value of http:www.w3.org/ns/oa#serializedAt.
     */
    @Iri(OADM.SERIALIZED_AT)
    void setSerializedAt(String serializedAt);

    /**
     * Sets http:www.w3.org/ns/oa#serializedAt according to the format year-month-dayThours:minutes:secondsZ, e.g. 2015-12-16T12:00:00Z.
     *
     * @param year      The year to set.
     * @param month     The month to set.
     * @param day       The day to set.
     * @param hours     The hours to set.
     * @param minutes   The minutes to set.
     * @param seconds   The seconds to set.
     */
    void setSerializedAt(int year, int month, int day, int hours, int minutes, int seconds);
}
