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
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Storage Web Service
 *
 * @author Sergio Fernández
 * @author Horst Stadler
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

    //TODO: RESTeasy does not support HTTP Range header in combination with streams.
    @GET
    @Path("/item/{contentId:.*}")
    public InputStream getItem(@PathParam("contentId") String contentId) throws IOException, URISyntaxException{
        return storageService.getInputStream(new URI(contentId));
    }

    @PUT
    @Path("/item/{contentId:.*}")
    @Consumes("application/octet-stream")
    public Response storeItem(@PathParam("contentId") String contentId, InputStream is) throws  IOException, URISyntaxException {
        OutputStream os = storageService.getOutputStream(new URI(contentId));
        IOUtils.copy(is, os);
        is.close();
        os.close();
        return Response.noContent().build();
    }

}
