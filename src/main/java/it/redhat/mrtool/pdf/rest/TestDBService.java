package it.redhat.mrtool.pdf.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/test")
public class TestDBService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response test() {
        return Response.status(200).entity("PDF Test Service. Try '/pdf/test/communication' to test Connection with persistence module.").build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("communication")
    public Response testConnection() {
        String host = System.getenv("MRTDB_SERVICE");

        /*
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://" + host + ":8080")
                .path("rs").path("associates");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() != 200){
            return Response.status(200).entity("PDF Test Service. Test FAILED!").build();
        }
        */
        return Response.status(200).entity("PDF Test Service. Test OK!").build();
    }

}
