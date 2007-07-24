/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceIdentifier;

/**
 * RequestFactory helps create REST request from HttpServletRequest
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RequestFactory {
  
  /* 
   * Class has only static methods.
   */
  private RequestFactory() {}

  /**
   * Create REST request.
   * @param httpRequest HTTP request
   * @return REST request REST request
   * @throws IOException Input/Output Exception
   */
  public static Request createRequest(HttpServletRequest httpRequest) throws IOException {
    String pathInfo = httpRequest.getPathInfo();
    String method = httpRequest.getMethod();
    MultivaluedMetadata headerParams = parseHttpHeaders(httpRequest);
    MultivaluedMetadata queryParams = parseQueryParams(httpRequest);

    InputStream in = httpRequest.getInputStream();
    // // TODO Apply Entity resolving strategy here
    // String contentType = httpRequest.getContentType();
    // if(contentType == null)
    // contentType = "application/octet-stream";
    // //
    String port = (httpRequest.getServerPort() == 80) ? "" : ":" + httpRequest.getServerPort();

    String baseURI = httpRequest.getScheme()
        + "://" + httpRequest.getServerName()
        + port
        + httpRequest.getContextPath();

    ResourceIdentifier identifier = new ResourceIdentifier(baseURI, pathInfo);
    return new Request(in, identifier, method, headerParams, queryParams);
  }

  /**
   * Parse headers from http request.
   * @param httpRequest HttpServletRequest 
   * @return Map provide http header in structure Map &lt; String,
   * Enumeration &lt; String &gt; &gt;
   */
  private static MultivaluedMetadata parseHttpHeaders(HttpServletRequest httpRequest) {
    MultivaluedMetadata headerParams = new MultivaluedMetadata();
    Enumeration temp = httpRequest.getHeaderNames();
    while (temp.hasMoreElements()) {
      String k = (String) temp.nextElement();
      Enumeration e = httpRequest.getHeaders(k);
      while (e.hasMoreElements()) {
        headerParams.putSingle(k, (String) e.nextElement());
      }
    }
    return headerParams;
  }

  /**
   * Parse query parameters from http request
   * @param httpRequest HttpServletRequest
   * @return Map provide http query params in structure Map &gt; String, String[] &gt;
   */
  private static MultivaluedMetadata parseQueryParams(HttpServletRequest httpRequest) {
    MultivaluedMetadata queryParams = new MultivaluedMetadata();
    Enumeration temp = httpRequest.getParameterNames();
    while (temp.hasMoreElements()) {
      String k = (String) temp.nextElement();
      String[] params = httpRequest.getParameterValues(k);
      for (int i = 0; i < params.length; i++) {
        queryParams.putSingle(k, params[i]);
      }
    }
    return queryParams;
  }

}
