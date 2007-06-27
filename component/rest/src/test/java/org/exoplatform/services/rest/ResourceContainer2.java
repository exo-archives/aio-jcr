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
  public Response method1(String str, @URIParam("id") String param) {
    System.out.println(">>> method1 called: id = " + param);
    System.out.println(">>> request entity - type: " + str.getClass().toString()
        + "; value: " + str);
    String e = ">>> this is response entity\n";
    StringEntityTransformer transformer = new StringEntityTransformer();
    Response resp =
      Response.Builder.ok(e).type("text/plain").transformer(transformer).build();

    return resp;
  }

}
