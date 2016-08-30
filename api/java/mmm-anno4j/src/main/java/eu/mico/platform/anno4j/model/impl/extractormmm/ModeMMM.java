package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.model.impl.ResourceObject;
import eu.mico.platform.anno4j.model.namespaces.MMM;
import org.openrdf.annotations.Iri;

import java.util.Set;

/**
 * Provenance class representing a ModeMMM in the MICO workflow.
 * A ModeMMM is the combination of a setting of an extractor and its parameters.
 */
@Iri(MMM.MODE)
public interface ModeMMM extends ResourceObject {

    @Iri(MMM.HAS_CONFIG_SCHEMA_URI)
    void setConfigSchemaUri(String schema);

    @Iri(MMM.HAS_CONFIG_SCHEMA_URI)
    String getConfigSchemaUri();

    @Iri(MMM.HAS_OUTPUT_SCHEMA_URI)
    void setOutputSchemaUri(String schema);

    @Iri(MMM.HAS_OUTPUT_SCHEMA_URI)
    String getOutputSchemaUri();

    @Iri(MMM.HAS_STRING_ID)
    void setStringId(String id);

    @Iri(MMM.HAS_STRING_ID)
    String getStringId();

    @Iri(MMM.HAS_DESCRIPTION)
    void setDescription(String description);

    @Iri(MMM.HAS_DESCRIPTION)
    String getDescription();

    @Iri(MMM.HAS_INPUT_DATA)
    void setInput(Set<InputMMM> input);

    @Iri(MMM.HAS_INPUT_DATA)
    Set<InputMMM> getInput();

    void addInput(InputMMM input);

    @Iri(MMM.HAS_OUTPUT_DATA)
    void setOutput(Set<OutputMMM> output);

    @Iri(MMM.HAS_OUTPUT_DATA)
    Set<OutputMMM> getOutput();

    void addOutput(OutputMMM output);

    @Iri(MMM.HAS_PARAM)
    void setParams(Set<ParamMMM> params);

    @Iri(MMM.HAS_PARAM)
    Set<ParamMMM> getParams();

    void addParam(ParamMMM param);
}
