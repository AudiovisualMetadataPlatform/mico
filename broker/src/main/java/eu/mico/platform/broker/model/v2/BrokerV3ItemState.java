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

package eu.mico.platform.broker.model.v2;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import eu.mico.platform.broker.api.ItemState;
import eu.mico.platform.broker.model.MICOJobStatus;

public class BrokerV3ItemState implements ItemState{
	
	MICOJobStatus s;
	
	public BrokerV3ItemState(MICOJobStatus s){
		this.s = s;
	}

	public boolean hasError() {
		return s.hasError();
	}
	

	public Date getCreated() {
		return s.getCreated();
	}

	@Override
	public boolean isFinalState() {
		return s.isFinished();
	}

	@Override
	@Deprecated
	public Set<Transition> getPossibleTransitions() {
		return new HashSet<Transition>();
	}

	@Override
	@Deprecated
	public Map<URI, TypeDescriptor> getStates() {
		return new HashMap<URI,TypeDescriptor>();
	}

	@Override
	@Deprecated
	public Map<String, Transition> getProgress() {
		return new HashMap<String,Transition>();
	}

	@Override
	public String getError() {
		return s.getErrorMessage();
	}
}
