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

import java.util.Set;

import org.openrdf.annotations.Iri;

import org.openrdf.repository.object.LangString;
import org.openrdf.repository.object.RDFObject;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.ENTITY_MENTION_ANNOTATION)
public interface NamedEntityBody extends FAMBody {

    
    @Iri(FAM.ENTITY_MENTION)
    public void setMention(LangString mention);
    
    @Iri(FAM.ENTITY_MENTION)
    public LangString getMention();
    
    /**
     * Sets the types. 
     * @param types the types or <code>null</code> to remove all current types
     */
    @Iri(FAM.ENTITY_TYPE)
    public void setTypes(Set<RDFObject> types);

    /**
     * Getter for the types.
     * @return the set of types
     */
    @Iri(FAM.ENTITY_TYPE)
    public Set<RDFObject> getTypes();
    
    public void addType(String type);
    
    public void addType(RDFObject type);
    
}
