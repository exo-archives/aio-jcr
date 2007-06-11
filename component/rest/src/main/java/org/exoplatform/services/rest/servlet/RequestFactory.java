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
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

/**
 * RequestHandler helps create REST request from HttpServletRequest 
 *
 */
public class RequestFactory {
  
  /**
   * 
   * Create REST request
   * 
   * @param httpRequest 
   * @return REST request
   * @throws IOException
   */
  public static Request createRequest (HttpServletRequest httpRequest) throws IOException {
    String pathInfo = httpRequest.getPathInfo();
    String method = httpRequest.getMethod();
    MultivaluedMetadata headerParams = parseHttpHeaders(httpRequest);
    MultivaluedMetadata queryParams = parseQueryParams(httpRequest);
    
    InputStream in = httpRequest.getInputStream();
//    // TODO Apply Entity resolving strategy here
//    String contentType = httpRequest.getContentType();
//    if(contentType == null)
//      contentType = "application/octet-stream";
//    // 
    return new Request(in, new ResourceIdentifier(pathInfo),
        method, headerParams, queryParams);
  }

  /**
   * 
   * Parse headers from http request
   * 
   * @param httpRequest
   * @return Map provide http header in structure Map<String, Enumeration<String>> 
   */
  private static MultivaluedMetadata parseHttpHeaders(HttpServletRequest httpRequest) {
    MultivaluedMetadata headerParms = new MultivaluedMetadata();
    Enumeration temp = httpRequest.getHeaderNames();
    while(temp.hasMoreElements()) {
      String k = (String)temp.nextElement();
      Enumeration e = httpRequest.getHeaders(k);
      while(e.hasMoreElements()) {
        headerParms.add(k, (String)e.nextElement());
      }
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
  private static MultivaluedMetadata parseQueryParams(HttpServletRequest httpRequest) {
    MultivaluedMetadata queryParams = new MultivaluedMetadata();
    Enumeration temp = httpRequest.getParameterNames();
    while(temp.hasMoreElements()) {
      String k = (String)temp.nextElement();
      String[] params = httpRequest.getParameterValues(k);
      for(int i=0; i<params.length; i++) {
        queryParams.add(k, params[i]);
      }
    }
    return queryParams;
  }
  
}
