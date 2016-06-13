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
