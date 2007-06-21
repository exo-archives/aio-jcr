/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringEntityTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

@EntityTransformerClass("org.exoplatform.services.rest.transformer.StringEntityTransformer")
public class ResourceContainer2 implements ResourceContainer {

  @HTTPMethod("GET")
  @URITemplate("/level1/{id}/level3/")
  @ProducedMimeTypes("text/*")
  public Response<String> method1(String str, @URIParam("id") String param,
      @BaseURI(true) String uri) {
    System.out.println(">>>>> method1 called!!! uri = " + uri +"; id = " + param);
    System.out.println(">>>>> entity  type: " + str.getClass().toString() + ", value: " + str);
    EntityMetadata entityMetadata = new EntityMetadata("text/plain");
    Response<String> resp = new Response<String>(RESTStatus.OK, entityMetadata,
        ">>>>> response!!!\n", new StringEntityTransformer());
    return resp;
  }

}
