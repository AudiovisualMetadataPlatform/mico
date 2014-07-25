package eu.mico.platform.broker.model;

import eu.mico.platform.event.model.Event;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.util.Date;

/**
 * A representation to uniquely identify a service in the dependency graph. Currently just uses the URI and queue name
 * of the service.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ServiceDescriptor {

    private URI uri;
    private String queueName;
    private Date registrationTime;
    private String provides,requires;
    private String language;

    private int calls = 0;

    public ServiceDescriptor() {
        registrationTime = new Date();
    }

    public ServiceDescriptor(URI uri, String queueName, String provides, String requires) {
        this.uri = uri;
        this.queueName = queueName;
        registrationTime = new Date();
        this.provides = provides;
        this.requires = requires;
    }

    public ServiceDescriptor(Event.RegistrationEvent e) {
        this.uri = new URIImpl(e.getServiceId());
        this.queueName = e.getQueueName();
        registrationTime = new Date();
        this.provides = e.getProvides();
        this.requires = e.getRequires();

        switch (e.getLanguage()) {
            case JAVA:
                this.language = "Java";
                break;
            case CPP:
                this.language = "C++";
                break;
            case PYTHON:
                this.language = "Python";
                break;
        }
    }

    /**
     * Return the service ID of this service (a unique URI).
     *
     * @return
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Return the queue name used by this service
     *
     * @return
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Return the time when this service was registered
     *
     * @return
     */
    public Date getRegistrationTime() {
        return registrationTime;
    }

    /**
     * Get a representation of the type of output provided by this service
     * @return
     */
    public String getProvides() {
        return provides;
    }

    /**
     * Get a representation of the type of input provided by this service
     * @return
     */
    public String getRequires() {
        return requires;
    }

    /**
     * Get the implementation language this service has been implemented in (Java, C++ or Python)
     * @return
     */
    public String getLanguage() {
        return language;
    }

    public int getCalls() {
        return calls;
    }

    public void incCalls() {
        calls++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceDescriptor that = (ServiceDescriptor) o;

        if (queueName != null ? !queueName.equals(that.queueName) : that.queueName != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (queueName != null ? queueName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return uri.stringValue().substring(uri.stringValue().lastIndexOf("/")+1);
    }
}
