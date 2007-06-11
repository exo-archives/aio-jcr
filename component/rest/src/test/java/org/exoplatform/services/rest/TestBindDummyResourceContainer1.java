/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.StringEntityTransformer;
import org.exoplatform.services.rest.data.StringRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class TestBindDummyResourceContainer1 implements ResourceContainer {
  
  @HTTPMethod("GET")
  @URITemplate("/level1/level2/level3/")
  @ConsumeMimeType
  public Response method1() {
    System.out.println(">>>>> method1 called!!!");
    Response res = new Response(RESTStatus.OK, new StringRepresentation("method1"), new StringEntityTransformer());
    return res;
  }
}
