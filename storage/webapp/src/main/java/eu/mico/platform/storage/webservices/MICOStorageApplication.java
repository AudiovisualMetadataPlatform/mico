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
package eu.mico.platform.storage.webservices;

import eu.mico.platform.storage.api.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.*;

/**
 * MICO Storage Application
 *
 * @author Sergio Fernández
 * @author Horst Stadler
 */
public class MICOStorageApplication extends Application {

    private static Logger log = LoggerFactory.getLogger(MICOStorageApplication.class);

    private Set<Object> services;

    private StorageService storageService;

    public MICOStorageApplication(@Context ServletContext context) throws Exception{
        super();

        String storageProviderName = getInitParameter(context, "mico.storage.provider", "eu.mico.platform.storage.impl.StorageServiceHDFS");

        HashMap<String, String> params = filterInitParameters(context, "mico.storage.");
        log.info("Initialising new MICO storage {} with parameters: {}", storageProviderName, params.toString());

        try {
            Class storageProviderClass = Class.forName(storageProviderName);
            storageService = (StorageService)storageProviderClass.getDeclaredConstructor(params.getClass()).newInstance(params);
        }catch (ClassNotFoundException cnfe)
        {
            log.error("MICO storage initialization error: Could not find storage provider class {}", storageProviderName);
            throw cnfe;
        }
        catch (NoSuchMethodException nsme)
        {
            log.error("MICO storage initialization error: Could not instance storage provider class {}, as it does not provide a proper constructor", storageProviderName);
            throw nsme;
        }
        catch (Exception e) {
            log.error("MICO storage initialization error: Could not instance storage provider class {} (message: {})", storageProviderName, e.getMessage());
            throw e;
        }

        services = new HashSet<>();
        try {
            services.add(new StorageWebService(storageService));
        } catch (Exception e) {
            log.error("Could not initialise MICO storage, services not available (message: {})", e.getMessage(), e);
            throw e;
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

    private String getInitParameter(ServletContext context, String parameterName, String defaultValue) {
        return context.getInitParameter(parameterName) != null ? context.getInitParameter(parameterName) : defaultValue;
    }

    private HashMap<String, String> filterInitParameters(ServletContext context, String parameterPrefix) {
        HashMap<String, String> parameterList = new HashMap<String, String>();
        Enumeration<String> parameterNames = context.getInitParameterNames();
        while(parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.startsWith(parameterPrefix)) {
                if (context.getInitParameter(parameterName) != null) {
                    parameterList.put(parameterName.substring(parameterPrefix.length()), context.getInitParameter(parameterName));
                }
            }
        }
        return parameterList;
    }

}
