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

public class ResourceContainer2 implements ResourceContainer {

  private static final String STRING_TRANSFORMER = "org.exoplatform.services.rest." +
  		"transformer.StringEntityTransformerFactory";
  
  @HTTPMethod("GET")
  @URITemplate("/level1/{id}/level3/")
  @ProducedMimeTypes("text/*")
  @ConsumedTransformerFactory(STRING_TRANSFORMER)
  @ProducedTransformerFactory(STRING_TRANSFORMER)
  public Response method1(String str, @URIParam("id") String param) {
    System.out.println(">>> method1 called: id = " + param);
    System.out.println(">>> request entity - type: " + str.getClass().toString()
        + "; value: " + str);
    String e = ">>> this is response entity\n";
    Response resp =
      Response.Builder.ok(e, "text/plain").build();

    return resp;
  }
  
  public void methodHu() {
    
  }

}
