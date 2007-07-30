/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import org.exoplatform.services.rest.container.ResourceContainer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@URITemplate("/level1/{method}/")
public class ResourceContainerGET implements ResourceContainer {

  @HTTPMethod("GET")
  public Response method1(@URIParam("method") String methodName) throws Exception {
    System.out.println(">>>ResourceContainerGET - method: " + methodName);
    return Response.Builder.ok().build();
  }
  
}
