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

import com.github.anno4j.model.impl.ResourceObject;
import com.github.anno4j.model.namespaces.DC;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * This class represents an Asset for an Item or a Part. An Asset stands for a multimedia file, associated with its
 * format and location.
 */
@Iri(MMM.ASSET)
public interface AssetMMM extends ResourceObject {

    /**
     * Gets this Asset's corresponding location over the http://www.mico-project.eu/ns/mmm/2.0/schema#hasLocation relationship.
     *
     * @return The corresponding location of this Asset.
     */
    @Iri(MMM.HAS_LOCATION)
    String getLocation();

    /**
     * Sets this Asset's corresponding location over the http://www.mico-project.eu/ns/mmm/2.0/schema#hasLocation relationship.
     *
     * @param location   The corresponding location of this Asset.
     */
    @Iri(MMM.HAS_LOCATION)
    void setLocation(String location);

    /**
     * Gets this Asset's corresponding format over the dc:format relationship.
     *
     * @return The format of this Asset.
     */
    @Iri(DC.FORMAT)
    String getFormat();

    /**
     * Sets this Asset's corresponding format over the dc:format relationship.
     *
     * @param format    The format to set.
     */
    @Iri(DC.FORMAT)
    void setFormat(String format);

    /**
     * Get this Asset's filename, so the name of the ingested multimedia file.
     *
     * @return  The name of this Asset.
     */
    @Iri(MMM.HAS_NAME)
    String getName();

    /**
     * Set this Asset's name, so the name of the ingested multimedia file.
     *
     * @param name  The name of this Asset.
     */
    @Iri(MMM.HAS_NAME)
    void setName(String name);

}
