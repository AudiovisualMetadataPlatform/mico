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

package eu.mico.platform.demo;

import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 06.10.15.
 */
@Path("/content")
public class ContentWebService {

    private enum ResourceType {
        ContentItem,
        ContentPart,
        Annotation,
        SpecificSelector,
        None
    }

    private final EventManager eventManager;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;

    public ContentWebService(EventManager eventManager, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
    }

    @GET
    @Path(".+")
    public Response getByUrl(
            @Context UriInfo uriInfo,
            @QueryParam("crop") @DefaultValue("true") boolean crop
    ) {
        return getById(uriInfo.getRequestUri().toString(), crop);
    }

    @GET
    public Response getById(
            @QueryParam("uri") String uriString,
            @QueryParam("crop") @DefaultValue("true") boolean crop
    ) {

        ResourceType resourceType = getResourceType(uriString);

        //TODO implement

        switch(resourceType) {
            case ContentItem:
                break;
            case ContentPart:
                break;
            case Annotation:
                break;
            case SpecificSelector:
                break;
        }

        return Response.noContent().entity("Service is not yet implemented").build();
    }

    public ResourceType getResourceType(String uriString) {

        return ResourceType.None;
    }


}
