/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
