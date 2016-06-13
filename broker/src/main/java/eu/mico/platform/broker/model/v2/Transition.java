/**
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
package eu.mico.platform.broker.model.v2;

import eu.mico.platform.persistence.model.Item;
import org.openrdf.model.URI;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class Transition {

    private Item item;
    private URI object;

    private TypeDescriptor stateStart;
    private TypeDescriptor stateEnd;
    private ServiceDescriptor service;
    private float progress = -1.0f;

    public Transition(Item item, URI object, TypeDescriptor stateStart, TypeDescriptor stateEnd, ServiceDescriptor service) {
        this.item = item;
        this.object = object;
        this.stateStart = stateStart;
        this.stateEnd = stateEnd;
        this.service = service;
    }


    public Item getItem() {
        return item;
    }

    public URI getObject() {
        return object;
    }

    public TypeDescriptor getStateStart() {
        return stateStart;
    }

    public TypeDescriptor getStateEnd() {
        return stateEnd;
    }

    public ServiceDescriptor getService() {
        return service;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "item=" + item.getURI() +
                ", object=" + object +
                ", stateStart=" + stateStart +
                ", stateEnd=" + stateEnd +
                ", service=" + service.getUri() +
                '}';
    }


    public float getProgress() {
        return progress;
    }


    public void setProgress(float progress) {
        this.progress = progress;
    }
}
