/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class TestBindDummyResourceWrapper3 implements ResourceWrapper {

  @HTTPMethod("GET")
  @URITemplate("/level/level2/{id3}/{id4}/")
  @ConsumeMimeType
  public Response method1() {
    System.out.println(">>>>> method1 called!!!");
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation("method1"));
    return res;
  }

}
