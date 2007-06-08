/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;


import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.StringRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class AnnotatedParamContainer implements ResourceContainer {

  @HTTPMethod("GET")
  @URITemplate("/level1/{id}/level3/")
  public Response method1(@URIParam("id") String param) {
    System.out.println(">>>>> -method1 called!!!");
    System.out.println("<<<<< id = " + param);
    Response res = Response.getInstance(RESTStatus.OK, new StringRepresentation("method1"));
    return res;
  }
}
