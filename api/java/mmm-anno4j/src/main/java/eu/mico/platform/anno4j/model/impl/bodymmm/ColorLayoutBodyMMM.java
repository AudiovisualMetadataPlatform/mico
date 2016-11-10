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
import eu.mico.platform.anno4j.model.namespaces.MMM;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;
import org.openrdf.annotations.Iri;

@Iri(MMMTERMS.COLORLAYOUT_BODY)
public interface ColorLayoutBodyMMM extends BodyMMM {

    @Iri(MMMTERMS.YDCCOEFF)
    String getYDC();

    @Iri(MMMTERMS.YDCCOEFF)
    void setYDC(String YDC);

    @Iri(MMMTERMS.CBDCCOEFF)
    String getCbDC();

    @Iri(MMMTERMS.CBDCCOEFF)
    void setCbDC(String cbDC);

    @Iri(MMMTERMS.CRDCCOEFF)
    String getCrDC();

    @Iri(MMMTERMS.CRDCCOEFF)
    void setCrDC(String crDC);

    @Iri(MMMTERMS.CBACCOEFF)
    String getCbAC();

    @Iri(MMMTERMS.CBACCOEFF)
    void setCbAC(String cbAC);

    @Iri(MMMTERMS.CRACCOEFF)
    String getCrAC();

    @Iri(MMMTERMS.CRACCOEFF)
    void setCrAC(String crAC);

    @Iri(MMMTERMS.YACCOEFF)
    String getYAC();

    @Iri(MMMTERMS.YACCOEFF)
    void setYAC(String YAC);
}
