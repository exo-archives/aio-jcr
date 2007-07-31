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
@URITemplate("/level1/{id1}/")
public class ResourceContainer4 implements ResourceContainer {
  
  @HTTPMethod("GET")
  @URITemplate("/{id2}/")
  public Response method2(@URIParam("id1") String param1, @URIParam("id2") String param2) {
    System.out.println("=== method2 called: id1 = " + param1);
    System.out.println("=== method2 called: id2 = " + param2);
    Response resp = Response.Builder.noContent().build(); 
    return resp;
  }

  @HTTPMethod("GET")
  @URITemplate("/{id2}/{id3}/{id4}/{id5}/")
  public Response method5(@URIParam("id1") String param1,
      @URIParam("id2") String param2,
      @URIParam("id3") String param3,
      @URIParam("id4") String param4,
      @URIParam("id5") String param5) {
    
    System.out.println("=== method5 called: id1 = " + param1);
    System.out.println("=== method5 called: id2 = " + param2);
    System.out.println("=== method5 called: id3 = " + param3);
    System.out.println("=== method5 called: id4 = " + param4);
    System.out.println("=== method5 called: id5 = " + param5);
    Response resp = Response.Builder.noContent().build(); 
    return resp;
  }

  @HTTPMethod("GET")
  @URITemplate("/{id2}/{id3}/")
  public Response method3(@URIParam("id1") String param1,
      @URIParam("id2") String param2,
      @URIParam("id3") String param3) {
    
    System.out.println("=== method3 called: id1 = " + param1);
    System.out.println("=== method3 called: id2 = " + param2);
    System.out.println("=== method3 called: id3 = " + param3);
    Response resp = Response.Builder.noContent().build(); 
    return resp;
  }
  
  @HTTPMethod("GET")
  public Response method1(@URIParam("id1") String param1) {
    System.out.println("=== method1 called: id1 = " + param1);
    Response resp = Response.Builder.noContent().build(); 
    return resp;
  }

  @HTTPMethod("GET")
  @URITemplate("/{id2}/{id3}/{id4}/")
  public Response method4(@URIParam("id1") String param1,
      @URIParam("id2") String param2,
      @URIParam("id3") String param3,
      @URIParam("id4") String param4) {
    
    System.out.println("=== method4 called: id1 = " + param1);
    System.out.println("=== method4 called: id2 = " + param2);
    System.out.println("=== method4 called: id3 = " + param3);
    System.out.println("=== method4 called: id4 = " + param4);
    Response resp = Response.Builder.noContent().build(); 
    return resp;
  }
}
