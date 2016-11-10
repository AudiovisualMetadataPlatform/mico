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

import org.openrdf.annotations.Iri;
import org.openrdf.repository.object.LangString;
import org.openrdf.repository.object.RDFObject;

import eu.mico.platform.anno4j.model.namespaces.FAM;


@Iri(FAM.LINKED_ENTITY_ANNOTATION)
public interface LinkedEntityBody extends NamedEntityBody {

    @Iri(FAM.ENTITY_REFERENCE)
    public void setEntity(RDFObject entity);
    
    @Iri(FAM.ENTITY_REFERENCE)
    public RDFObject getEntity();
    
    @Iri(FAM.ENTITY_LABEL)
    public void setLabel(LangString label);
    
    @Iri(FAM.ENTITY_LABEL)
    public LangString getLabel();
    
}
