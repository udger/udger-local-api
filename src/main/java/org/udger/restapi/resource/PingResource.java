package org.udger.restapi.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.udger.restapi.service.ParserStatistics;

@Path("ping")
public class PingResource {

    @Inject
    private ParserStatistics parserStatistics;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatistic() {
        parserStatistics.updateStatisticUA(0);
        return Response.ok().entity("OK").build();
    }
}