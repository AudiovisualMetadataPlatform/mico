package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.impl.ResourceObjectSupport;

import java.util.HashSet;

/**
 * Support class for the ExtractorMMM class, adding functionality.
 */
@Partial
public abstract class ExtractorMMMSupport extends ResourceObjectSupport implements ExtractorMMM {

    @Override
    public void addMode(ModeMMM mode) {
        if(this.getModes() == null) {
            this.setModes(new HashSet<ModeMMM>());
        }

        this.getModes().add(mode);
    };
}
