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
    String uri = httpRequest.getRequestURL().toString();
//    // TODO Apply Entity resolving strategy here
//    String contentType = httpRequest.getContentType();
//    if(contentType == null)
//      contentType = "application/octet-stream";
//    //
    ResourceIdentifier identifier =
      new ResourceIdentifier(uri.substring(0, uri.lastIndexOf(pathInfo)), pathInfo); 
    return new Request(in, identifier, method, headerParams, queryParams);
  }

  /**
   * 
   * Parse headers from http request
   * 
   * @param httpRequest
   * @return Map provide http header in structure Map<String, Enumeration<String>> 
   */
  private static MultivaluedMetadata parseHttpHeaders(HttpServletRequest httpRequest) {
    MultivaluedMetadata headerParams = new MultivaluedMetadata();
    Enumeration temp = httpRequest.getHeaderNames();
    while(temp.hasMoreElements()) {
      String k = (String)temp.nextElement();
      Enumeration e = httpRequest.getHeaders(k);
      while(e.hasMoreElements()) {
        headerParams.putSingle(k, (String)e.nextElement());
      }
    }
    return headerParams;
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
        queryParams.putSingle(k, params[i]);
      }
    }
    return queryParams;
  }
  
}
