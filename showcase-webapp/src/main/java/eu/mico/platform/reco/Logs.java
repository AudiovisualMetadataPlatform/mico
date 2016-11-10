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

package eu.mico.platform.reco;

import org.apache.commons.io.input.ReversedLinesFileReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("/logs")
@Produces(MediaType.TEXT_PLAIN)
public class Logs {


    @GET
    @Path("/{logname}")
    public Response getLog(@PathParam("logname") String logname) {

        Map<String, String> pathMapping = new HashMap<>();
        pathMapping.put("catalina", "/var/log/tomcat7/catalina.out");


        if (pathMapping.containsKey((logname))) {

            String filePath = pathMapping.get(logname);

            try (ReversedLinesFileReader rlfr = new ReversedLinesFileReader(new File(filePath))) {

                StringBuilder response = new StringBuilder();

                for (int i = 0; i < 500; i++) {
                    response.insert(0, rlfr.readLine() + "\n");
                }

                return Response.ok(response.toString()).build();


            } catch (IOException e) {
                return Response.serverError().build();
            }
        }

        return Response.serverError().build();

    }


}
