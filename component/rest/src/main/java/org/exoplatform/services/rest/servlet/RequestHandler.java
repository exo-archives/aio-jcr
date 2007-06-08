/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceIdentifier;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

/**
 * RequestHandler helps create REST request from HttpServletRequest 
 *
 */
public class RequestHandler {
  
  /**
   * 
   * Create REST request
   * 
   * @param httpRequest 
   * @return REST request
   * @throws IOException
   */
  public static Request createRequest (HttpServletRequest httpRequest) throws IOException {
    InputStream in = httpRequest.getInputStream();
    String pathInfo = httpRequest.getPathInfo();
    String method = httpRequest.getMethod();
    Map<String, Enumeration<String>> headerParams = parseHttpHeaders(httpRequest);
    Map<String, String[]> queryParams = parseQueryParams(httpRequest);

    return Request.getInstance(in, new ResourceIdentifier(pathInfo),
        method, headerParams, queryParams);
  }

  /**
   * 
   * Parse headers from http request
   * 
   * @param httpRequest
   * @return Map provide http header in structure Map<String, Enumeration<String>> 
   */
  public static Map<String, Enumeration<String>> parseHttpHeaders(HttpServletRequest httpRequest) {
    Map<String, Enumeration<String>> headerParms = new HashMap<String, Enumeration<String>>();
    Enumeration<String> temp = httpRequest.getHeaderNames();
    while(temp.hasMoreElements()) {
      String k = temp.nextElement();
      headerParms.put(k, httpRequest.getHeaders(k));
    }
    return headerParms;
  }
  
  /**
   * 
   * Parse query parameters from http request 
   * 
   * @param httpRequest
   * @return Map provide http query params in structure Map<String, String[]>
   */
  public static Map<String, String[]> parseQueryParams(HttpServletRequest httpRequest) {
    Map<String, String[]> queryParams = new HashMap<String, String[]>();
    Enumeration<String> temp = httpRequest.getParameterNames();
    while(temp.hasMoreElements()) {
      String k = temp.nextElement();
      queryParams.put(k, httpRequest.getParameterValues(k));
    }
    return queryParams;
  }
  
}
