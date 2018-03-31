/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
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
     * @param accessKey the new client key
     * @return the response
     */
    @POST
    @Path("/key")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setKey(@QueryParam("access_key") String accessKey) {
        if (accessKey != null && !accessKey.isEmpty()) {
            dbFileManager.setClientKey(accessKey);
            return Response.ok("OK").build();
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    /**
     * Update database from http://data.udger.com/{key}.
     *
     * @return the response
     */
    @POST
    @Path("/updatedata")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateData() {
        try {
            poolManager.updateDb(true);
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
    @POST
    @Path("/autoupdate")
    @Produces(MediaType.TEXT_PLAIN)
    public Response autoUpdate(@QueryParam("time") String time) {
        if (poolManager.scheduleUpdateDb(time)) {
            return Response.ok("OK").build();
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    /**
     * Upload db file. Possibly in gzip format.
     *
     * @param data the data
     * @return the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @POST
    @Path("/datafile")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDbFile(@Multipart(value="file", type="application/octet-stream") byte data[]) throws IOException {
        try {
            InputStream is = new ByteArrayInputStream(data);
            if (isGZipped(is)) {
                is = new GZIPInputStream(is);
            }
            dbFileManager.updateDbFileFromStream(is);
            poolManager.updateDb(false);
            LOG.info("Udger db uploaded and updated.");
            return Response.ok("OK").build();
        } catch (UdgerException e) {
            LOG.log(Level.WARNING, "uploadDbFile(): failed." + e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "uploadDbFile(): failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "isGZipped(): failed.", e);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }
}
