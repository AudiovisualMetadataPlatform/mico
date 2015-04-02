package eu.mico.platform.persistence.impl.ProvenanceImpl;

import eu.mico.platform.persistence.impl.ModelPersistenceImpl.ModelPersistenceProvenanceImpl;
import eu.mico.platform.persistence.util.Ontology;
import org.openrdf.annotations.Iri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Iri(Ontology.SOFTWARE_AGENT_PROV)
public class AnnotationProvenance extends ModelPersistenceProvenanceImpl {

    /**
     * The unique ID for the extractor.
     */
    @Iri(Ontology.HAS_ID_MICO)
    private String id;

    /**
     * Defines the MIME type that the extractor is able to process.
     */
    @Iri(Ontology.REQUIRES_MICO)
    private String requires;

    /**
     * Counterpart to the mico:requires, this property supports the MIME type of the result that the extractor produces.
     */
    @Iri(Ontology.PROVIDES_MICO)
    private String provides;

    /**
     * Specifies the version of the extractor. Will be updated once a new version of the same extractor-type is
     * registered at the MICO platform.
     */
    @Iri(Ontology.HAS_VERSION_TYPE_MICO)
    private Double version;

    /**
     * Timestamp when the extractor was successfully registered at a given MICO platform.
     */
    @Iri(Ontology.GENERATED_AT_TIME_POV)
    private String generatedAtTime;

    /**
     * Indicates who registered the extractor at the given MICO platform.
     */
    @Iri(Ontology.WAS_GENERATED_BY_POV)
    private String generatedBy;

    /**
     * Timestamp when the extractor is unregistered from the given MICO platform.
     */
    @Iri(Ontology.INVALIDATED_AT_TIME_POV)
    private String invalidatedAt;

    /**
     * The name of the queue that the extractor listens to. This impacts the
     * RabbitMQ10 implementation of the MICO platform.
     */
    @Iri(Ontology.HAS_QUEUE_NAME)
    private String queueName;

    public AnnotationProvenance() {
    }

    public AnnotationProvenance(String id, String requires, String provides, Double version, Date generatedAtTime, String generatedBy, Date invalidatedAt, String queueName) {
        this.id = id;
        this.requires = requires;
        this.provides = provides;
        this.version = version;
        this.generatedAtTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(generatedAtTime);
        this.generatedBy = generatedBy;
        this.invalidatedAt = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(invalidatedAt);
        this.queueName = queueName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequires() {
        return requires;
    }

    public void setRequires(String requires) {
        this.requires = requires;
    }

    public String getProvides() {
        return provides;
    }

    public void setProvides(String provides) {
        this.provides = provides;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public Date getGeneratedAtTime() {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").parse(generatedAtTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void setGeneratedAtTime(Date generatedAtTime) {
        this.generatedAtTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(generatedAtTime);
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(Date generatedBy) {
        this.generatedBy = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(invalidatedAt);
    }

    public Date getInvalidatedAt() {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").parse(invalidatedAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void setInvalidatedAt(String invalidatedAt) {
        this.invalidatedAt = invalidatedAt;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
