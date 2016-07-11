package eu.mico.platform.reco.model;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 *    Value-type class for jackson Mapping of Prediction-IO Event api.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PioEventData {

    public String event;
    public String entityId;
    public String entityType;
    public String targetEntityId;
    public String targetEntityType;

    public static PioEventData fromJSON(String jsonRepr) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonRepr, PioEventData.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PioEventData that = (PioEventData) o;

        return new EqualsBuilder()
                .append(event, that.event)
                .append(entityId, that.entityId)
                .append(entityType, that.entityType)
                .append(targetEntityId, that.targetEntityId)
                .append(targetEntityType, that.targetEntityType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(event)
                .append(entityId)
                .append(entityType)
                .append(targetEntityId)
                .append(targetEntityType)
                .toHashCode();
    }

}
