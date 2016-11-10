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

package eu.mico.platform.broker.api;

import eu.mico.platform.broker.model.v2.Transition;
import eu.mico.platform.broker.model.v2.TypeDescriptor;
import org.openrdf.model.URI;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface ItemState {

	boolean isFinalState();

	Set<Transition> getPossibleTransitions();
	public Map<URI, TypeDescriptor> getStates();
	public Map<String, Transition> getProgress();
	
	Date getCreated();
	
    public boolean hasError();
    public String getError();

}
