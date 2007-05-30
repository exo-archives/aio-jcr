/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.servlet;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceIdentifier;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class ServletUtils {
  
  public static Request createRequest (HttpServletRequest httpRequest) {
    Map<String, Enumeration<String>> headerParm = new HashMap<String, Enumeration<String>>();
    Map<String, String[]> queryParam = new HashMap<String, String[]>();
    String pathInfo = httpRequest.getPathInfo();
    String method = httpRequest.getMethod();
    Request request = new Request(new ResourceIdentifier(pathInfo), method, null);
    
    Enumeration<String> temp = httpRequest.getHeaderNames();
    while(temp.hasMoreElements()) {
      String k = temp.nextElement();
      headerParm.put(k, httpRequest.getHeaders(k));
    }
    request.setHttpHeaderParameters(headerParm);
    
    temp = httpRequest.getParameterNames();
    while(temp.hasMoreElements()) {
      String k = temp.nextElement();
      queryParam.put(k, httpRequest.getParameterValues(k));
    }
    request.setHttpQueryParameters(queryParam);

    return request;
  }
  
}
