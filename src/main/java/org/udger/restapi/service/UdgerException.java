/*
  UdgerParser - Java agent string parser based on Udger https://udger.com/products/local_parser

  author     The Udger.com Team (info@udger.com)
  copyright  Copyright (c) Udger s.r.o.
  license    GNU Lesser General Public License
  link       https://udger.com/products
*/
package org.udger.restapi.service;

/**
 * The Class UdgerException.
 */
public class UdgerException extends Exception {

    private static final long serialVersionUID = 1L;

    public UdgerException(String msg) {
        super(msg);
    }

}
