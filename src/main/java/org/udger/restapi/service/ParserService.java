/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.udger.parser.UdgerIpResult;
import org.udger.parser.UdgerParser;
import org.udger.parser.UdgerUaResult;

/**
 * The Class ParserService.
 */
@ApplicationScoped
public class ParserService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ParserPool parserPool;

    /**
     * Parses the user agent string
     *
     * @param ua the ua
     * @return the udger ua result
     * @throws SQLException the SQL exception
     */
    public UdgerUaResult parseUa(String ua) throws SQLException {
        UdgerParser parser = null;
        try {
            parser = parserPool.borrowParser();
            if (parser != null) {
                return parser.parseUa(ua);
            }
        }
        finally {
            parserPool.returnParser(parser);
        }
        return null;
    }

    /**
     * Parses the ip.
     *
     * @param ip the ip
     * @return the udger ip result
     * @throws SQLException the SQL exception
     * @throws UnknownHostException the unknown host exception
     */
    public UdgerIpResult parseIp(String ip) throws SQLException, UnknownHostException {
        UdgerParser parser = null;
        try {
            parser = parserPool.borrowParser();
            if (parser != null) {
                return parser.parseIp(ip);
            }
        }
        finally {
            parserPool.returnParser(parser);
        }
        return null;
    }
}
