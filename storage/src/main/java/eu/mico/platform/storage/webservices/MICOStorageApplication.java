/**
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
package eu.mico.platform.storage.webservices;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.impl.StorageServiceHDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.HashSet;
import java.util.Set;

/**
 * MICO Storage Application
 *
 * @author Sergio Fernández
 */
public class MICOStorageApplication extends Application {

    private static Logger log = LoggerFactory.getLogger(MICOStorageApplication.class);

    private Set<Object> services;

    private StorageService storageService;

    public MICOStorageApplication(@Context ServletContext context) {
        super();

        String host = context.getInitParameter("mico.host") != null ? context.getInitParameter("mico.host") : "localhost";
        String user = context.getInitParameter("mico.user") != null ? context.getInitParameter("mico.user") : "mico";
        String pass = context.getInitParameter("mico.pass") != null ? context.getInitParameter("mico.pass") : "mico";

        log.info("initialising new MICO storage over {}...", host);

        services = new HashSet<>();
        try {
            storageService  = new StorageServiceHDFS(host, user, pass); //TODO configuration
            services.add(new StorageWebService(storageService));
        } catch (Exception e) {
            log.error("could not initialise MICO storage, services not available (message: {})", e.getMessage(), e);
        }
    }


    @Override
    public Set<Object> getSingletons() {
        return services;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

}
