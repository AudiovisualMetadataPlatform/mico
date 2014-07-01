package eu.mico.platform.persistence.model;

import org.openrdf.model.URI;

/**
 * A content part represents a part of a content item, e.g. an HTML document or an image, possibly together with
 * RDF metadata. The ContentPart API offers access to  both the metadata (in form of an OpenRDF Sesame repository)
 * and the content (in form of Input/Output streams)
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface ContentPart {

    /**
     *  Return the URI uniquely identifying this content part. The URI should be either a UUID or constructed in a way
     *  that it derives from the ContentItem this part belongs to.
      * @return
     */
    public URI getID();


    /**
     * Return the binary content of this ContentPart. Updates will be reflected in the underlying representation.
     * @return
     */
    public Content getContent();


    /**
     * Return the RDF metadata for this ContentPart. Updates will be reflected in the underlying representation.
     * @return
     */
    public Metadata getMetadata();

}
