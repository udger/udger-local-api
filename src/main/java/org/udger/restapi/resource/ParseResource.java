/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.resource;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.udger.parser.UdgerIpResult;
import org.udger.parser.UdgerUaResult;
import org.udger.restapi.service.ParserService;
import org.udger.restapi.service.ParserStatistics;
import org.udger.restapi.service.UdgerException;

/**
 * The Class ParseResource.
 */
@Path("parse")
public class ParseResource {

    private static final Logger LOG =  Logger.getLogger(ParseResource.class.getName());

    @Inject
    private ParserService parserService;

    @Inject
    private ParserStatistics parserStatistics;

    /**
     * Parses the ua
     *
     * @param ua the ua
     * @return the response
     */
    @GET
    @Path("/ua/{ua:.+}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response parseUa(@PathParam("ua") String ua) {

        long tm = System.nanoTime();

        try {
            JsonObjectBuilder uaJson = doParseUa(ua);

            if (uaJson == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            return Response.ok(uaJson.build().toString()).build();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "parseUa(): sql failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (UdgerException e) {
            LOG.log(Level.WARNING, "parseUa(): failed." + e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "parseUa(): failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            parserStatistics.updateStatisticUA(System.nanoTime() - tm);
        }
    }

    /**
     * Parses the ua / ip
     *
     * @param ua the ua
     * @param ip the ip
     * @return the response
     */
    @GET
    @Path("/ip/{ip:.+}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response parseIp(@PathParam("ip") String ip) {

        long tm = System.nanoTime();

        try {
            JsonObjectBuilder ipJson = doParseIp(ip);

            if (ipJson == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            return Response.ok(ipJson.build().toString()).build();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "parseIp(): sql failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (UdgerException e) {
            LOG.log(Level.WARNING, "parseIP(): failed." + e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (UnknownHostException e) {
            LOG.log(Level.WARNING, "parseIp(): host ip failed.", e);
            return Response.status(Status.BAD_REQUEST).entity("error: unkown host.").build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "parseIp(): failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            parserStatistics.updateStatisticIP(System.nanoTime() - tm);
        }
    }
    /**
     * Parses the ua / ip
     *
     * @param ua the ua
     * @param ip the ip
     * @return the response
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response parseUaIp(@QueryParam("ua") String ua, @QueryParam("ip") String ip) {

        long tm = System.nanoTime();

        try {
            JsonObjectBuilder uaJson = doParseUa(ua);
            JsonObjectBuilder ipJson = doParseIp(ip);

            if (uaJson == null && ipJson == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

            if (uaJson != null) {
                jsonBuilder.add("user_agent", uaJson);
            }

            if (ipJson != null) {
                jsonBuilder.add("ip_address", ipJson);
            }

            return Response.ok(jsonBuilder.build().toString()).build();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "parseUaIp(): sql failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (UdgerException e) {
            LOG.log(Level.WARNING, "parseUaIP(): failed." + e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (UnknownHostException e) {
            LOG.log(Level.WARNING, "parseUaIp(): host ip failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "parseUaIp(): failed.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        finally {
            long dtm = System.nanoTime() - tm;
            parserStatistics.updateStatisticUA(dtm);
            parserStatistics.updateStatisticIP(dtm);
        }
    }

    private JsonObjectBuilder doParseUa(String ua) throws SQLException, UdgerException {

        if (ua != null && ua.length() > 0) {

            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

            UdgerUaResult uaResult = parserService.parseUa(ua);

            jsonBuilder.add("ua_string", ua)
                .add("ua_class", uaResult.getUaClass())
                .add("ua_class_code",uaResult.getUaClassCode())
                .add("ua", uaResult.getUa())
                .add("ua_engine", uaResult.getUaEngine())
                .add("ua_version", uaResult.getUaVersion())
                .add("ua_version_major", uaResult.getUaVersionMajor())
                .add("ua_uptodate_current_version", uaResult.getUaUptodateCurrentVersion())
                .add("ua_family", uaResult.getUaFamily())
                .add("ua_family_code", uaResult.getUaFamilyCode())
                .add("ua_family_homepage", uaResult.getUaFamilyHomepage())
                .add("ua_family_vendor", uaResult.getUaFamilyVendor())
                .add("ua_family_vendor_code", uaResult.getUaFamilyVendorCode())
                .add("ua_family_vendor_homepage", uaResult.getUaFamilyVendorHomepage())
                .add("ua_family_icon", uaResult.getUaFamilyIcon())
                .add("ua_family_icon_big", uaResult.getUaFamilyIconBig())
                .add("ua_family_info_url", uaResult.getUaFamilyInfoUrl())
                .add("ua_engine", uaResult.getUaEngine())
                .add("os", uaResult.getOs())
                .add("os_code", uaResult.getOsCode())
                .add("os_homepage", uaResult.getOsHomePage())
                .add("os_icon", uaResult.getOsIcon())
                .add("os_icon_big", uaResult.getOsIconBig())
                .add("os_info_url", uaResult.getOsInfoUrl())
                .add("os_family", uaResult.getOsFamily())
                .add("os_family_code", uaResult.getOsFamilyCode())
                .add("os_family_vendor", uaResult.getOsFamilyVendor())
                .add("os_family_vendor_code", uaResult.getOsFamilyVendorCode())
                .add("os_family_vendor_homepage", uaResult.getOsFamilyVedorHomepage())
                .add("device_class", uaResult.getDeviceClass())
                .add("device_class_code", uaResult.getDeviceClassCode())
                .add("device_class_icon", uaResult.getDeviceClassIcon())
                .add("device_class_icon_big", uaResult.getDeviceClassIconBig())
                .add("device_class_info_url", uaResult.getDeviceClassInfoUrl())
                .add("device_brand", uaResult.getDeviceBrand())
                .add("device_brand_code", uaResult.getDeviceBrandCode())
                .add("device_brand_homepage", uaResult.getDeviceBrandHomepage())
                .add("device_brand_icon", uaResult.getDeviceBrandIcon())
                .add("device_brand_icon_big", uaResult.getDeviceBrandIconBig())
                .add("device_marketname", uaResult.getDeviceMarketname())
                .add("device_brand_info_url", uaResult.getDeviceBrandInfoUrl())
                .add("crawler_last_seen", uaResult.getCrawlerLastSeen())
                .add("crawler_category", uaResult.getCrawlerCategory())
                .add("crawler_category_code", uaResult.getCrawlerCategoryCode())
                .add("crawler_respect_robotstxt", uaResult.getCrawlerRespectRobotstxt());

            return jsonBuilder;
        }
        return null;
    }

    private JsonObjectBuilder doParseIp(String ip) throws UnknownHostException, SQLException, UdgerException {

        if (ip != null && ip.length() > 0) {

            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

            UdgerIpResult ipResult = parserService.parseIp(ip);

            jsonBuilder.add("ip", ip)
                .add("ip_ver", ipResult.getIpVer())
                .add("ip_classification", ipResult.getIpClassification())
                .add("ip_classification_code", ipResult.getIpClassificationCode())
                .add("ip_hostname", ipResult.getIpHostname())
                .add("ip_last_seen", ipResult.getIpLastSeen())
                .add("ip_country", ipResult.getIpCountry())
                .add("ip_country_code", ipResult.getIpCountryCode())
                .add("ip_city", ipResult.getIpCity())
                .add("crawler_name", ipResult.getCrawlerName())
                .add("crawler_ver", ipResult.getCrawlerVer())
                .add("crawler_ver_major", ipResult.getCrawlerVerMajor())
                .add("crawler_family", ipResult.getCrawlerFamily())
                .add("crawler_family_code", ipResult.getCrawlerFamilyCode())
                .add("crawler_family_homepage", ipResult.getCrawlerFamilyHomepage())
                .add("crawler_family_vendor", ipResult.getCrawlerFamilyVendor())
                .add("crawler_family_vendor_code", ipResult.getCrawlerFamilyVendorCode())
                .add("crawler_family_vendor_homepage", ipResult.getCrawlerFamilyVendorHomepage())
                .add("crawler_family_icon", ipResult.getCrawlerFamilyIcon())
                .add("crawler_family_info_url", ipResult.getCrawlerFamilyInfoUrl())
                .add("crawler_last_seen", ipResult.getCrawlerLastSeen())
                .add("crawler_category", ipResult.getCrawlerCategory())
                .add("crawler_category_code", ipResult.getCrawlerCategoryCode())
                .add("crawler_respect_robotstxt", ipResult.getCrawlerRespectRobotstxt())
                .add("datacenter_name", ipResult.getDataCenterName())
                .add("datacenter_name_code", ipResult.getDataCenterNameCode())
                .add("datacenter_homepage", ipResult.getDataCenterHomePage());
            return jsonBuilder;
        }
        return null;
    }

}
