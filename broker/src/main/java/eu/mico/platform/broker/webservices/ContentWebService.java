package eu.mico.platform.broker.webservices;

import eu.mico.platform.broker.api.MICOBroker;
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
    private final MICOBroker broker;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;

    public ContentWebService(EventManager eventManager, MICOBroker broker, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.broker = broker;
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
