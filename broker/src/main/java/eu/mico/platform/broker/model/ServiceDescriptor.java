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

        if (!queueName.equals(that.queueName)) return false;
        if (!uri.equals(that.uri)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + queueName.hashCode();
        return result;
    }
}
