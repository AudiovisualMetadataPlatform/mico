package eu.mico.platform.anno4j.model;

import com.github.anno4j.model.*;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

import java.util.Set;

/**
 * Class represents a Part. A Part resembles an extractor step and consecutively an (intermediary)
 * result of an Item and its extraction chain.
 */
@Iri(MMM.PART)
public interface PartMMM extends ResourceMMM {

    /**
     * Gets http:www.w3.org/ns/oa#hasBody relationship.
     *
     * @return Value of http:www.w3.org/ns/oa#hasBody.
     */
    @Iri(MMM.HAS_BODY)
    Body getBody();

    /**
     * Sets http:www.w3.org/ns/oa#hasBody.
     *
     * @param body New value of http:www.w3.org/ns/oa#hasBody.
     */
    @Iri(MMM.HAS_BODY)
    void setBody(Body body);

    /**
     * Gets http:www.w3.org/ns/oa#hasTarget relationships.
     *
     * @return Values of http:www.w3.org/ns/oa#hasTarget.
     */
    @Iri(MMM.HAS_TARGET)
    Set<Target> getTarget();

    /**
     * Sets http:www.w3.org/ns/oa#hasTarget.
     *
     * @param targets New value of http:www.w3.org/ns/oa#hasTarget.
     */
    @Iri(MMM.HAS_TARGET)
    void setTarget(Set<Target> targets);

    /**
     * Gets the objects that were the semantic input for this Part.
     *
     * @return A set of objects that are used as semantic input for creating this Part.
     */
    @Iri(MMM.HAS_INPUT)
    Set<ResourceMMM> getInputs();

    /**
     * Sets the Set of objects that are the semantic input for this Part.
     *
     * @param inputs    The set of objects that form the semantic input for this Part
     */
    @Iri(MMM.HAS_INPUT)
    void setInputs(Set<ResourceMMM> inputs);

    /**
     * Adds a single object to the set of objects, that form the semantic input for this Part.
     *
     * @param input The object that is to be added to the set of objects, that form the semantic input for this part.
     */
    void addInput(ResourceMMM input);

    /**
     * Adds a http:www.w3.org/ns/oa#hasTarget relationship.
     *
     * @param target New http:www.w3.org/ns/oa#hasTarget relationship.
     */
    void addTarget(Target target);

    @Iri("http://www.w3.org/ns/oa#motivatedBy")
    Motivation getMotivatedBy();

    @Iri("http://www.w3.org/ns/oa#motivatedBy")
    void setMotivatedBy(Motivation var1);

    @Iri("http://www.w3.org/ns/oa#serializedBy")
    Agent getSerializedBy();

    @Iri("http://www.w3.org/ns/oa#serializedBy")
    void setSerializedBy(Agent var1);

    @Iri("http://www.w3.org/ns/oa#annotatedBy")
    Agent getAnnotatedBy();

    @Iri("http://www.w3.org/ns/oa#annotatedBy")
    void setAnnotatedBy(Agent var1);

    @Iri("http://www.w3.org/ns/oa#serializedAt")
    String getSerializedAt();

    @Iri("http://www.w3.org/ns/oa#serializedAt")
    void setSerializedAt(String var1);

    void setSerializedAt(int var1, int var2, int var3, int var4, int var5, int var6);

    @Iri("http://www.w3.org/ns/oa#annotatedAt")
    String getAnnotatedAt();

    @Iri("http://www.w3.org/ns/oa#annotatedAt")
    void setAnnotatedAt(String var1);

    void setAnnotatedAt(int var1, int var2, int var3, int var4, int var5, int var6);

    @Iri("http://www.w3.org/ns/oa#annotatesdAt")
    String getAnnotatedAts();

    @Iri("http://www.w3.org/ns/oa#annotatesdAt")
    void setAnnotatedAts(String var1);
}
