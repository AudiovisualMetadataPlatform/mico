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

package eu.mico.platform.reco.Resources;

import eu.mico.platform.anno4j.model.fam.LinkedEntityBody;

import java.net.URI;

public class EntityInfo extends SimpleExtractionResult {

    private URI reference;
    private String label;


    public EntityInfo(LinkedEntityBody b) {

        String uriString = b.getEntity().getResource().toString();
        try {
            uriString = b.getEntity().getResource().toString();

            // remove invisible control characters
            uriString = uriString.replaceAll("\\p{C}", "");

            this.reference = URI.create(uriString);
        } catch (IllegalArgumentException e) {
            this.reference = URI.create("http://dbpedia.org/page/Illegal_URI");
        }
        this.label = b.getEntity().toString();

    }

    public URI getReference() {
        return reference;
    }


    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityInfo that = (EntityInfo) o;

        if (!reference.equals(that.reference)) return false;
        return label != null ? label.equals(that.label) : that.label == null;

    }

    @Override
    public int hashCode() {
        int result = reference.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EntityInfo{" +
                "reference=" + reference +
                ", label='" + label + '\'' +
                '}';
    }
}
