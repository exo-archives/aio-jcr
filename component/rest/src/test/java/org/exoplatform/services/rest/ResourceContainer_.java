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

@URITemplate("/level1/level2/")
public class ResourceContainer_ implements ResourceContainer {
  
  @HTTPMethod("GET")
  @URITemplate("/level3/{id1}/")
  public Response<String> method1(@URIParam("id1") String param) {
    System.out.println(">>>>> (annot. class) method1 called!!!");
    System.out.println(">>>>> (annot. class) param = " + param);
    EntityMetadata entityMetadata = new EntityMetadata("text/plain");
    Response<String> resp = new Response<String>(RESTStatus.OK, entityMetadata,
        ">>>>> response!!!", new StringEntityTransformer());
    return resp;
  }
 
} 
