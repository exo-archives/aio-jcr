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

public class AutoBindedResourceContainer implements ResourceContainer{

  @HTTPMethod("GET")
  @URITemplate("/level1/{id1}/level2/")
  public Response method_(@URIParam("id1") String param) {
    System.out.println("##### (autobinded container) method_ called!!!");
    System.out.println("##### (autobinded container) param = " + param);
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation("method_"));
    return res;
  }

}
