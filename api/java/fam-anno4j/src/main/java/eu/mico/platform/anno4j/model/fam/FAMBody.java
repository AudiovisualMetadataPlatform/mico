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

package eu.mico.platform.anno4j.model.fam;

import java.util.Collection;
import java.util.Set;

import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.model.Selector;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.ANNOTATION_BODY)
public interface FAMBody extends BodyMMM {

    @Iri(FAM.CONFIDENCE)
    void setConfidence(Double confidence);
    
    @Iri(FAM.CONFIDENCE)
    Double getConfidence();
    
    @Iri(FAM.EXTRACTED_FROM)
    public void setContent(RDFObject content);

    void setContentURI(Resource content);

    void setContentURI(String uri);
    
    @Iri(FAM.EXTRACTED_FROM)
    public RDFObject getContent();
    
    public void addSelector(Selector selector);
    
    @Iri(FAM.SELECTOR)
    public void setSelectors(Set<Selector> selectors);
    
    /**
     * Unmodifiable collection of the selectors for this Body
     * @return
     */
    @Iri(FAM.SELECTOR)
    public Set<Selector> getSelectors();


}
