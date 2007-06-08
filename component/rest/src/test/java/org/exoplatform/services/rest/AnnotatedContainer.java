/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.InputStream;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.BaseEntity;
import org.exoplatform.services.rest.data.BaseRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

@URITemplate("/level1/level2/")
public class AnnotatedContainer implements ResourceContainer {
  
  @HTTPMethod("GET")
  @URITemplate("/level3/{id1}/")
  public Response method1(InputStream entity, @URIParam("id1") String param) {
    System.out.println(">>>>> (annot. class) method1 called!!!");
    System.out.println(">>>>> (annot. class) param = " + param);
    System.out.println(">>>>> (annot. class) entity = " + entity);
    Response res = Response.getInstance(RESTStatus.OK,
        new BaseRepresentation<InputStream>(entity));
    return res;
  }
}
