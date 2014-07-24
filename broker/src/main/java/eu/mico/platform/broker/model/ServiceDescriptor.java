package eu.mico.platform.broker.model;

import org.openrdf.model.URI;

/**
 * A representation to uniquely identify a service in the dependency graph. Currently just uses the URI and queue name
 * of the service.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ServiceDescriptor {

    private URI uri;
    private String queueName;

    public ServiceDescriptor() {
    }

    public ServiceDescriptor(URI uri, String queueName) {
        this.uri = uri;
        this.queueName = queueName;
    }

    public URI getUri() {
        return uri;
    }

    public String getQueueName() {
        return queueName;
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
