package eu.mico.platform.broker.model;

import eu.mico.platform.persistence.model.ContentItem;
import org.openrdf.model.URI;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class Transition {

    private ContentItem item;
    private URI object;

    private TypeDescriptor stateStart;
    private TypeDescriptor stateEnd;
    private ServiceDescriptor service;

    public Transition(ContentItem item, URI object, TypeDescriptor stateStart, TypeDescriptor stateEnd, ServiceDescriptor service) {
        this.item = item;
        this.object = object;
        this.stateStart = stateStart;
        this.stateEnd = stateEnd;
        this.service = service;
    }


    public ContentItem getItem() {
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
}
