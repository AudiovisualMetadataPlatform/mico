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

import java.util.HashSet;
import java.util.Set;

import org.openrdf.repository.object.RDFObject;

import com.github.anno4j.annotations.Partial;

@Partial
public abstract class NamedEntityBodySupport extends FAMBodySupport
        implements NamedEntityBody {

    @Override
    public void addType(String typeUri) {
        if(typeUri == null){
            return;
        }
        addType(new Reference(typeUri));
    }

    @Override
    public void addType(RDFObject type) {
        if(type == null){
            return;
        }
        Set<RDFObject> types = getTypes();
        if(types == null){
            types = new HashSet<>();
            setTypes(types);
        }
        types.add(type);

    }

}
