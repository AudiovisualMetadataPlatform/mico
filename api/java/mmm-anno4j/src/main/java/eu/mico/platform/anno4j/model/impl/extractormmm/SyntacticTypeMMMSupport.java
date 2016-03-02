package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.impl.ResourceObjectSupport;

import java.util.HashSet;

/**
 * Support class for the SyntacticType class, adding functionality.
 */
@Partial
public abstract class SyntacticTypeMMMSupport extends ResourceObjectSupport implements SyntacticTypeMMM  {

    @Override
    public void addMimeType(MimeTypeMMM mimeType) {
        if(this.getMimeTypes() == null) {
            this.setMimeTypes(new HashSet<MimeTypeMMM>());
        }

        this.getMimeTypes().add(mimeType);
    }
}
