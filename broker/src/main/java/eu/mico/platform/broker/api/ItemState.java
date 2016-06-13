package eu.mico.platform.broker.api;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import eu.mico.platform.broker.model.v2.Transition;
import eu.mico.platform.broker.model.v2.TypeDescriptor;

public interface ItemState {

	boolean isFinalState();

	Set<Transition> getPossibleTransitions();
	public Map<URI, TypeDescriptor> getStates();
	public Map<String, Transition> getProgress();
	
	Date getCreated();
	
    public boolean hasError();
    public String getError();

}
