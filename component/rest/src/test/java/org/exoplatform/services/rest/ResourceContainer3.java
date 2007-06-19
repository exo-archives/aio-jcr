/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;


import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringEntityTransformer;
import org.exoplatform.services.rest.transformer.DummyEntityTransformer;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

@URITemplate("/level1/{id}/level3/")
public class ResourceContainer3 implements ResourceContainer {

  @HTTPMethod("POST")
  @EntityTransformerClass("org.exoplatform.services.rest.transformer.StringEntityTransformer")
  public Response postMethod(String str, @URIParam("id") String param) {
    System.out.println("----- POST method called!!! id = " + param);
    System.out.println("----- entity type: " + str.getClass().toString() + ", value: " + str);
    EntityMetadata entityMetadata = new EntityMetadata("text/plain");
    Response<String> resp = new Response<String>(RESTStatus.OK, entityMetadata,
        "----- POST response!!!\n", new StringEntityTransformer());
    return resp;
  }

  @HTTPMethod("PUT")
  public Response putMethod(InputStream in, @URIParam("id") String param) throws IOException {
    System.out.println("----- PUT method called!!! id = " + param);
    System.out.print("----- entity type: " + in.getClass().toString() +", value: ");
    DummyEntityTransformer tr = new DummyEntityTransformer(); 
    tr.writeTo(in, System.out);
    EntityMetadata entityMetadata = new EntityMetadata("text/plain");
    Response<String> resp = new Response<String>(RESTStatus.OK, entityMetadata,
        "----- PUT response!!!\n", new StringEntityTransformer());
    return resp;
  }

  @HTTPMethod("DELETE")
  @URITemplate("/{myid}/")
  @EntityTransformerClass("org.exoplatform.services.rest.transformer.StringEntityTransformer")
  public Response delMethod(String str, @URIParam("myid") String param) {
    System.out.println("----- DELETE method called!!! id = " + param);
    System.out.println("----- entity  type: " + str.getClass().toString() + ", value: " + str);
    EntityMetadata entityMetadata = new EntityMetadata("text/plain");
    Response<String> resp = new Response<String>(RESTStatus.OK, entityMetadata,
        "----- DELETE response!!!\n", new StringEntityTransformer());
    return resp;
  }
}
