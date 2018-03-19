/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.resource;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.udger.restapi.service.DbFileManager;
import org.udger.restapi.service.PoolManager;
import org.udger.restapi.service.UdgerException;

/**
 * The Class SetResource.
 */
@Path("set")
public class SetResource {

    private static final Logger LOG =  Logger.getLogger(SetResource.class.getName());

    @Inject
    private DbFileManager dbFileManager;
    @Inject
    private PoolManager poolManager;

    /**
     * Sets the client key.
     *
     * @param key the new client key
     * @return the response
     */
    @GET
    @Path("/key/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setKey(@PathParam("key") String key) {
        if (key != null && !key.isEmpty()) {
            dbFileManager.setClientKey(key);
            return Response.ok("OK").build();
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    /**
     * Update database from http://data.udger.com/{key}.
     *
     * @return the response
     */
    @GET
    @Path("/updatedata")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateData() {
        try {
            poolManager.updateDb();
            LOG.info("Udger db updated.");
            return Response.ok("OK").build();
        } catch (UdgerException e) {
            LOG.log(Level.WARNING, "updateData(): failed." + e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "updateData(): failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Schedule periodic update to given time
     *
     * @param time the time
     * @return the response
     */
    @GET
    @Path("/autoupdate/{time}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response autoUpdate(@PathParam("key") String time) {
        if (poolManager.scheduleUpdateDb(time)) {
            return Response.ok("OK").build();
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

}
