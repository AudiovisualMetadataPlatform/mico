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
import eu.mico.platform.storage.model.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.util.*;

/**
 * Storage Web Service
 *
 * @author Sergio Fernández
 */
@Path("/rest")
public class StorageWebService {

    private static Logger log = LoggerFactory.getLogger(StorageWebService.class);

    private final StorageService storageService;

    public StorageWebService(StorageService storageService) {
        if (storageService == null) {
            throw new RuntimeException("Initialization error: storageService cannot be null");
        }
        this.storageService = storageService;
    }

    @GET
    @Path("/items")
    @Produces("application/json")
    public Collection<ContentItem> getItems() {
        return storageService.list();
    }

}
