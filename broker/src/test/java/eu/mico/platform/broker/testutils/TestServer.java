package eu.mico.platform.broker.testutils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.HashSet;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class TestServer {

    private Server server;

    public TestServer() {
        server = new Server(0);
        String webappFolder = TestServer.class.getClassLoader().getResource("webapp").getPath();
        WebAppContext context = new WebAppContext(webappFolder,"/");
        server.setHandler(context);
        TestApplication.webservices = new HashSet<>();
    }

    public void addWebservice(Object o) {
        TestApplication.webservices.add(o);
    }

//    public String getUrl() {
//        return "http://localhost:" + server.getConnectors()[0].getLocalPort() + "/";
//    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        if(server != null) server.stop();
    }

}
