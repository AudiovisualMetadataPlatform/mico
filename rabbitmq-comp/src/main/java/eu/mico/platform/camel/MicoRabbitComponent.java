package eu.mico.platform.camel;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link MicoRabbitEndpoint}.
 */
public class MicoRabbitComponent extends UriEndpointComponent {
    
    public MicoRabbitComponent() {
        super(MicoRabbitEndpoint.class);
    }

    public MicoRabbitComponent(CamelContext context) {
        super(context, MicoRabbitEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new MicoRabbitEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
