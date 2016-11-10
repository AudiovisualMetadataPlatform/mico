/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
