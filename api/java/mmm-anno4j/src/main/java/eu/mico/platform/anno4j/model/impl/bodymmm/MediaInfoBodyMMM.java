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

import eu.mico.platform.anno4j.model.namespaces.gen.MA;
import eu.mico.platform.anno4j.model.namespaces.MMMTERMS;

import org.openrdf.annotations.Iri;

/**
 * Generic body designed for low level media Info. Ontology for Media Resources 1.0 offers many possible relationships.
 * Should be more fine-grained for different media files.
 *
 * XML file associated (via Asset) which contains all extracted information.
 */
@Iri(MMMTERMS.MEDIA_INFO_BODY)
public interface MediaInfoBodyMMM extends MultiMediaBodyMMM, ImageDimensionBodyMMM {

  @Iri(MA.SAMPLING_RATE_STRING)
  public String getSamplingRate();

  @Iri(MA.SAMPLING_RATE_STRING)
  public void setSamplingRate(String rate);

  @Iri(MA.AVERAGE_BIT_RATE_STRING)
  public String getAvgBitRate();

  @Iri(MA.AVERAGE_BIT_RATE_STRING)
  public void setAvgBitRate(String avgBitRate);
}
