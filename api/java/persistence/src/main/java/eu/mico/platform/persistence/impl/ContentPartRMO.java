package eu.mico.platform.persistence.impl;

import com.github.anno4j.model.Annotation;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

/**
 * Wrapper for the mico ContentPart object. Containing
 * only the information that used to be persisted using Anno4j.
 */
@Iri(Ontology.CONTENT_PART_MICO)
public class ContentPartRMO implements RDFObject {

    /**
     * Unique identifier for the instance.
     */
    private Resource resource;

    /**
     * Conforms to oa:Annotation (http://www.openannotation.org/spec/core/core.html)
     */
    @Iri(Ontology.HAS_CONTENT_MICO)
    private Annotation annotation;


    public ContentPartRMO() {
    }

    public ContentPartRMO(Resource resource) {
        this.resource = resource;
    }

    /**
     * The current {@link org.openrdf.repository.object.ObjectConnection} this object is attached to. Will be implemented by the proxy object.
     */
    @Override
    public ObjectConnection getObjectConnection() {
        return null;
    }

    /**
     * Gets Unique identifier for the instance.
     *
     * @return Value of Unique identifier for the instance.
     */
    @Override
    public Resource getResource() {
        return resource;
    }

    /**
     * Sets new Unique identifier for the instance..
     *
     * @param resource New value of Unique identifier for the instance..
     */
    public void setResource(Resource resource) {
        this.resource = resource;
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
