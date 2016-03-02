package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.impl.ResourceObjectSupport;

import java.util.HashSet;

/**
 * Support class for the ModeMMM class, adding functionality.
 */
@Partial
public abstract class ModeMMMSupport extends ResourceObjectSupport implements ModeMMM {

    @Override
    public void addOutput(OutputMMM output) {
        if(this.getOutput() == null) {
            this.setOutput(new HashSet<OutputMMM>());
        }

        this.getOutput().add(output);
    }

    @Override
    public void addInput(InputMMM input) {
        if(this.getInput() == null) {
            this.setInput(new HashSet<InputMMM>());
        }

        this.getInput().add(input);
    }
}
