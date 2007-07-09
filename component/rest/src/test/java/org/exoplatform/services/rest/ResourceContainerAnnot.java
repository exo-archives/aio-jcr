/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@URITemplate("/level1/level2/")
public class ResourceContainerAnnot implements ResourceContainer {
  
  @HTTPMethod("GET")
  @URITemplate("/level3/{id1}/{id2}/")
  public Response method1(@URIParam("id1") String param) {
    System.out.println(">>> (annot. class) method1 called");
    System.out.println(">>> (annot. class) param = " + param);
    String entity = ">>> annotated container response!!!\n";
    StringOutputTransformer transformer = new StringOutputTransformer();
    Response resp = Response.Builder.ok(entity, "text/plain").transformer(transformer).build();
    return resp;
  }
 
} 
