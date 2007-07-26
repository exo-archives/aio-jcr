/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;


import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.StringInputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@URITemplate("/level1/{id}/le vel3/")
public class ResourceContainer3 implements ResourceContainer {

  @HTTPMethod("POST")
  @InputTransformer(StringInputTransformer.class)
  public Response postMethod(String str, @URIParam("id") String param) {
    System.out.println("--- POST method called: id = " + param);
    System.out.println("--- request entity - type: " + str.getClass().toString()
        + "; value: " + str);
    Response resp = Response.Builder.created("http://localhost/test/_post").build(); 
    return resp;
  }

  @HTTPMethod("PUT")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(StringOutputTransformer.class)
  public Response putMethod(InputStream in, @URIParam("id") String param) throws IOException {
    System.out.println("--- PUT method called: id = " + param);
    System.out.print("--- entity type: " + in.getClass().toString() +", value: ");
    PassthroughOutputTransformer tr = new PassthroughOutputTransformer(); 
    tr.writeTo(in, System.out);
    String entity = "--- PUT response\n";
    String location = "http://localhost/test/_put"; 
    Response resp =
      Response.Builder.created(entity, location).mediaType("text/plain").build(); 
    return resp;
  }

  @HTTPMethod("DELETE")
  @URITemplate("/{myid}/")
  @InputTransformer(StringInputTransformer.class)
  public Response delMethod(String str, @URIParam("myid") String param) {
    System.out.println("--- DELETE method called: id = " + param);
    System.out.println("--- entity type: " + str.getClass().toString() + ", value: " + str);
    Response resp = Response.Builder.ok().build();
    return resp;
  }

  @HTTPMethod("GET")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getMethod(InputStream in, @URIParam("id") String param) throws IOException {
    System.out.println("--- GET method called: id = " + param);
    System.out.print("--- entity type: " + in.getClass().toString() +", value: ");
    PassthroughOutputTransformer tr = new PassthroughOutputTransformer(); 
    tr.writeTo(in, System.out);
    String entity = "--- GET response\n This response is represented by Stream" +
    		" , so Content-Length can't be counted!\n";
    CacheControl cache = new CacheControl();
    cache.setPublicCacheable(!cache.isPublicCacheable()); // ~:)
    cache.setPrivateCacheable(true);
    cache.setNoTransform(false);
    Response resp = Response.Builder.ok(new ByteArrayInputStream(entity.getBytes()),
        "text/plain").cacheControl(cache).build(); 
    return resp;
  }
}
