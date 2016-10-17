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
