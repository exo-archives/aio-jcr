/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;


import org.exoplatform.services.rest.container.ResourceContainer;
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
  @ConsumedTransformerFactory("org.exoplatform.services.rest.StringEntityTransformerFactory")
  public Response postMethod(String str, @URIParam("id") String param) {
    
    System.out.println("--- POST method called: id = " + param);
    System.out.println("--- request entity - type: " + str.getClass().toString()
        + "; value: " + str);
    
    Response resp = Response.Builder.created("http://localhost/test/_post").build(); 

    return resp;
  }

  // ConsumedTransformerFactory is not defined here becouse
  // request entity represented by InputStream
  @HTTPMethod("PUT")
  @ProducedTransformerFactory("org.exoplatform.services.rest.StringEntityTransformerFactory")
  public Response putMethod(InputStream in, @URIParam("id") String param) throws IOException {
    
    System.out.println("--- PUT method called: id = " + param);
    System.out.println("--- entity type: " + in.getClass().toString() +", value: ");
    DummyEntityTransformer tr = new DummyEntityTransformer(); 
    tr.writeTo(in, System.out);

    String entity = "--- PUT response\n";
    String location = "http://localhost/test/_put"; 
    Response resp =
      Response.Builder.created(entity, location).mediaType("text/plain").build(); 
    return resp;
  }

  @HTTPMethod("DELETE")
  @URITemplate("/{myid}/")
  @ConsumedTransformerFactory("org.exoplatform.services.rest.StringEntityTransformerFactory")
  public Response delMethod(String str, @URIParam("myid") String param) {
    System.out.println("----- DELETE method called: id = " + param);
    System.out.println("----- entity type: " + str.getClass().toString() + ", value: " + str);

    Response resp = Response.Builder.ok().build();
    return resp;
  }
}
