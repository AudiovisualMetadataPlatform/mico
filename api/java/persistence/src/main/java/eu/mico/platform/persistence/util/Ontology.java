package eu.mico.platform.persistence.util;

public class Ontology {

    /**
     * MICO (mico:)
     */
    
    public final static String NS_MICO = "http://www.mico-project.eu/ns/platform/1.0/schema#";
    
    public final static String HAS_BODY_MICO = NS_MICO + "hasBody";

    public final static String HAS_CONFIDENCE_MICO = NS_MICO + "hasConfidence";

    public final static String HAS_CONTENT_MICO = NS_MICO + "hasContent";

    public final static String HAS_CONTENT_PART_MICO = NS_MICO + "hasContentPart";

    public final static String HAS_ID_MICO = NS_MICO + "hasId";

    public final static String HAS_LOCATION_MICO = NS_MICO + "hasLocation";

    public final static String HAS_QUEUE_NAME = NS_MICO + "hasQueueName";

    public final static String HAS_REDEFINED_TYPE_MICO = NS_MICO + "hasRefinedType";

    public final static String HAS_VERSION_TYPE = NS_MICO + "hasVersion";

    public final static String PROVIDES_MICO = NS_MICO + "provides";

    public final static String REQUIRES_MICO = NS_MICO + "requires";
    
    public final static String COLORLAYOUT_BODY_MICO = NS_MICO + "ColorLayoutBody";
    
    public final static String CONTENT_PART_MICO = NS_MICO + "ContentPart";

    /**
     * Open Annotation (oa:)
     */
    
    public final static String NS_OA = "http://www.w3.org/ns/oa#";

    public final static String ANNOTATION_OA = NS_OA + "annotation";
    
    public final static String SPECIFIC_RESOURCE_OA = NS_OA + "SpecificResource";

    public final static String HAS_BODY_OA = NS_OA + "hasBody";

    public final static String HAS_TARGET_OA = NS_OA + "hasTarget";

    public final static String ANNOTATED_BY_OA = NS_OA + "annotatedBy";

    public final static String ANNOTATED_AT_OA = NS_OA + "annotatedAt";

    public final static String SERIALIZED_BY_OA = NS_OA + "serializedBy";

    public final static String SERIALIZED_AT_OA = NS_OA + "serializedAt";

    public final static String HAS_SELECTOR_OA = NS_OA + "hasSelector";

    public final static String HAS_SOURCE_OA = NS_OA + "hasSource";



    /**
     * RDF (rdf:)
     */

    public final static String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public final static String TYPE_RDF = NS_RDF + "type";

    /**
     * Dublin Core 
     */

    public final static String NS_DCTERMS = "http://purl.org/dc/dcmitype/";
    
    public final static String IMAGE_DCTERMS = NS_DCTERMS + "Image";

    /**
     * Provenance (prov:) 
     */
    
    public final static String NS_PROV = "http://www.w3.org/ns/prov/";
    
    public final static String SOFTWARE_AGENT_PROV = NS_PROV + "SoftwareAgent";

    /**
     * Friend of a Friend (foaf:) 
     */
    
    public final static String NS_FOAF = "http://xmlns.com/foaf/0.1/";
    
    public final static String NAME_FOAF = NS_FOAF + "name";
}

