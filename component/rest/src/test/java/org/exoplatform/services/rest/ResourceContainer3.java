/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;


import org.exoplatform.services.rest.container.ResourceContainer;
import java.io.InputStream;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

//@URITemplate("/level1/{id}/level3/")
public class ResourceContainer3 implements ResourceContainer {

  @HTTPMethod("POST")
  @URITemplate("/level1/{id}/level3/")
  @EntityTransformerClass("any") // this is just test, here must real class name
  public Response postMethod(Object o) {
    return null;
  }

  @HTTPMethod("PUT")
  @URITemplate("/level1/{id}/level3/")
  public Response putMethod(@URIParam("id") String param) {
    return null;
  }

  @HTTPMethod("DELETE")
  @URITemplate("/level1/{id}/level3/")
  // @EntityTransformerClass("any")
  // transformer is not nessered
  // (java.io.InputStream represents HTTP request
  public Response delMethod(InputStream i, @URIParam("id") String param) {
    return null;
  }
}
