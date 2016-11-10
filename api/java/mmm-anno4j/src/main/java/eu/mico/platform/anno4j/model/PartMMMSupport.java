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

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.AnnotationSupport;
import com.github.anno4j.model.Target;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Precedes;
import org.openrdf.repository.object.RDFObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Support class for the Part.
 */
@Partial
@Precedes(AnnotationSupport.class)
public abstract class PartMMMSupport extends AnnotationSupport implements PartMMM {

    @Iri(MMM.HAS_TARGET)
    private Set<Target> targets;

    @Override
    public void addTarget(Target target) {
        this.getTarget().add(target);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void addInput(ResourceMMM input) {
        this.getInputs().add(input);
    }

    @Override
    public Set<Target> getTarget() {
        return targets;
    }

    @Override
    public void setTarget(Set<Target> targets) {
        if(targets != null) {
            this.targets.clear();
            this.targets.addAll(targets);
        } else {
            this.targets.clear();
        }
    }
}
