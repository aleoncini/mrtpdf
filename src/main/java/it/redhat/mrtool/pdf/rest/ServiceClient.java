package it.redhat.mrtool.pdf.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServiceClient {
    private static final Logger logger = LoggerFactory.getLogger("it.redhat.mrtool");
    String host = System.getenv("MRTDB_SERVICE");

    public String invoke(String url) {
        String targetUrl = "http://" + host + ":8080" + url;
        logger.info("[ServiceClient] invoking: " + targetUrl);
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(targetUrl);
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() != 200){
            logger.warn("[ServiceClient] call status: " + response.getStatus());
            logger.warn("[ServiceClient] call status: " + response.getStatusInfo().getReasonPhrase());
            return "{}";
        }
        return response.readEntity(String.class);
    }
}
