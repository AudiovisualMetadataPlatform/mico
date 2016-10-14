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
package eu.mico.platform.broker.api;

import eu.mico.platform.broker.api.rest.WorkflowInfo;
import eu.mico.platform.broker.model.MICOJobStatus;
import eu.mico.platform.broker.model.v2.ServiceGraph;
import eu.mico.platform.broker.model.MICOJob;
import eu.mico.platform.persistence.api.PersistenceService;

import java.util.Map;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface MICOBroker {
    /**
    * The value can be: <br>
    * <b>ONLINE</b> - (all extractors registered and connected <br>
    * <b>RUNNABLE</b> - (all extractors registered, but at least one is not connected.
    * The missing extractors can still be started by the broker <br>
    * <b>UNAVAILABLE</b> - all extractors registered, but at least one is deployed <br>
    * <b>BROKEN</b> - at least one extractor is not registered anymore <br>
    */
    public enum WorkflowStatus {
        ONLINE, RUNNABLE, UNAVAILABLE, BROKEN, PROCESSING;

        @Override
        public String toString() {
            switch (this) {
            case ONLINE:       return "ONLINE";
            case RUNNABLE:     return "RUNNABLE";
            case UNAVAILABLE:  return "UNAVAILABLE";
            case BROKEN:       return "BROKEN";
            default:
                throw new IllegalArgumentException();
            }
        }

    }

    /**
    *
    * possible status values are: <br>
    * <dl>
    * <dt>CONNECTED</dt> <dd>a running instance is connected </dd>
    * <dt>DEPLOYED</dt>  <dd>the extractor is registered but no running instance is connected</dd>
    *                    <dd>(The missing extractors can probably be started by the broker) </dd>
    * <dt>NOT_DEPLOYED</dt> <dd> the extractor is registered but no running instance is connected and the broker is not able to start one </dd>
    * <dt>UNREGISTERED</dt> <dd> an extractor with this id and version is not registered (unknown) to the system </dd>
    * </dl>
    */
    public enum ExtractorStatus {
        /**
         * a running instance is connected
         */
        CONNECTED,
        /**
         * the extractor is registered but no running instance is connected <br>
         * (The missing extractors can probably be started by the broker)
         */
        DEPLOYED,
        /**
         * the extractor is registered but no running instance is connected and
         * the broker is not able to start one
         */
        NOT_DEPLOYED,
        /**
         * an extractor with this id and version is not registered (unknown) to
         * the system
         */
        UNREGISTERED;

        @Override
        public String toString() {
          switch (this) {
            case CONNECTED:     return "CONNECTED";
            case DEPLOYED:      return "DEPLOYED";
            case NOT_DEPLOYED:  return "NOT_DEPLOYED";
            case UNREGISTERED:  return "UNREGISTERED";
            default:
                throw new IllegalArgumentException();
          }
        }

    }

    ServiceGraph getDependencies();

    Map<String, ItemState> getStates();

    PersistenceService getPersistenceService();

    WorkflowInfo getRouteStatus(String camelRoute);

    
    void addMICOCamelJobStatus(MICOJob job,MICOJobStatus jobState);
	MICOJobStatus getMICOCamelJobStatus(MICOJob job);
	
    /**
     * This function is here *only* for handling requests by the old GUI. 
     *
     * @deprecated use {@link #getMICOCamelJobStatus(MICOJob job)} instead.  
     */
    @Deprecated
	 Map<String, ItemState> getItemStatesFromCamel();
}
