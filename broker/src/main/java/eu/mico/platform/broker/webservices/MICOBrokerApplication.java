package eu.mico.platform.broker.webservices;

import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.broker.impl.MICOBrokerImpl;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MICOBrokerApplication extends Application {

    private static Logger log = LoggerFactory.getLogger(MICOBrokerApplication.class);

    Set<Object> services;

    private MICOBroker broker;
    private EventManager manager;

    public MICOBrokerApplication(@Context ServletContext context) {
        super();

        String host = context.getInitParameter("mico.host") != null ? context.getInitParameter("mico.host") : "localhost";
        String user = context.getInitParameter("mico.user") != null ? context.getInitParameter("mico.user") : "mico";
        String pass = context.getInitParameter("mico.pass") != null ? context.getInitParameter("mico.pass") : "mico";

        if("localhost".equals(host)) {
            try {
                host = FileUtils.fileRead("/var/run/mico-ip").trim();
            } catch (IOException e) {
                log.warn("could not read MICO virtual server IP address");
            }
        }

        log.info("initialising new MICO broker for host {}", host);

        try {
            broker  = new MICOBrokerImpl(host, user, pass);
            manager = new EventManagerImpl(host, user, pass);
            manager.init();

            services = new HashSet<>();
            services.add(new StatusWebService(broker));
            services.add(new InjectionWebService(manager));
        } catch (IOException ex) {
            log.error("could not initialise MICO broker, services not available (message: {})", ex.getMessage());
            log.debug("Exception:",ex);
        }
    }


    @Override
    public Set<Object> getSingletons() {
        return services;
    }


    @Override
    protected void finalize() throws Throwable {
        manager.shutdown();

        super.finalize();
    }
}
