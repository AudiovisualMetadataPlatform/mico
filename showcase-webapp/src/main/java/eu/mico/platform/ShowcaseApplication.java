/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.platform;

import eu.mico.platform.anno4j.querying.MICOQueryHelperMMM;
import eu.mico.platform.demo.ContentWebService;
import eu.mico.platform.demo.DemoWebService;
import eu.mico.platform.demo.DownloadWebService;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.event.impl.EventManagerImpl;
import eu.mico.platform.reco.EchoService;
import eu.mico.platform.reco.NerService;
import eu.mico.platform.reco.RecoWebService;
import eu.mico.platform.reco.Resources.NERQuery;
import eu.mico.platform.reco.Videos;
import eu.mico.platform.zooniverse.AnimalDetectionWebService;
import eu.mico.platform.zooniverse.TextAnalysisWebService;
import eu.mico.platform.zooniverse.util.BrokerServices;
import org.codehaus.plexus.util.FileUtils;
import org.openrdf.OpenRDFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class ShowcaseApplication extends Application {

    private static Logger log = LoggerFactory.getLogger(ShowcaseApplication.class);

    Set<Object> services;

    private EventManager manager;

    public ShowcaseApplication(@Context ServletContext context) {
        super();

        String host = context.getInitParameter("mico.host") != null ? context.getInitParameter("mico.host") : "mico-platform";
        String user = context.getInitParameter("mico.user") != null ? context.getInitParameter("mico.user") : "mico";
        String pass = context.getInitParameter("mico.pass") != null ? context.getInitParameter("mico.pass") : "mico";
        String brokerBaseUri = context.getInitParameter("mico.brokerBaseUri") != null ? context.getInitParameter("mico.brokerBaseUri") : "http://" + host + ":8080/broker";
        while (brokerBaseUri.endsWith("/")) {
            brokerBaseUri = brokerBaseUri.substring(0, brokerBaseUri.length() - 1);
        }
        String marmottaBaseUri = context.getInitParameter("mico.marmottaBaseUri") != null ? context.getInitParameter("mico.marmottaBaseUri") : "http://" + host + ":8080/marmotta";
        while (marmottaBaseUri.endsWith("/")) {
            marmottaBaseUri = marmottaBaseUri.substring(0, marmottaBaseUri.length() - 1);
        }


        String mediaDirectory = context.getInitParameter("mico-demo.mediaDirectory") != null ? context.getInitParameter("mico-demo.mediaDirectory") : "/tmp/mico-demo-media";
        String mediaUrl = context.getInitParameter("mico-demo.mediaUrl") != null ? context.getInitParameter("mico-demo.mediaUrl") : "http://" + host + ":8080/mico-demo-media";
        while (mediaUrl.endsWith("/")) {
            mediaUrl = mediaUrl.substring(0, mediaUrl.length() - 1);
        }

        if ("localhost".equals(host)) {
            try {
                host = FileUtils.fileRead("/var/run/mico-ip").trim();
            } catch (IOException e) {
                log.warn("could not read MICO virtual server IP address");
            }
        }

        log.info("initialising new MICO broker for host {}", host);

        try {
            manager = new EventManagerImpl(host, user, pass);
            Thread.sleep(20 * 1000);                 //Give the broker a chance to start up.
            manager.init();

            BrokerServices brokerServices = new BrokerServices(brokerBaseUri, marmottaBaseUri);

            services = new HashSet<>();

            MICOQueryHelperMMM mqh = NERQuery.getMicoQueryHelper();

            //WP5
            services.add(new RecoWebService(manager, marmottaBaseUri));
            services.add(new EchoService());
            services.add(new Videos(mqh));
            services.add(new NerService());

            // Zooniverse
            services.add(new AnimalDetectionWebService(manager, marmottaBaseUri, brokerServices));
            services.add(new TextAnalysisWebService(manager, marmottaBaseUri, brokerServices));

            //Demo
            services.add(new DownloadWebService(manager, marmottaBaseUri, mediaDirectory, mediaUrl));
            services.add(new ContentWebService(manager, marmottaBaseUri));
            services.add(new DemoWebService(manager, marmottaBaseUri, brokerServices));

        } catch (IOException ex) {
            log.error("could not initialise MICO broker, services not available (message: {})", ex.getMessage());
            log.debug("Exception:", ex);
        } catch (URISyntaxException ex) {
            log.error("could not initialise MICO broker, invalid URI (message: {})", ex.getMessage());
            log.debug("Exception:", ex);
        } catch (TimeoutException ex) {
            log.error("could not fetch Marmotta and storage configuration, request timed out (message: {})", ex.getMessage());
            log.debug("Exception:", ex);
        } catch (InterruptedException ex) {
            log.error("wait for broker got interrupted (message: {})", ex.getMessage());
            log.debug("Exception:", ex);
            Thread.currentThread().interrupt();
        } catch (OpenRDFException ex) {
            log.error("could not initialize Anno4j (message: {})", ex.getMessage());
            log.debug("Exception:", ex);
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
