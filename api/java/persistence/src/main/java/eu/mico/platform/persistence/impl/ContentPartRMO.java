package eu.mico.platform.persistence.impl;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.namespaces.MICO;
import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;

/**
 * Wrapper for the mico ContentPart object. Containing
 * only the information that used to be persisted using Anno4j.
 */
@Iri(MICO.CONTENT_PART)
public class ContentPartRMO extends ResourceObject {

    /**
     * Conforms to oa:Annotation (http://www.openannotation.org/spec/core/core.html)
     */
    @Iri(MICO.HAS_CONTENT)
    private Annotation annotation;

    public ContentPartRMO() {
    }

    public ContentPartRMO(Resource uri) {
        this.setResource(uri);
    }

    /**
     * Sets new Conforms to oa:Annotation http:www.openannotation.orgspeccorecore.html.
     *
     * @param annotation New value of Conforms to oa:Annotation http:www.openannotation.orgspeccorecore.html.
     */
    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Gets Conforms to oa:Annotation http:www.openannotation.orgspeccorecore.html.
     *
     * @return Value of Conforms to oa:Annotation http:www.openannotation.orgspeccorecore.html.
     */
    public Annotation getAnnotation() {
        return annotation;
    }
}
