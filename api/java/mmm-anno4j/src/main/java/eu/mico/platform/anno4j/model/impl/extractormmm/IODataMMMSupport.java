package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.impl.ResourceObjectSupport;

import java.util.HashSet;

/**
 * Support class for the IOData class, adding functionality.
 */
@Partial
public abstract class IODataMMMSupport extends ResourceObjectSupport implements IODataMMM {

    @Override
    public void addMimeType(MimeTypeMMM mimeType) {
        if(this.getMimeTypes() == null) {
            this.setMimeTypes(new HashSet<MimeTypeMMM>());
        }

        this.getMimeTypes().add(mimeType);
    };

    @Override
    public void addSemanticType(SemanticTypeMMM semanticType) {
        if(this.getSemanticTypes() == null) {
            this.setSemanticTypes(new HashSet<SemanticTypeMMM>());
        }

        this.getSemanticTypes().add(semanticType);
    };

    @Override
    public void addSyntacticType(SyntacticTypeMMM syntacticType) {
        if(this.getSyntacticTypes() == null) {
            this.setSyntacticTypes(new HashSet<SyntacticTypeMMM>());
        }

        this.getSyntacticTypes().add(syntacticType);
    };
}
