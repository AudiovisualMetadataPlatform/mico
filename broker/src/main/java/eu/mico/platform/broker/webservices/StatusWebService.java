package eu.mico.platform.broker.webservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@Path("/status")
public class StatusWebService {

    private static Logger log = LoggerFactory.getLogger(StatusWebService.class);



    @GET
    public Response getStatus() {
        return Response.status(Response.Status.OK).build();
    }

}
