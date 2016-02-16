package eu.mico.platform.reco;


import eu.mico.platform.broker.api.MICOBroker;
import eu.mico.platform.event.api.EventManager;
import eu.mico.platform.persistence.api.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/reco")
public class RecoWebService {

    private static final Logger log = LoggerFactory.getLogger(RecoWebService.class);

    private final EventManager eventManager;
    private final MICOBroker broker;
    private final String marmottaBaseUri;
    private final PersistenceService persistenceService;


    public RecoWebService(EventManager eventManager, MICOBroker broker, String marmottaBaseUri) {
        this.eventManager = eventManager;
        this.broker = broker;
        this.marmottaBaseUri = marmottaBaseUri;
        this.persistenceService = eventManager.getPersistenceService();
    }


    @GET
    @Path("/testcall")
    @Produces("text/plain")
    public Response getTestResponse() {
        return Response.ok("Trallala").build();
    }

}
