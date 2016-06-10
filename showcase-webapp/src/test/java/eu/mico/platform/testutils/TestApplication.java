package eu.mico.platform.testutils;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TestApplication extends Application {

    public TestApplication() {
        super();
    }

    public static Set<Object> webservices;

    @Override
    public Set<Object> getSingletons() {
        return webservices;
    }
}
