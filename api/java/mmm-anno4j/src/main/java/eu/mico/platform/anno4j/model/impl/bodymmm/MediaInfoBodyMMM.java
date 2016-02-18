package eu.mico.platform.anno4j.model.impl.bodymmm;

import eu.mico.platform.anno4j.model.BodyMMM;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

/**
 * Generic body designed for low level media Info. Ontology for Media Resources 1.0 offers many possible relationships.
 * Should be more fine-grained for different media files.
 *
 * XML file associated (via Asset) which contains all extracted information.
 */
@Iri(MMM.MEDIA_INFO_BODY)
public interface MediaInfoBodyMMM extends BodyMMM {

}
