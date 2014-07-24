package eu.mico.platform.broker.webservices;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MICOBrokerApplication extends Application {

    Set<Object> services;

    public MICOBrokerApplication() {
        super();


        // TODO: here we should also initialise the broker application itself
        services = new HashSet<>();
        services.add(new StatusWebService());
    }


    @Override
    public Set<Object> getSingletons() {
        return services;
    }

}
